package me.champeau.gradle;

import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

public interface JmhParameters {
    @Input
    Property<String> getJmhVersion();

    @Input
    Property<Boolean> getIncludeTests();

    @Input
    ListProperty<String> getIncludes();

    @Input
    ListProperty<String> getExcludes();

    @Input
    ListProperty<String> getBenchmarkMode();

    @Input
    @Optional
    Property<Integer> getIterations();

    @Input
    @Optional
    Property<Integer> getBatchSize();

    @Input
    @Optional
    Property<Integer> getFork();

    @Input
    Property<Boolean> getFailOnError();

    @Input
    Property<Boolean> getForceGC();

    @Input
    @Optional
    Property<String> getJvm();

    @Input
    ListProperty<String> getJvmArgs();

    @Input
    ListProperty<String> getJvmArgsAppend();

    @Input
    ListProperty<String> getJvmArgsPrepend();

    @Input
    @Optional
    Property<Integer> getOperationsPerInvocation();

    @Input
    MapProperty<String, ListProperty<String>> getBenchmarkParameters();

    @Input
    @Optional
    ListProperty<String> getProfilers();

    @Input
    @Optional
    Property<String> getTimeOnIteration();

    @Input
    @Optional
    Property<String> getResultExtension();

    @Input
    Property<String> getResultFormat();

    @Input
    @Optional
    Property<Boolean> getSynchronizeIterations();

    @Input
    @Optional
    Property<Integer> getThreads();

    @Input
    ListProperty<Integer> getThreadGroups();

    @Input
    @Optional
    Property<String> getTimeUnit();

    @Input
    @Optional
    Property<String> getVerbosity();

    @Input
    @Optional
    Property<String> getJmhTimeout();

    @Input
    @Optional
    Property<String> getWarmup();

    @Input
    @Optional
    Property<Integer> getWarmupBatchSize();

    @Input
    @Optional
    Property<Integer> getWarmupForks();

    @Input
    @Optional
    Property<Integer> getWarmupIterations();

    @Input
    @Optional
    Property<String> getWarmupMode();

    @Input
    @Optional
    ListProperty<String> getWarmupBenchmarks();

    @Input
    Property<Boolean> getZip64();

    @Input
    Property<DuplicatesStrategy> getDuplicateClassesStrategy();

    @OutputFile
    RegularFileProperty getHumanOutputFile();

    @OutputFile
    RegularFileProperty getResultsFile();
}
