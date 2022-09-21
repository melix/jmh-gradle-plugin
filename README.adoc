= JMH Gradle Plugin
:jmh-version: 1.35
:plugin-version: 0.6.8

image:https://github.com/melix/jmh-gradle-plugin/workflows/Main/badge.svg["Build Status", link="https://github.com/melix/jmh-gradle-plugin/actions?query=workflow%3AMain"]
image:https://img.shields.io/coveralls/melix/jmh-gradle-plugin/master.svg["Coverage Status (coveralls)", link="https://coveralls.io/r/melix/jmh-gradle-plugin"]
image:https://img.shields.io/maven-metadata/v.svg?metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fme%2Fchampeau%2Fjmh%2Fme.champeau.jmh.gradle.plugin%2Fmaven-metadata.xml&label=plugin%20portal[Download, link="https://plugins.gradle.org/plugin/me.champeau.jmh"]
image:https://img.shields.io/badge/license-ASF2-blue.svg["Apache License 2", link="https://www.apache.org/licenses/LICENSE-2.0.txt"]

This plugin integrates the https://openjdk.java.net/projects/code-tools/jmh/[JMH micro-benchmarking framework] with Gradle.

== Usage

[source,groovy]
[subs="attributes"]
.build.gradle
----
plugins {
  id "me.champeau.jmh" version "{plugin-version}"
}
----

WARNING: Versions of the plugin prior to 0.6.0 used the `me.champeau.gradle.jmh` plugin id.

Samples can be found in the https://github.com/melix/jmh-gradle-plugin/tree/master/samples[samples] folder.

== What plugin version to use?

Version 0.6+ requires Gradle 6.8+.

[options="header"]
|===
|Gradle|Minimal plugin version
|7.0|0.5.3
|5.5|0.5.0
|5.1|0.4.8
|4.9|0.4.7 (to benefit from lazy tasks API)
|4.8|0.4.5
|4.7|0.4.5
|4.6|0.4.5
|4.5|0.4.5
|4.4|0.4.5
|4.3|0.4.5
|4.2|0.4.4
|4.1|0.4.4
|===

== Configuration

The plugin makes it easy to integrate into an existing project thanks to a specific configuration. In particular,
benchmark source files are expected to be found in the `src/jmh` directory:

----
src/jmh
     |- java       : java sources for benchmarks
     |- resources  : resources for benchmarks
----

The plugin creates a `jmh` configuration that you should use if your benchmark files depend on a 3rd party library.
For example, if you want to use `commons-io`, you can add the dependency like this:

[source,groovy]
.build.gradle
----
dependencies {
    jmh 'commons-io:commons-io:2.4'
}
----

The plugin uses JMH {jmh-version}. You can upgrade the version just by changing the version in the `dependencies` block:

[source,groovy]
.build.gradle
----
dependencies {
    jmh 'org.openjdk.jmh:jmh-core:0.9'
    jmh 'org.openjdk.jmh:jmh-generator-annprocess:0.9'
}
----

== Tasks

The project will add several tasks:

* `jmhClasses`                 : compiles raw benchmark code
* `jmhRunBytecodeGenerator`    : runs bytecode generator over raw benchmark code and generates actual benchmarks
* `jmhCompileGeneratedClasses` : compiles generated benchmarks
* `jmhJar`                     : builds the JMH jar containing the JMH runtime and your compiled benchmark classes
* `jmh`                        : executes the benchmarks

The `jmh` task is the main task and depends on the others so it is in general sufficient to execute this task:

----
gradle jmh
----

== Configuration options

By default, all benchmarks will be executed, and the results will be generated into `$buildDir/results/jmh`. But you
can change various options thanks to the `jmh` configuration block. All configurations variables apart from `includes`
are unset, implying that they fall back to the default JMH values:

[source,groovy]
[subs="attributes"]
.build.gradle
----
jmh {
   includes = ['some regular expression'] // include pattern (regular expression) for benchmarks to be executed
   excludes = ['some regular expression'] // exclude pattern (regular expression) for benchmarks to be executed
   iterations = 10 // Number of measurement iterations to do.
   benchmarkMode = ['thrpt','ss'] // Benchmark mode. Available modes are: [Throughput/thrpt, AverageTime/avgt, SampleTime/sample, SingleShotTime/ss, All/all]
   batchSize = 1 // Batch size: number of benchmark method calls per operation. (some benchmark modes can ignore this setting)
   fork = 2 // How many times to forks a single benchmark. Use 0 to disable forking altogether
   failOnError = false // Should JMH fail immediately if any benchmark had experienced the unrecoverable error?
   forceGC = false // Should JMH force GC between iterations?
   jvm = 'myjvm' // Custom JVM to use when forking.
   jvmArgs = ['Custom JVM args to use when forking.']
   jvmArgsAppend = ['Custom JVM args to use when forking (append these)']
   jvmArgsPrepend =[ 'Custom JVM args to use when forking (prepend these)']
   humanOutputFile = project.file("${project.buildDir}/results/jmh/human.txt") // human-readable output file
   resultsFile = project.file("${project.buildDir}/results/jmh/results.txt") // results file
   operationsPerInvocation = 10 // Operations per invocation.
   benchmarkParameters =  [:] // Benchmark parameters.
   profilers = [] // Use profilers to collect additional data. Supported profilers: [cl, comp, gc, stack, perf, perfnorm, perfasm, xperf, xperfasm, hs_cl, hs_comp, hs_gc, hs_rt, hs_thr, async]
   timeOnIteration = '1s' // Time to spend at each measurement iteration.
   resultFormat = 'CSV' // Result format type (one of CSV, JSON, NONE, SCSV, TEXT)
   synchronizeIterations = false // Synchronize iterations?
   threads = 4 // Number of worker threads to run with.
   threadGroups = [2,3,4] //Override thread group distribution for asymmetric benchmarks.
   timeout = '1s' // Timeout for benchmark iteration.
   timeUnit = 'ms' // Output time unit. Available time units are: [m, s, ms, us, ns].
   verbosity = 'NORMAL' // Verbosity mode. Available modes are: [SILENT, NORMAL, EXTRA]
   warmup = '1s' // Time to spend at each warmup iteration.
   warmupBatchSize = 10 // Warmup batch size: number of benchmark method calls per operation.
   warmupForks = 0 // How many warmup forks to make for a single benchmark. 0 to disable warmup forks.
   warmupIterations = 1 // Number of warmup iterations to do.
   warmupMode = 'INDI' // Warmup mode for warming up selected benchmarks. Warmup modes are: [INDI, BULK, BULK_INDI].
   warmupBenchmarks = ['.*Warmup'] // Warmup benchmarks to include in the run in addition to already selected. JMH will not measure these benchmarks, but only use them for the warmup.

   zip64 = true // Use ZIP64 format for bigger archives
   jmhVersion = '{jmh-version}' // Specifies JMH version
   includeTests = true // Allows to include test sources into generate JMH jar, i.e. use it when benchmarks depend on the test classes.
   duplicateClassesStrategy = DuplicatesStrategy.FAIL // Strategy to apply when encountring duplicate classes during creation of the fat jar (i.e. while executing jmhJar task)
}
----

== JMH Options Mapping

The following table describes the mappings between JMH's command line options and the plugin's extension properties.

[options="header"]
|===
| JMH Option               | Extension Property
| -bm <mode>               | benchmarkMode
| -bs <int>                | batchSize
| -e <regexp+>             | exclude
| -f <int>                 | fork
| -foe <bool>              | failOnError
| -gc <bool>               | forceGC
| -i <int>                 | iterations
| -jvm <string>            | jvm
| -jvmArgs <string>        | jvmArgs
| -jvmArgsAppend <string>  | jvmArgsAppend
| -jvmArgsPrepend <string> | jvmArgsPrepend
| -o <filename>            | humanOutputFile
| -opi <int>               | operationsPerInvocation
| -p <param={v,}*>         | benchmarkParameters?
| -prof <profiler>         | profilers
| -r <time>                | timeOnIteration
| -rf <type>               | resultFormat
| -rff <filename>          | resultsFile
| -si <bool>               | synchronizeIterations
| -t <int>                 | threads
| -tg <int+>               | threadGroups
| -to <time>               | timeout
| -tu <TU>                 | timeUnit
| -v <mode>                | verbosity
| -w <time>                | warmup
| -wbs <int>               | warmupBatchSize
| -wf <int>                | warmupForks
| -wi <int>                | warmupIterations
| -wm <mode>               | warmupMode
| -wmb <regexp+>           | warmupBenchmarks
|===

== Dependency on project files

The `jmh` plugin makes it easy to test existing sources *without* having to create a separate project for this. This is
the reason why you must put your benchmark source files into `src/jmh/java` instead of `src/main/java`. This means that
by default, the `jmh` (benchmarks) task depends on your `main` (production) source set.

It is possible a dependency on the `test` source set by setting property `includeTests` to true inside `jmh` block.

== Using JMH Gradle Plugin with Shadow Plugin

Optionally it is possible to use https://github.com/johnrengelman/shadow/[Shadow Plugin] to do actual JMH jar
creation. The configuration of Shadow Plugin for JMH jar is done via `jmhJar` block.
For example:
[source,groovy]
.build.gradle
----
jmhJar {
  append('META-INF/spring.handlers')
  append('META-INF/spring.schemas')
  exclude 'LICENSE'
}
----

== Duplicate dependencies and classes

This plugin will merge all dependencies that are defined as part of `jmh`, `runtime` and optionally `testRuntime`
configurations into a single set from which fat jar will be created when executing `jmhJar` task. This is done to ensure
that no duplicate dependencies will be added the generated jar.

In addition plugin applies https://docs.gradle.org/current/javadoc/org/gradle/api/file/DuplicatesStrategy.html[DuplicatesStrategy]
defined via `duplicateClassesStrategy` extension property to every class while creating fat jar. By default this
property is set to `DuplicatesStrategy.FAIL` which means that upon detection of
duplicate classes the task will fail.

It is possible to change this behavior by configuring `duplicateClassesStrategy` property via `jmh` block, e.g.:
[source,groovy]
.build.gradle
----
jmh {
  duplicateClassesStrategy = DuplicatesStrategy.WARN
}
----
However if you do encounter problem with defaut value it means that the classpath or sources in your project do contain
duplicate classes which means that it is not possible to predict which one will be used when fat jar will generated.

To deal with duplicate files other than classes use
https://github.com/johnrengelman/shadow/[Shadow Plugin] capabilities, see <<Using JMH Gradle Plugin with Shadow Plugin>>.
