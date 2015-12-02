package me.champeau.gradle.jmh.sample;

import org.openjdk.jmh.annotations.Benchmark;

public class SampleBenchmark {
    private double value = 3.0;

    @Benchmark
    public double sqrtBenchmark(){
        return Math.sqrt(value);
    }
}
