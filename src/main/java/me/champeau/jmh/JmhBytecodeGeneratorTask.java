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
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;

@CacheableTask
public abstract class JmhBytecodeGeneratorTask extends DefaultTask implements WithJavaToolchain {

    @Inject
    public abstract ExecOperations getExecOperations();

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
    public abstract DirectoryProperty getGeneratedSourcesDir();

    @OutputDirectory
    public abstract DirectoryProperty getGeneratedResourcesDir();

    @TaskAction
    public void generate() {

        // Delete output directories, since JMH doesn't clean up between runs automatically. If we don't delete the
        //  former outputs, we might end up with stale classes.
        cleanup(getGeneratedSourcesDir().get().getAsFile());
        cleanup(getGeneratedResourcesDir().get().getAsFile());

        for (File classesDir : getClassesDirsToProcess()) {
            getExecOperations().javaexec(spec -> {
                spec.setMain("org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator");
                spec.classpath(getJmhClasspath(), getRuntimeClasspath(), getClassesDirsToProcess());
                spec.args(
                        classesDir,
                        getGeneratedSourcesDir().get().getAsFile(),
                        getGeneratedResourcesDir().get().getAsFile(),
                        getGeneratorType().get()
                );
                spec.jvmArgs(getJvmArgs().get());
                Provider<JavaLauncher> javaLauncher = getJavaLauncher();
                if (javaLauncher.isPresent()) {
                    spec.executable(javaLauncher.get().getExecutablePath().getAsFile());
                }
            });
        }
    }

    private static void cleanup(final File file) {
        if (file.exists()) {
            File[] listing = file.listFiles();
            if (listing != null) {
                for (File sub : listing) {
                    cleanup(sub);
                }
            }
            file.delete();
        }
    }
}
