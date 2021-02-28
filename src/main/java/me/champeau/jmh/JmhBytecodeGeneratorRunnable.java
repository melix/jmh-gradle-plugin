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
package me.champeau.jmh;

import org.gradle.workers.WorkAction;
import org.openjdk.jmh.generators.asm.ASMGeneratorSource;
import org.openjdk.jmh.generators.core.BenchmarkGenerator;
import org.openjdk.jmh.generators.core.FileSystemDestination;
import org.openjdk.jmh.generators.core.GeneratorSource;
import org.openjdk.jmh.generators.core.SourceError;
import org.openjdk.jmh.generators.reflection.RFGeneratorSource;
import org.openjdk.jmh.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator.DEFAULT_GENERATOR_TYPE;
import static org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator.GENERATOR_TYPE_ASM;
import static org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator.GENERATOR_TYPE_DEFAULT;
import static org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator.GENERATOR_TYPE_REFLECTION;

public abstract class JmhBytecodeGeneratorRunnable implements WorkAction<BytecodeGeneratorWorkParameters> {
    @Override
    public void execute() {
        BytecodeGeneratorWorkParameters params = getParameters();
        File outputSourceDirectory = params.getOutputSourceDirectory().getAsFile().get();
        File outputResourceDirectory = params.getOutputResourceDirectory().getAsFile().get();
        cleanup(outputSourceDirectory);
        cleanup(outputResourceDirectory);

        String generatorType = params.getGeneratorType().get();
        if (generatorType.equals(GENERATOR_TYPE_DEFAULT)) {
            generatorType = DEFAULT_GENERATOR_TYPE;
        }

        Set<File> classpath = params.getClasspath().getFiles();
        Set<File> compiledBytecodeDirectories = params.getClassesDirsToProcess().getFiles();
        Set<File> allFiles = new HashSet<>();
        allFiles.addAll(classpath);
        allFiles.addAll(compiledBytecodeDirectories);
        URL[] urls = allFiles.stream()
                .map(JmhBytecodeGeneratorRunnable::toURL)
                .filter(Objects::nonNull)
                .toArray(URL[]::new);

        URLClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());
        ClassLoader ocl = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(loader);

            FileSystemDestination destination = new FileSystemDestination(outputResourceDirectory, outputSourceDirectory);

            Map<File, Collection<File>> allClasses = new HashMap<File, Collection<File>>(urls.length);
            for (File compiledBytecodeDirectory : compiledBytecodeDirectories) {
                Collection<File> classes = FileUtils.getClasses(compiledBytecodeDirectory);
                System.out.println("Processing " + classes.size() + " classes from " + compiledBytecodeDirectory + " with \"" + generatorType + "\" generator");
                allClasses.put(compiledBytecodeDirectory, classes);
            }
            System.out.println("Writing out Java source to " + outputSourceDirectory + " and resources to " + outputResourceDirectory);

            for (Map.Entry<File, Collection<File>> entry : allClasses.entrySet()) {
                File compiledBytecodeDirectory = entry.getKey();
                Collection<File> classes = entry.getValue();
                GeneratorSource source = null;
                if (generatorType.equalsIgnoreCase(GENERATOR_TYPE_ASM)) {
                    ASMGeneratorSource src = new ASMGeneratorSource();
                    try {
                        src.processClasses(classes);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    source = src;
                } else if (generatorType.equalsIgnoreCase(GENERATOR_TYPE_REFLECTION)) {
                    RFGeneratorSource src = new RFGeneratorSource();
                    for (File f : classes) {
                        String name = f.getAbsolutePath().substring(compiledBytecodeDirectory.getAbsolutePath().length() + 1);
                        name = name.replaceAll("\\\\", ".");
                        name = name.replaceAll("/", ".");
                        if (name.endsWith(".class")) {
                            try {
                                src.processClasses(Class.forName(name.substring(0, name.length() - 6), false, loader));
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    source = src;
                }

                BenchmarkGenerator gen = new BenchmarkGenerator();
                gen.generate(source, destination);
                gen.complete(source, destination);
            }


            if (destination.hasErrors()) {
                int errCount = 0;
                StringBuilder sb = new StringBuilder();
                for (SourceError e : destination.getErrors()) {
                    errCount++;
                    sb.append("  - ").append(e.toString()).append("\n");
                }
                throw new RuntimeException("Generation of JMH bytecode failed with " + errCount + " errors:\n" + sb);
            }
        } finally {
            try {
                loader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread.currentThread().setContextClassLoader(ocl);
        }
    }

    private static URL toURL(File f) {
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
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
