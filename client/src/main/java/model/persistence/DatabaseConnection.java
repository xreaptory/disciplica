package model.persistence;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DatabaseConnection implements AutoCloseable {
    private final String jdbcUrl;
    private final int maxPoolSize;
    private final int validationTimeoutSeconds;
    private final Deque<Connection> idleConnections = new ArrayDeque<>();
    private final Set<Connection> leasedConnections = new HashSet<>();
    private boolean closed;

    public DatabaseConnection(String jdbcUrl) {
        this(jdbcUrl, 5, Duration.ofSeconds(2));
    }

    public DatabaseConnection(String jdbcUrl, int maxPoolSize, Duration validationTimeout) {
        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("maxPoolSize must be > 0");
        }
        this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl must not be null");
        this.maxPoolSize = maxPoolSize;
        this.validationTimeoutSeconds = (int) Math.max(1, validationTimeout.getSeconds());
    }

    public synchronized Connection getConnection() {
        ensureOpen();
        try {
            Connection physicalConnection = borrowPhysicalConnection();
            leasedConnections.add(physicalConnection);
            return wrapConnection(physicalConnection);
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to acquire database connection", exception);
        }
    }

    public synchronized boolean validateConnection() {
        ensureOpen();
        try (Connection connection = getConnection()) {
            return connection.isValid(validationTimeoutSeconds);
        } catch (SQLException exception) {
            throw new DatabaseException("Connection validation failed", exception);
        }
    }

    private Connection borrowPhysicalConnection() throws SQLException {
        while (!idleConnections.isEmpty()) {
            Connection connection = idleConnections.pop();
            if (connection.isClosed() || !connection.isValid(validationTimeoutSeconds)) {
                silentlyClose(connection);
                continue;
            }
            return connection;
        }
        if (leasedConnections.size() < maxPoolSize) {
            return DriverManager.getConnection(jdbcUrl);
        }
        throw new DatabaseException("No available database connections in pool");
    }

    private Connection wrapConnection(Connection physicalConnection) {
        InvocationHandler handler = new InvocationHandler() {
            private boolean returned;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("close".equals(method.getName())) {
                    if (!returned) {
                        returned = true;
                        returnConnection(physicalConnection);
                    }
                    return null;
                }
                if ("isClosed".equals(method.getName())) {
                    return returned || physicalConnection.isClosed();
                }
                return method.invoke(physicalConnection, args);
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler
        );
    }

    private synchronized void returnConnection(Connection connection) {
        if (closed) {
            silentlyClose(connection);
            return;
        }
        leasedConnections.remove(connection);
        try {
            if (!connection.isClosed() && connection.isValid(validationTimeoutSeconds)) {
                idleConnections.push(connection);
            } else {
                silentlyClose(connection);
            }
        } catch (SQLException exception) {
            silentlyClose(connection);
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new DatabaseException("DatabaseConnection is already closed");
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (Connection connection : leasedConnections) {
            silentlyClose(connection);
        }
        leasedConnections.clear();
        while (!idleConnections.isEmpty()) {
            silentlyClose(idleConnections.pop());
        }
    }

    private void silentlyClose(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
            // intentionally ignored during shutdown/cleanup
        }
    }
}
