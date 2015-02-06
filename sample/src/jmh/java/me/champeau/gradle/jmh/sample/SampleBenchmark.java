package me.champeau.gradle.jmh.sample;

import org.openjdk.jmh.annotations.Benchmark;

public class SampleBenchmark {

    @Benchmark
    public double sqrtBenchmark(){
        // return prevents dead code removal!
        return Math.sqrt(3.0);
    }
}
