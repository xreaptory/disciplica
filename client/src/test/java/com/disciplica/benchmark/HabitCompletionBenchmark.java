package com.disciplica.benchmark;

import model.domain.model.User;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class HabitCompletionBenchmark {
    private User user;

    @Setup
    public void setup() {
        user = new User("bench-user");
    }

    @Benchmark
    public int habitCompletionCalculation() {
        user.awardXpAndGold(18, 3);
        return user.getExperience() + user.getLevel() + user.getGold();
    }
}
