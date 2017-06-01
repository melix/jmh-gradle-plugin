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
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerConfiguration;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;

/**
 * The JMH task converts our {@link JMHPluginExtension extension configuration} into JMH specific
 * {@link org.openjdk.jmh.runner.options.Options Options} then serializes them to disk. Then a forked
 * JVM is created and a runner is executed using the JMH version that was used to compile the benchmarks.
 * This runner will read the options from the serialized file and execute JMH using them.
 */
public class JMHTask extends DefaultTask {
    private final WorkerExecutor workerExecutor;

    @Inject
    public JMHTask(final WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    @TaskAction
    public void before() {
        final JMHPluginExtension extension = getProject().getExtensions().getByType(JMHPluginExtension.class);
        extension.resolveArgs();
        final ExtensionOptions options = new ExtensionOptions(extension);
        extension.getResultsFile().getParentFile().mkdirs();
        workerExecutor.submit(IsolatedRunner.class, new Action<WorkerConfiguration>() {
            @Override
            public void execute(final WorkerConfiguration workerConfiguration) {
                workerConfiguration.setIsolationMode(IsolationMode.PROCESS);
                workerConfiguration.classpath(getProject().getConfigurations().getByName("jmh").plus(getProject().files(getJarArchive())));
                workerConfiguration.params(options.asSerializable());
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
