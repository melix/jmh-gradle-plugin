/*
 * Copyright 2014-2021 the original author or authors.
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
package me.champeau.jmh;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class ParameterConverter {
    public static void collectParameters(JmhParameters from, final List<String> into) {
        // ordered as when running -help
        addOption(into, from.getIncludes(), "");
        addIntOption(into, from.getIterations(), "i");
        addOption(into, from.getBenchmarkMode(), "bm");
        addIntOption(into, from.getBatchSize(), "bs");
        addOption(into, from.getExcludes(), "e");
        addIntOption(into, from.getFork(), "f");
        addBooleanOption(into, from.getFailOnError(), "foe");
        addBooleanOption(into, from.getForceGC(), "gc");
        addOption(into, from.getJvm(), "jvm");
        addOption(into, from.getJvmArgs(), "jvmArgs");
        addOption(into, from.getJvmArgsAppend(), "jvmArgsAppend");
        addOption(into, from.getJvmArgsPrepend(), "jvmArgsPrepend");
        addFileOption(into, from.getHumanOutputFile(), "o");
        addIntOption(into, from.getOperationsPerInvocation(), "opi");
        addMapOption(into, from.getBenchmarkParameters(), "p");
        addRepeatableOption(into, from.getProfilers(), "prof");
        addOption(into, from.getTimeOnIteration(), "r");
        addOption(into, from.getResultFormat(), "rf");
        addFileOption(into, from.getResultsFile(), "rff");
        addBooleanOption(into, from.getSynchronizeIterations(), "si");
        addIntOption(into, from.getThreads(), "t");
        addOption(into, from.getThreadGroups(), "tg");
        addOption(into, from.getJmhTimeout(), "to");
        addOption(into, from.getTimeUnit(), "tu");
        addOption(into, from.getVerbosity(), "v");
        addOption(into, from.getWarmup(), "w");
        addIntOption(into, from.getWarmupBatchSize(), "wbs");
        addIntOption(into, from.getWarmupForks(), "wf");
        addIntOption(into, from.getWarmupIterations(), "wi");
        addOption(into, from.getWarmupMode(), "wm");
        addOption(into, from.getWarmupBenchmarks(), "wmb");
    }

    private static <T> void addOption(List<String> into, Provider<T> str, String option) {
        if (str.isPresent()) {
            into.add("-" + option);
            into.add(String.valueOf(str.get()));
        }
    }

    private static <T> void addOption(List<String> options, ListProperty<T> values, String option) {
        addOption(options, values, option, ",");
    }

    private static <T> void addOption(List<String> options, final ListProperty<T> values, String option, final String separator) {
        if (values.isPresent()) {
            List<T> list = values.get();
            if (!list.isEmpty()) {
                if (!option.isEmpty()) {
                    options.add("-" + option);
                }
                String joined = list.stream()
                        .map(Object::toString)
                        .collect(joining(separator));
                options.add(joined);
            }
        }

    }

    private static <T> void addRepeatableOption(List<String> options, ListProperty<T> values, String option) {
        if (values.isPresent()) {
            List<T> listOfValues = values.get();
            for (Object value : listOfValues) {
                options.add("-" + option);
                options.add(String.valueOf(value));
            }
        }
    }

    private static void addBooleanOption(List<String> options, Provider<Boolean> b, String option) {
        if (b.isPresent()) {
            options.add("-" + option);
            options.add(b.get() ? "1" : "0");
        }

    }

    private static void addIntOption(List<String> options, Provider<Integer> i, String option) {
        if (i.isPresent()) {
            options.add("-" + option);
            options.add(String.valueOf(i.get()));
        }

    }

    private static void addFileOption(List<String> options, RegularFileProperty f, String option) {
        if (f.isPresent()) {
            options.add("-" + option);
            options.add(f.getAsFile().get().getAbsolutePath());
        }

    }

    private static void addMapOption(List<String> options, MapProperty<String, ListProperty<String>> params, String option) {
        if (params.isPresent()) {
            Map<String, ListProperty<String>> map = params.get();
            map.forEach((key, listProperty) -> {
                List<String> value = listProperty.get();
                for (String str : value) {
                    options.add("-" + option);
                    options.add(key + "=" + str);
                }
            });
        }
    }
}
