/*
 * Copyright 2003-2012 the original author or authors.
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

import org.gradle.api.GradleException;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;

import javax.inject.Inject;
import java.io.File;
import java.util.Set;

public class IsolatedRunner implements Runnable {

    private final Options options;
    private Set<File> classpathUnderTest;

    @Inject
    public IsolatedRunner(final Options options, final Set<File> classpathUnderTest) {
        this.options = options;
        this.classpathUnderTest = classpathUnderTest;
    }

    @Override
    public void run() {
        String originalClasspath = System.getProperty("java.class.path");
        Runner runner = new Runner(options);
        try {
            System.setProperty("java.class.path", toPath(classpathUnderTest));
            // JMH uses the system property java.class.path to derive the runtime classpath of the forked JVM
            runner.run();
        } catch (RunnerException e) {
            throw new GradleException("Error during execution of benchmarks", e);
        } finally {
            runner.runSystemGC();
            if (originalClasspath != null) {
                System.setProperty("java.class.path", originalClasspath);
            } else {
                System.clearProperty("java.class.path");
            }
        }
    }

    private String toPath(Set<File> classpathUnderTest) {
        StringBuilder sb = new StringBuilder();
        for (File entry : classpathUnderTest) {
            sb.append(entry.getAbsolutePath());
            sb.append(File.pathSeparatorChar);
        }
        return sb.toString();
    }
}
