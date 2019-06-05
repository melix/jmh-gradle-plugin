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

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerConfiguration;
import org.gradle.workers.WorkerExecutor;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;

/**
 * The JMH task converts our {@link JMHPluginExtension extension configuration} into JMH specific
 * {@link org.openjdk.jmh.runner.options.Options Options} then serializes them to disk. Then a forked
 * JVM is created and a runner is executed using the JMH version that was used to compile the benchmarks.
 * This runner will read the options from the serialized file and execute JMH using them.
 */
public class JMHTask extends DefaultTask {
    private final static String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private final WorkerExecutor workerExecutor;

    @Inject
    public JMHTask(final WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    @TaskAction
    public void before() {
        final JMHPluginExtension extension = getProject().getExtensions().getByType(JMHPluginExtension.class);
        final Options options = extension.resolveArgs();
        extension.getResultsFile().getParentFile().mkdirs();

        workerExecutor.submit(IsolatedRunner.class, new Action<WorkerConfiguration>() {
            @Override
            public void execute(final WorkerConfiguration workerConfiguration) {
                workerConfiguration.setIsolationMode(IsolationMode.PROCESS);
                ConfigurationContainer configurations = getProject().getConfigurations();
                FileCollection classpath = configurations.getByName("jmh").plus(getProject().files(getJarArchive()));
                if (extension.isIncludeTests()) {
                    classpath = classpath.plus(configurations.getByName("testRuntimeClasspath"));
                }
                // TODO: This isn't quite right.  JMH is already a part of the worker classpath,
                // but we need it to be part of the "classpath under test" too.
                // We only need the jar for the benchmarks on the classpath so that the BenchmarkList resource reader
                // can find the BenchmarkList file in the jar.
                workerConfiguration.classpath(classpath);
                workerConfiguration.params(options, classpath.getFiles());
                workerConfiguration.getForkOptions().getSystemProperties().put(JAVA_IO_TMPDIR, getTemporaryDir());
            }
        });
    }

    @Override
    public void setDidWork(final boolean didWork) {
        super.setDidWork(didWork);
    }

    private File getJarArchive() {
        return ((Jar)getProject().getTasks().getByName(JMHPlugin.JMH_JAR_TASK_NAME)).getArchivePath();
    }

}
