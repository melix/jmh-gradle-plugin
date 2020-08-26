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
import org.openjdk.jmh.runner.BenchmarkList;
import org.openjdk.jmh.runner.CompilerHints;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

public class IsolatedRunner implements Runnable {

    private final Options options;
    private final Set<File> classpathUnderTest;
    private final File benchmarkList;
    private final File compilerHints;
    private final String jmhVersion;

    @Inject
    public IsolatedRunner(Options options,
                          Set<File> classpathUnderTest,
                          File benchmarkList,
                          File compilerHints,
                          String jmhVersion) {
        this.options = options;
        this.classpathUnderTest = classpathUnderTest;
        this.benchmarkList = benchmarkList;
        this.compilerHints = compilerHints;
        this.jmhVersion = jmhVersion;
    }

    @Override
    public void run() {
        String originalClasspath = System.getProperty("java.class.path");
        Runner runner = new Runner(options);
        updateBenchmarkList(runner);
        updateCompilerHints();
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

    private void updateBenchmarkList(Runner runner) {
        BenchmarkList benchmarkList = BenchmarkList.fromFile(this.benchmarkList.getAbsolutePath());
        tryUpdateFieldViaReflection(runner.getClass(), runner, "list", benchmarkList);
    }

    private void updateCompilerHints() {
        CompilerHints compilerHints = CompilerHints.fromFile(this.compilerHints.getAbsolutePath());
        tryUpdateFieldViaReflection(CompilerHints.class, null, "defaultList", compilerHints);
    }

    private static void makeWriteable(Field listField) throws NoSuchFieldException, IllegalAccessException {
        listField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(listField, listField.getModifiers() & ~Modifier.FINAL);
    }

    private void tryUpdateFieldViaReflection(Class<?> clazz, Object target, String fieldName, Object value) {
        try {
            Field listField = clazz.getDeclaredField(fieldName);
            makeWriteable(listField);
            listField.set(target, value);
        } catch (Exception e) {
            throw new GradleException(
                    "Error while instantiating tests: unable to set '" + fieldName + "' on " + clazz.getSimpleName() +
                            ". This plugin version doesn't seem to be compatible with JMH " + jmhVersion +
                            ". Please report to the plugin authors at https://github.com/melix/jmh-gradle-plugin/.", e);
        }
    }

    private static String toPath(Set<File> classpathUnderTest) {
        StringBuilder sb = new StringBuilder();
        for (File entry : classpathUnderTest) {
            sb.append(entry.getAbsolutePath());
            sb.append(File.pathSeparatorChar);
        }
        return sb.toString();
    }
}
