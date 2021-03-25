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

import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.file.DuplicatesStrategy;

class DefaultsConfigurer {
    public static void configureDefaults(JmhParameters params, Project project) {
        params.getJmhVersion().convention("1.28");
        params.getIncludeTests().convention(true);
        params.getZip64().convention(false);
        params.getDuplicateClassesStrategy().convention(DuplicatesStrategy.INCLUDE);
        params.getFailOnError().convention(false);
        params.getForceGC().convention(false);
        params.getResultFormat().convention("text");
        params.getResultsFile().convention(
                project.getProviders().zip(params.getResultFormat(), project.getLayout().getBuildDirectory(), (format, dir) ->
                        dir.file("results/" + nameOf(params) + "/results." + extensionFor(format)))
        );
    }

    private static String nameOf(JmhParameters params) {
        if (params instanceof Named) {
            return ((Named) params).getName();
        }
        return "jmh";
    }

    private static String extensionFor(String format) {
        if ("TEXT".equalsIgnoreCase(format)) {
            return "txt";
        }
        return format.toLowerCase();
    }

    public static void configureConvention(JmhParameters from, JmhParameters into) {
        into.getJmhVersion().convention(from.getJmhVersion());
        into.getIncludeTests().convention(from.getIncludeTests());
        into.getIncludes().convention(from.getIncludes());
        into.getExcludes().convention(from.getExcludes());
        into.getBenchmarkMode().convention(from.getBenchmarkMode());
        into.getIterations().convention(from.getIterations());
        into.getBatchSize().convention(from.getBatchSize());
        into.getFork().convention(from.getFork());
        into.getFailOnError().convention(from.getFailOnError());
        into.getForceGC().convention(from.getForceGC());
        into.getJvm().convention(from.getJvm());
        into.getJvmArgs().convention(from.getJvmArgs());
        into.getJvmArgsAppend().convention(from.getJvmArgsAppend());
        into.getJvmArgsPrepend().convention(from.getJvmArgsPrepend());
        into.getOperationsPerInvocation().convention(from.getOperationsPerInvocation());
        into.getBenchmarkParameters().convention(from.getBenchmarkParameters());
        into.getProfilers().convention(from.getProfilers());
        into.getTimeOnIteration().convention(from.getTimeOnIteration());
        into.getResultExtension().convention(from.getResultExtension());
        into.getResultFormat().convention(from.getResultFormat());
        into.getSynchronizeIterations().convention(from.getSynchronizeIterations());
        into.getThreads().convention(from.getThreads());
        into.getThreadGroups().convention(from.getThreadGroups());
        into.getTimeUnit().convention(from.getTimeUnit());
        into.getVerbosity().convention(from.getVerbosity());
        into.getJmhTimeout().convention(from.getJmhTimeout());
        into.getWarmup().convention(from.getWarmup());
        into.getWarmupBatchSize().convention(from.getWarmupBatchSize());
        into.getWarmupForks().convention(from.getWarmupForks());
        into.getWarmupIterations().convention(from.getWarmupIterations());
        into.getWarmupMode().convention(from.getWarmupMode());
        into.getWarmupBenchmarks().convention(from.getWarmupBenchmarks());
        into.getZip64().convention(from.getZip64());
        into.getDuplicateClassesStrategy().convention(from.getDuplicateClassesStrategy());
        into.getJavaLauncher().convention(from.getJavaLauncher());
    }

}
