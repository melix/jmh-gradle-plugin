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

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

@CacheableTask
public abstract class JmhBytecodeGeneratorTask extends DefaultTask implements WithJavaToolchain {

    @Inject
    public abstract WorkerExecutor getWorkerExecutor();

    @Input
    public abstract ListProperty<String> getJvmArgs();

    @Input
    public abstract Property<String> getGeneratorType();

    @Classpath
    public abstract ConfigurableFileCollection getJmhClasspath();

    @Classpath
    public abstract ConfigurableFileCollection getRuntimeClasspath();

    @Classpath
    public abstract ConfigurableFileCollection getClassesDirsToProcess();

    @OutputDirectory
    public abstract DirectoryProperty getGeneratedClassesDir();

    @OutputDirectory
    public abstract DirectoryProperty getGeneratedSourcesDir();

    @TaskAction
    public void generate() {
        getWorkerExecutor().processIsolation(process -> {
            if (getJavaLauncher().isPresent()) {
                process.getForkOptions().executable(getJavaLauncher().get().getExecutablePath().getAsFile());
            }
            process.getClasspath().setFrom(getJmhClasspath());
            process.getForkOptions().jvmArgs(getJvmArgs().get());
        }).submit(JmhBytecodeGeneratorRunnable.class, params -> {
            params.getClasspath().from(getRuntimeClasspath());
            params.getGeneratorType().set(getGeneratorType());
            params.getOutputSourceDirectory().set(getGeneratedSourcesDir());
            params.getOutputResourceDirectory().set(getGeneratedClassesDir());
            params.getClassesDirsToProcess().from(getClassesDirsToProcess());
        });
    }
}
