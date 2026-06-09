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

/**
 * Einfacher Verbindungspool für Datenbankverbindungen.
 * <p>
 * Hält eine begrenzte Anzahl an Verbindungen bereit und gibt sie über
 * {@link #getConnection()} aus. Wird eine ausgeliehene Verbindung
 * geschlossen, kehrt sie tatsächlich in den Pool zurück, statt physisch
 * geschlossen zu werden. Beim Schließen des Pools werden alle Verbindungen
 * freigegeben.
 */
public class DatabaseConnection implements AutoCloseable {
    private final String jdbcUrl;
    private final int maxPoolSize;
    private final int validationTimeoutSeconds;
    private final Deque<Connection> idleConnections = new ArrayDeque<>();
    private final Set<Connection> leasedConnections = new HashSet<>();
    private boolean closed;

    /**
     * Erzeugt einen Pool mit Standardgröße (5 Verbindungen) und einem
     * Prüf-Zeitlimit von 2 Sekunden.
     *
     * @param jdbcUrl die JDBC-Adresse der Datenbank
     */
    public DatabaseConnection(String jdbcUrl) {
        this(jdbcUrl, 5, Duration.ofSeconds(2));
    }

    /**
     * Erzeugt einen Pool mit den angegebenen Einstellungen.
     *
     * @param jdbcUrl           die JDBC-Adresse der Datenbank
     * @param maxPoolSize       die maximale Anzahl gleichzeitiger Verbindungen
     *                          (muss größer als 0 sein)
     * @param validationTimeout das Zeitlimit für die Gültigkeitsprüfung einer
     *                          Verbindung
     * @throws IllegalArgumentException wenn {@code maxPoolSize} nicht positiv
     *                                  ist
     */
    public DatabaseConnection(String jdbcUrl, int maxPoolSize, Duration validationTimeout) {
        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("maxPoolSize must be > 0");
        }
        this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl must not be null");
        this.maxPoolSize = maxPoolSize;
        this.validationTimeoutSeconds = (int) Math.max(1, validationTimeout.getSeconds());
    }

    /**
     * Leiht eine Verbindung aus dem Pool aus. Die zurückgegebene Verbindung
     * kehrt beim Schließen automatisch in den Pool zurück.
     *
     * @return eine einsatzbereite Datenbankverbindung
     * @throws DatabaseException wenn der Pool geschlossen ist oder keine
     *                           Verbindung beschafft werden kann
     */
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

    /**
     * Prüft, ob eine Verbindung aus dem Pool gültig ist.
     *
     * @return {@code true}, wenn eine gültige Verbindung hergestellt werden
     *         kann
     * @throws DatabaseException wenn die Prüfung fehlschlägt
     */
    public synchronized boolean validateConnection() {
        ensureOpen();
        try (Connection connection = getConnection()) {
            return connection.isValid(validationTimeoutSeconds);
        } catch (SQLException exception) {
            throw new DatabaseException("Connection validation failed", exception);
        }
    }

    /**
     * Beschafft eine physische Verbindung – bevorzugt aus dem Vorrat
     * ungenutzter Verbindungen, sonst durch Neuaufbau.
     *
     * @return eine gültige physische Verbindung
     * @throws SQLException      bei einem Fehler beim Verbindungsaufbau
     * @throws DatabaseException wenn die maximale Poolgröße erreicht ist
     */
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

    /**
     * Umhüllt eine physische Verbindung mit einem Stellvertreter, dessen
     * {@code close()}-Aufruf die Verbindung in den Pool zurückgibt, statt sie
     * zu schließen.
     *
     * @param physicalConnection die zu umhüllende physische Verbindung
     * @return die umhüllte Verbindung
     */
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

    /**
     * Gibt eine Verbindung in den Pool zurück oder schließt sie, falls sie
     * nicht mehr gültig oder der Pool geschlossen ist.
     *
     * @param connection die zurückzugebende Verbindung
     */
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

    /**
     * Stellt sicher, dass der Pool noch geöffnet ist.
     *
     * @throws DatabaseException wenn der Pool bereits geschlossen wurde
     */
    private void ensureOpen() {
        if (closed) {
            throw new DatabaseException("DatabaseConnection is already closed");
        }
    }

    /**
     * Schließt den Pool und gibt alle ausgeliehenen und ungenutzten
     * Verbindungen frei.
     */
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

    /**
     * Schließt eine Verbindung und unterdrückt dabei etwaige Fehler (für den
     * Aufräum-/Abschaltvorgang).
     *
     * @param connection die zu schließende Verbindung
     */
    private void silentlyClose(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
            // Während Abschaltung/Aufräumen bewusst ignoriert
        }
    }
}
