package com.disciplica.benchmark;

import model.domain.model.Habit;
import model.domain.model.User;
import model.persistence.SQLiteHabitRepository;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class DatabaseQueryBenchmark {
    private SQLiteHabitRepository repository;
    private User user;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        user = new User("bench-db-user");
        Path dbFile = Files.createTempFile("disciplica-jmh-", ".db");
        repository = new SQLiteHabitRepository(user, "jdbc:sqlite:" + dbFile.toAbsolutePath());

        for (int i = 0; i < 1000; i++) {
            repository.save(new Habit("habit-" + i, "desc-" + i));
        }
    }

    @Benchmark
    public void queryHabitsForUser1000Rows(Blackhole blackhole) {
        List<Habit> habits = repository.findByUser(user);
        blackhole.consume(habits.size());
    }
}
