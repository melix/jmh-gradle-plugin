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
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.gradle.work.DisableCachingByDefault;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The JMH task is responsible for launching a JMH benchmark.
 */
@DisableCachingByDefault(because = "Benchmark results depend on the runtime environment and should not be cached")
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

    @Classpath
    public abstract RegularFileProperty getJarArchive();

    @OutputFile
    @Optional
    public abstract RegularFileProperty getHumanOutputFile();

    @OutputFile
    public abstract RegularFileProperty getResultsFile();

    /**
     * Allows passing arbitrary JMH command line options at invocation time, e.g.
     * <pre>
     *     ./gradlew jmh --jmhArgs="-t 4 -wi 5 -i 10"
     * </pre>
     * The value is tokenized on whitespace and replaces matching flags from the build script.
     *
     * @return the raw JMH options, space separated
     */
    @Optional
    @Input
    @Option(option = "jmhArgs", description = "Arbitrary JMH command line options to pass to the JMH runner, space separated.")
    public abstract Property<String> getJmhArgs();

    @TaskAction
    public void callJmh() {
        List<String> args = new ArrayList<>();
        ParameterConverter.collectParameters(this, args);
        if (getJmhArgs().isPresent()) {
            String raw = getJmhArgs().get();
            if (!raw.trim().isEmpty()) {
                applyCliArgs(args, raw.trim().split("\\s+"));
            }
        }
        getLogger().info("Running JMH with arguments: " + args);
        getExecOperations().javaexec(spec -> {
            spec.setClasspath(computeClasspath());
            spec.getMainClass().set("org.openjdk.jmh.Main");
            spec.args(args);
            spec.systemProperty(JAVA_IO_TMPDIR, getTemporaryDir().getAbsolutePath());
            spec.environment(getEnvironment().get());
            Provider<JavaLauncher> javaLauncher = getJavaLauncher();
            if (javaLauncher.isPresent()) {
                spec.executable(javaLauncher.get().getExecutablePath().getAsFile());
            }
        });
    }

    /**
     * Applies CLI tokens to the args list: matching flags get their values replaced,
     * presence-only flags that appear in CLI are kept; new flags are appended.
     */
    private static void applyCliArgs(List<String> args, String[] tokens) {
        // presence-only flags (no value)
        Set<String> presenceFlags = new LinkedHashSet<>(Arrays.asList(
                "-l", "-lp", "-lprof", "-lrf", "-h", "-hh"));

        for (int i = 0; i < tokens.length; i++) {
            String flag = tokens[i];
            if (!flag.startsWith("-")) {
                args.add(flag);
                continue;
            }

            String value = null;
            if (!presenceFlags.contains(flag)) {
                boolean hasNextToken = i + 1 < tokens.length;
                boolean nextIsValue = hasNextToken && !tokens[i + 1].startsWith("-");
                if (nextIsValue) {
                    value = tokens[++i];
                }
            }

            // replace matching flag in existing args, or append
            int idx = args.indexOf(flag);
            if (idx >= 0) {
                boolean hasBuildscriptValue = idx + 1 < args.size()
                        && !args.get(idx + 1).startsWith("-");
                if (value != null && hasBuildscriptValue) {
                    args.set(idx + 1, value);
                }
            } else {
                args.add(flag);
                if (value != null) {
                    args.add(value);
                }
            }
        }
    }

    private FileCollection computeClasspath() {
        ConfigurableFileCollection classpath = getObjects().fileCollection();
        classpath.from(getJmhClasspath());
        classpath.from(getJarArchive());
        classpath.from(getTestRuntimeClasspath());
        return classpath;
    }

}
