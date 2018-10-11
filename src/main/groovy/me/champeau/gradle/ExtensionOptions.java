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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class ExtensionOptions implements Options {
    private final JMHPluginExtension extension;

    public ExtensionOptions(final JMHPluginExtension extension) {
        this.extension = extension;
    }

    @Override
    public List<String> getIncludes() {
        return extension.getInclude();
    }

    @Override
    public List<String> getExcludes() {
        return extension.getExclude();
    }

    @Override
    public Optional<String> getOutput() {
        return fromNullableFile(extension.getHumanOutputFile());
    }

    private Optional<String> fromNullableFile(final File humanOutputFile) {
        if (humanOutputFile != null) {
            return Optional.of(humanOutputFile.getAbsolutePath());
        }
        return Optional.none();
    }

    @Override
    public Optional<ResultFormatType> getResultFormat() {
        String resultFormat = extension.getResultFormat();
        if (resultFormat != null) {
            return Optional.of(ResultFormatType.valueOf(resultFormat.toUpperCase()));
        }
        return Optional.none();
    }

    @Override
    public Optional<String> getResult() {
        return fromNullableFile(extension.getResultsFile());
    }

    @Override
    public Optional<Boolean> shouldDoGC() {
        return Optional.eitherOf(extension.getForceGC());
    }

    @Override
    public List<ProfilerConfig> getProfilers() {
        List<String> profilers = extension.getProfilers();
        if (profilers == null) {
            return Collections.emptyList();
        }
        List<ProfilerConfig> configs = new ArrayList<ProfilerConfig>(profilers.size());
        for (String profiler : profilers) {
            int idx = profiler.indexOf(":");
            String profName = (idx == -1) ? profiler : profiler.substring(0, idx);
            String params = (idx == -1) ? "" : profiler.substring(idx + 1);
            configs.add(new ProfilerConfig(profName, params));
        }
        return configs;
    }

    @Override
    public Optional<VerboseMode> verbosity() {
        String verbosity = extension.getVerbosity();
        if (verbosity != null) {
            return Optional.of(VerboseMode.valueOf(verbosity.toUpperCase()));
        }
        return Optional.none();
    }

    @Override
    public Optional<Boolean> shouldFailOnError() {
        return Optional.eitherOf(extension.getFailOnError());
    }

    @Override
    public Optional<Integer> getThreads() {
        return Optional.eitherOf(extension.getThreads());
    }

    @Override
    public Optional<int[]> getThreadGroups() {
        List<Integer> threadGroups = extension.getThreadGroups();
        if (threadGroups != null) {
            int[] arr = new int[threadGroups.size()];
            Iterator<Integer> it = threadGroups.iterator();
            for (int i = 0; i < arr.length; i++) {
                arr[i] = it.next();
            }
            return Optional.of(arr);
        }
        return Optional.none();
    }

    @Override
    public Optional<Boolean> shouldSyncIterations() {
        return Optional.eitherOf(extension.getSynchronizeIterations());
    }

    @Override
    public Optional<Integer> getWarmupIterations() {
        return Optional.eitherOf(extension.getWarmupIterations());
    }

    @Override
    public Optional<TimeValue> getWarmupTime() {
        if (extension.getWarmup() != null) {
            return Optional.of(TimeValue.fromString(extension.getWarmup()));
        }
        return Optional.none();
    }

    @Override
    public Optional<Integer> getWarmupBatchSize() {
        return Optional.eitherOf(extension.getWarmupBatchSize());
    }

    @Override
    public Optional<WarmupMode> getWarmupMode() {
        String warmupMode = extension.getWarmupMode();
        if (warmupMode != null) {
            return Optional.of(WarmupMode.valueOf(warmupMode.toUpperCase()));
        }
        return Optional.none();
    }

    @Override
    public List<String> getWarmupIncludes() {
        List<String> warmupBenchmarks = extension.getWarmupBenchmarks();
        return warmupBenchmarks != null ? warmupBenchmarks : Collections.<String>emptyList();
    }

    @Override
    public Optional<Integer> getMeasurementIterations() {
        return Optional.eitherOf(extension.getIterations());
    }

    @Override
    public Optional<TimeValue> getMeasurementTime() {
        if (extension.getTimeOnIteration() != null) {
            return Optional.of(TimeValue.fromString(extension.getTimeOnIteration()));
        }
        return Optional.none();
    }

    @Override
    public Optional<Integer> getMeasurementBatchSize() {
        return Optional.eitherOf(extension.getBatchSize());
    }

    @Override
    public Collection<Mode> getBenchModes() {
        List<String> benchmarkMode = extension.getBenchmarkMode();
        List<Mode> modes = new ArrayList<Mode>();
        if (benchmarkMode!=null) {
            for (String str : benchmarkMode) {
                modes.add(Mode.deepValueOf(str));
            }
        }
        return modes;
    }

    @Override
    public Optional<TimeUnit> getTimeUnit() {
        String timeUnit = extension.getTimeUnit();
        if (timeUnit != null) {
            return Optional.of(toTimeUnit(timeUnit.toLowerCase()));
        }
        return Optional.none();
    }

    @Override
    public Optional<Integer> getOperationsPerInvocation() {
        return Optional.eitherOf(extension.getOperationsPerInvocation());
    }

    @Override
    public Optional<Integer> getForkCount() {
        return Optional.eitherOf(extension.getFork());
    }

    @Override
    public Optional<Integer> getWarmupForkCount() {
        return Optional.eitherOf(extension.getWarmupForks());
    }

    @Override
    public Optional<String> getJvm() {
        return Optional.eitherOf(extension.getJvm());
    }

    @Override
    public Optional<Collection<String>> getJvmArgs() {
        return Optional.eitherOf((Collection<String>)extension.getJvmArgs());
    }

    @Override
    public Optional<Collection<String>> getJvmArgsAppend() {
        Collection<String> jvmArgsAppend = extension.getJvmArgsAppend();
        return Optional.eitherOf(jvmArgsAppend);
    }

    @Override
    public Optional<Collection<String>> getJvmArgsPrepend() {
        Collection<String> jvmArgsPrepend = extension.getJvmArgsPrepend();
        return Optional.eitherOf(jvmArgsPrepend);
    }

    @Override
    public Optional<Collection<String>> getParameter(final String name) {
        Map<String, Collection<String>> benchmarkParameters = extension.getBenchmarkParameters();
        if (benchmarkParameters != null) {
            Collection<String> stringCollection = benchmarkParameters.get(name);
            return Optional.eitherOf(stringCollection);
        }
        return Optional.none();
    }

    @Override
    public Optional<TimeValue> getTimeout() {
        if (extension.getTimeout() != null) {
            return Optional.of(TimeValue.fromString(extension.getTimeout()));
        }
        return Optional.none();
    }

    private TimeUnit toTimeUnit(String str) {
        TimeUnit tu;
        if (str.equalsIgnoreCase("ns")) {
            tu = TimeUnit.NANOSECONDS;
        } else if (str.equalsIgnoreCase("us")) {
            tu = TimeUnit.MICROSECONDS;
        } else if (str.equalsIgnoreCase("ms")) {
            tu = TimeUnit.MILLISECONDS;
        } else if (str.equalsIgnoreCase("s")) {
            tu = TimeUnit.SECONDS;
        } else if (str.equalsIgnoreCase("m")) {
            tu = TimeUnit.MINUTES;
        } else if (str.equalsIgnoreCase("h")) {
            tu = TimeUnit.HOURS;
        } else {
            throw new IllegalArgumentException("Unknown time unit: " + str);
        }
        return tu;
    }

    public SerializableOptions asSerializable() {
        Map<String, Collection<String>> benchmarkParameters = extension.getBenchmarkParameters();
        Map<String, Optional<Collection<String>>> asOptional = new HashMap<String, Optional<Collection<String>>>();
        if (benchmarkParameters != null) {
            for (Map.Entry<String, Collection<String>> entry : benchmarkParameters.entrySet()) {
                asOptional.put(entry.getKey(), Optional.eitherOf(entry.getValue()));
            }
        }
        return new SerializableOptions(this, asOptional);

    }
}
