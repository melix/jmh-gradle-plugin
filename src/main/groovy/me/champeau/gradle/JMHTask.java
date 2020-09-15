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

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerExecutor;
import org.openjdk.jmh.runner.options.Options;

import javax.inject.Inject;
import java.io.File;

/**
 * The JMH task converts our {@link JMHPluginExtension extension configuration} into JMH specific
 * {@link org.openjdk.jmh.runner.options.Options Options} then serializes them to disk. Then a forked
 * JVM is created and a runner is executed using the JMH version that was used to compile the benchmarks.
 * This runner will read the options from the serialized file and execute JMH using them.
 */
public class JMHTask extends DefaultTask {
    private final static String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private final ObjectFactory objects;
    private final WorkerExecutor workerExecutor;
    private final JMHPluginExtension extension = getProject().getExtensions().getByType(JMHPluginExtension.class);

    private File benchmarkList;
    private File compilerHints;

    @Classpath
    FileCollection jmhClasspath = getProject().getConfigurations().getByName("jmh");

    @Classpath
    FileCollection testRuntimeClasspath = getProject().getConfigurations().getByName("testRuntimeClasspath");

    @Classpath
    Provider<RegularFile> jarArchive;

    @Inject
    public JMHTask(ObjectFactory objects, WorkerExecutor workerExecutor) {
        this.objects = objects;
        this.workerExecutor = workerExecutor;
    }

    @TaskAction
    public void before() {
        final Options options = extension.resolveArgs();

        extension.getResultsFile().getParentFile().mkdirs();

        workerExecutor.submit(IsolatedRunner.class, workerConfiguration -> {
            workerConfiguration.setIsolationMode(IsolationMode.PROCESS);
            workerConfiguration.classpath(jmhClasspath);
            FileCollection benchmarkClasspath = jmhClasspath.plus(objects.fileCollection().from(jarArchive));
            if (extension.isIncludeTests()) {
                benchmarkClasspath = benchmarkClasspath.plus(testRuntimeClasspath);
            }
            workerConfiguration.params(options, benchmarkClasspath.getFiles(), benchmarkList, compilerHints, extension.getJmhVersion());
            workerConfiguration.getForkOptions().getSystemProperties().put(JAVA_IO_TMPDIR, getTemporaryDir());
        });
    }

    @Override
    public void setDidWork(final boolean didWork) {
        super.setDidWork(didWork);
    }

    public void setBenchmarkList(File benchmarkList) {
        this.benchmarkList = benchmarkList;
    }

    public void setCompilerHints(File compilerHints) {
        this.compilerHints = compilerHints;
    }
}
