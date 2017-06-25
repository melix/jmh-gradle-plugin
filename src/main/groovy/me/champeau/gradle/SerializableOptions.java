/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.champeau.gradle;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.ProfilerConfig;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.VerboseMode;
import org.openjdk.jmh.runner.options.WarmupMode;
import org.openjdk.jmh.util.Optional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SerializableOptions implements Options, Serializable {

    private static final long serialVersionUID = -1927363889083220761L;

    private List<String> includes;
    private List<String> excludes;
    private Optional<String> output;
    private Optional<ResultFormatType> resultFormat;
    private Optional<String> result;
    private Optional<Boolean> shouldDoGC;
    private List<ProfilerConfig> profilers;
    private Optional<VerboseMode> verbosity;
    private Optional<Boolean> shouldFailOnError;
    private Optional<Integer> threads;
    private Optional<int[]> threadGroups;
    private Optional<Boolean> shouldSyncIterations;
    private Optional<Integer> warmupIterations;
    private Optional<TimeValue> warmupTime;
    private Optional<Integer> warmupBatchSize;
    private Optional<WarmupMode> warmupMode;
    private List<String> warmupIncludes;
    private Optional<Integer> measurementIterations;
    private Optional<TimeValue> measurementTime;
    private Optional<Integer> measurementBatchSize;
    private Collection<Mode> benchModes;
    private Optional<TimeUnit> timeUnit;
    private Optional<Integer> operationsPerInvocation;
    private Optional<Integer> forkCount;
    private Optional<Integer> warmupForkCount;
    private Optional<String> jvm;
    private Optional<Collection<String>> jvmArgs;
    private Optional<Collection<String>> jvmArgsAppend;
    private Optional<Collection<String>> jvmArgsPrepend;
    private Map<String, Optional<Collection<String>>> parameters;
    private Optional<TimeValue> timeout;

    public SerializableOptions() {
    }

    public SerializableOptions(Options source, Map<String, Optional<Collection<String>>> params) {
        includes = source.getIncludes();
        excludes = source.getExcludes();
        output = source.getOutput();
        resultFormat = source.getResultFormat();
        result = source.getResult();
        shouldDoGC = source.shouldDoGC();
        profilers = source.getProfilers();
        verbosity = source.verbosity();
        shouldFailOnError = source.shouldFailOnError();
        threads = source.getThreads();
        threadGroups = source.getThreadGroups();
        shouldSyncIterations = source.shouldSyncIterations();
        warmupIterations = source.getWarmupIterations();
        warmupTime = source.getWarmupTime();
        warmupBatchSize = source.getWarmupBatchSize();
        warmupMode = source.getWarmupMode();
        warmupIncludes = source.getWarmupIncludes();
        measurementIterations = source.getMeasurementIterations();
        measurementTime = source.getMeasurementTime();
        measurementBatchSize = source.getMeasurementBatchSize();
        benchModes = source.getBenchModes();
        timeUnit = source.getTimeUnit();
        operationsPerInvocation = source.getOperationsPerInvocation();
        forkCount = source.getForkCount();
        warmupForkCount = source.getWarmupForkCount();
        jvm = source.getJvm();
        jvmArgs = source.getJvmArgs();
        jvmArgsAppend = source.getJvmArgsAppend();
        jvmArgsPrepend = source.getJvmArgsPrepend();
        timeout = source.getTimeout();
        parameters = params;
    }

    @Override
    public List<String> getIncludes() {
        return includes;
    }

    @Override
    public List<String> getExcludes() {
        return excludes;
    }

    @Override
    public Optional<String> getOutput() {
        return output;
    }

    @Override
    public Optional<ResultFormatType> getResultFormat() {
        return resultFormat;
    }

    @Override
    public Optional<String> getResult() {
        return result;
    }

    @Override
    public Optional<Boolean> shouldDoGC() {
        return shouldDoGC;
    }

    @Override
    public List<ProfilerConfig> getProfilers() {
        return profilers;
    }

    @Override
    public Optional<VerboseMode> verbosity() {
        return verbosity;
    }

    @Override
    public Optional<Boolean> shouldFailOnError() {
        return shouldFailOnError;
    }

    @Override
    public Optional<Integer> getThreads() {
        return threads;
    }

    @Override
    public Optional<int[]> getThreadGroups() {
        return threadGroups;
    }

    @Override
    public Optional<Boolean> shouldSyncIterations() {
        return shouldSyncIterations;
    }

    @Override
    public Optional<Integer> getWarmupIterations() {
        return warmupIterations;
    }

    @Override
    public Optional<TimeValue> getWarmupTime() {
        return warmupTime;
    }

    @Override
    public Optional<Integer> getWarmupBatchSize() {
        return warmupBatchSize;
    }

    @Override
    public Optional<WarmupMode> getWarmupMode() {
        return warmupMode;
    }

    @Override
    public List<String> getWarmupIncludes() {
        return warmupIncludes;
    }

    @Override
    public Optional<Integer> getMeasurementIterations() {
        return measurementIterations;
    }

    @Override
    public Optional<TimeValue> getMeasurementTime() {
        return measurementTime;
    }

    @Override
    public Optional<Integer> getMeasurementBatchSize() {
        return measurementBatchSize;
    }

    @Override
    public Collection<Mode> getBenchModes() {
        return benchModes;
    }

    @Override
    public Optional<TimeUnit> getTimeUnit() {
        return timeUnit;
    }

    @Override
    public Optional<Integer> getOperationsPerInvocation() {
        return operationsPerInvocation;
    }

    @Override
    public Optional<Integer> getForkCount() {
        return forkCount;
    }

    @Override
    public Optional<Integer> getWarmupForkCount() {
        return warmupForkCount;
    }

    @Override
    public Optional<String> getJvm() {
        return jvm;
    }

    @Override
    public Optional<Collection<String>> getJvmArgs() {
        return jvmArgs;
    }

    @Override
    public Optional<Collection<String>> getJvmArgsAppend() {
        return jvmArgsAppend;
    }

    @Override
    public Optional<Collection<String>> getJvmArgsPrepend() {
        return jvmArgsPrepend;
    }

    @Override
    public Optional<Collection<String>> getParameter(final String name) {
        Optional<Collection<String>> optional = parameters.get(name);
        return optional == null ? Optional.<Collection<String>>none() : optional;
    }

    @Override
    public Optional<TimeValue> getTimeout() {
        return timeout;
    }
}
