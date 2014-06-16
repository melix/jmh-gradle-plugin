/*
 * Copyright 2014 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.champeau.gradle

import org.gradle.api.Project

/*
Usage: java -jar ... [regexp*] [options]
 [opt] means optional argument.
 <opt> means required argument.
 "+" means comma-separated list of values.
 "time" arguments accept time suffixes, like "100ms".

  [arguments]                 Benchmarks to run (regexp+).
  -bm <mode>                  Benchmark mode. Available modes are: [Throughput/thrpt,
                              AverageTime/avgt, SampleTime/sample, SingleShotTime/ss,
                              All/all]
  -bs <int>                   Batch size: number of benchmark method calls per
                              operation. (some benchmark modes can ignore this
                              setting)
  -e <regexp+>                Benchmarks to exclude from the run.
  -f [int]                    How many times to forks a single benchmark. Use 0 to
                              disable forking altogether (WARNING: disabling
                              forking may have detrimental impact on benchmark
                              and infrastructure reliability, you might want
                              to use different warmup mode instead).
  -foe [bool]                 Should JMH fail immediately if any benchmark had
                              experienced the unrecoverable error?
  -gc [bool]                  Should JMH force GC between iterations?
  -h                          Display help.
  -i <int>                    Number of measurement iterations to do.
  -jvm <string>               Custom JVM to use when forking.
  -jvmArgs <string>           Custom JVM args to use when forking.
  -jvmArgsAppend <string>     Custom JVM args to use when forking (append these)

  -jvmArgsPrepend <string>    Custom JVM args to use when forking (prepend these)

  -l                          List matching benchmarks and exit.
  -lprof                      List profilers.
  -lrf                        List result formats.
  -o <filename>               Redirect human-readable output to file.
  -opi <int>                  Operations per invocation.
  -p <param={v,}*>            Benchmark parameters. This option is expected to
                              be used once per parameter. Parameter name and parameter
                              values should be separated with equals sign. Parameter
                              values should be separated with commas.
  -prof <profiler+>           Use profilers to collect additional data. See the
                              list of available profilers first.
  -r <time>                   Time to spend at each measurement iteration.
  -rf <type>                  Result format type. See the list of available result
                              formats first.
  -rff <filename>             Write results to given file.
  -si [bool]                  Synchronize iterations?
  -t <int>                    Number of worker threads to run with.
  -tg <int+>                  Override thread group distribution for asymmetric
                              benchmarks.
  -tu <TU>                    Output time unit. Available time units are: [m, s,
                              ms, us, ns].
  -v <mode>                   Verbosity mode. Available modes are: [SILENT, NORMAL,
                              EXTRA]
  -w <time>                   Time to spend at each warmup iteration.
  -wbs <int>                  Warmup batch size: number of benchmark method calls
                              per operation. (some benchmark modes can ignore
                              this setting)
  -wf <int>                   How many warmup forks to make for a single benchmark.
                              0 to disable warmup forks.
  -wi <int>                   Number of warmup iterations to do.
  -wm <mode>                  Warmup mode for warming up selected benchmarks.
                              Warmup modes are: [INDI, BULK, BULK_INDI].
  -wmb <regexp+>              Warmup benchmarks to include in the run in addition
                              to already selected. JMH will not measure these benchmarks,
                              but only use them for the warmup.

 */

class JMHPluginExtension {

    private final Project project

    String include = '.*'
    String exclude
    String benchmarkMode
    Integer iterations
    Integer batchSize
    Integer fork
    Boolean failOnError
    Boolean forceGC
    String jvm
    String jvmArgs
    String jvmArgsAppend
    String jvmArgsPrepend
    File humanOutputFile = project.file("${project.buildDir}/reports/jmh/human.txt")
    File resultsFile = project.file("${project.buildDir}/reports/jmh/results.txt")
    Integer operationsPerInvocation
    Map benchmarkParameters
    List<String> profilers
    String timeOnIteration
    String resultFormat
    Boolean synchronizeIterations
    Integer threads
    List<Integer> threadGroups
    String timeUnit
    String verbosity
    String warmup
    Integer warmupBatchSize
    Integer warmupForks
    Integer warmupIterations
    String warmupMode
    List<String> warmupBenchmarks

    JMHPluginExtension(final Project project) {
        this.project = project
    }

    List<String> buildArgs() {
        List<String> args = []
        args.add(include)
        addOption(args, exclude, 'e')
        addOption(args, iterations, 'i')
        addOption(args, benchmarkMode, 'bm')
        addOption(args, batchSize, 'bs')
        addOption(args, fork, 'f')
        addOption(args, failOnError,'foe')
        addOption(args, forceGC, 'gc')
        addOption(args, jvm, 'jvm')
        addOption(args, jvmArgs, 'jvmArgs')
        addOption(args, jvmArgsAppend, 'jvmArgsAppend')
        addOption(args, jvmArgsPrepend, 'jvmArgsPrepend')
        addOption(args, humanOutputFile, 'o')
        addOption(args, operationsPerInvocation, 'opi')
        addOption(args, benchmarkParameters, 'p')
        addOption(args, profilers, 'prof')
        addOption(args, resultsFile, 'rff')
        addOption(args, timeOnIteration, 'r')
        addOption(args, resultFormat, 'rf')
        addOption(args, synchronizeIterations, 'si')
        addOption(args, threads, 't')
        addOption(args, threadGroups, 'tg')
        addOption(args, timeUnit, 'tu')
        addOption(args, verbosity, 'v')
        addOption(args, warmup, 'w')
        addOption(args, warmupBatchSize, 'wbs')
        addOption(args, warmupForks, 'wf')
        addOption(args, warmupIterations, 'wi')
        addOption(args, warmupMode, 'wm')
        addOption(args, warmupBenchmarks, 'wmb')

        args
    }

    private void addOption(List<String> options, String str, String option) {
        if (str!=null) {
            options << "-${option}".toString() << str
        }
    }

    private void addOption(List<String> options, List values, String option) {
        if (values!=null) {
            options << "-${option}".toString() << values.collect { it as String }.join(',')
        }
    }

    private void addOption(List<String> options, Boolean b, String option) {
        if (b!=null) {
            options << "-${option}".toString() << (b?'1':'0')
        }
    }

    private void addOption(List<String> options, Integer i, String option) {
        if (i!=null) {
            options << "-${option}".toString() << (String.valueOf(i))
        }
    }

    private void addOption(List<String> options, File f, String option) {
        if (f!=null) {
            options << "-${option}".toString() << (project.relativePath(f))
        }
    }

    private void addOption(List<String> options, Map params, String option) {
        if (params) {
            options << "-${option}".toString() << params.collect { k,v -> "${k}=${v}" }.join(',')
        }
    }

}
