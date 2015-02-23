package me.champeau.gradle.jmh.sample;

import org.openjdk.jmh.annotations.Benchmark;

public class SampleBenchmark {

    @Benchmark
    public void sqrtBenchmark(){
        Math.sqrt(3.0);
    }
}
