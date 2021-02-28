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
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * The JMH task is responsible for launching a JMH benchmark.
 */
public abstract class JMHTask extends DefaultTask implements JmhParameters {
    private final static String JAVA_IO_TMPDIR = "java.io.tmpdir";

    @Inject
    public abstract ExecOperations getExecOperations();

    @Inject
    public abstract ObjectFactory getObjects();

    @Classpath
    public abstract ConfigurableFileCollection getJmhClasspath();

    @Classpath
    public abstract ConfigurableFileCollection getTestRuntimeClasspath();

    @InputFile
    public abstract RegularFileProperty getJarArchive();


    @OutputFile
    public abstract RegularFileProperty getHumanOutputFile();

    @OutputFile
    public abstract RegularFileProperty getResultsFile();

    @TaskAction
    public void callJmh() {
        List<String> jmhArgs = new ArrayList<>();
        ParameterConverter.collectParameters(this, jmhArgs);
        getExecOperations().javaexec(spec -> {
            spec.setClasspath(computeClasspath());
            spec.getMainClass().set("org.openjdk.jmh.Main");
            spec.args(jmhArgs);
            spec.systemProperty(JAVA_IO_TMPDIR, getTemporaryDir().getAbsolutePath());
            Provider<JavaLauncher> javaLauncher = getJavaLauncher();
            if (javaLauncher.isPresent()) {
                spec.executable(javaLauncher.get().getExecutablePath().getAsFile());
            }
        });
    }

    private FileCollection computeClasspath() {
        ConfigurableFileCollection classpath = getObjects().fileCollection();
        classpath.from(getJmhClasspath());
        classpath.from(getJarArchive());
        classpath.from(getTestRuntimeClasspath());
        return classpath;
    }

}
