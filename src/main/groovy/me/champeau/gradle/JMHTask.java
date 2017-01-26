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

import me.champeau.jmh.runner.SerializableOptions;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;

/**
 * The JMH task converts our {@link JMHPluginExtension extension configuration} into JMH specific
 * {@link org.openjdk.jmh.runner.options.Options Options} then serializes them to disk. Then a forked
 * JVM is created and a runner is executed using the JMH version that was used to compile the benchmarks.
 * This runner will read the options from the serialized file and execute JMH using them.
 */
public class JMHTask extends JavaExec {

    @TaskAction
    public void exec() {
        final JMHPluginExtension extension = getProject().getExtensions().getByType(JMHPluginExtension.class);
        extension.resolveArgs();
        ExtensionOptions options = new ExtensionOptions(extension);
        File parentFile = extension.getResultsFile().getParentFile();
        createOptionsFile(options, parentFile);
        super.exec();
    }

    private File createOptionsFile(final ExtensionOptions options, final File parentFile) {
        try {
            File tempFile = File.createTempFile("options-", ".bin", getTemporaryDir());
            if (parentFile.exists() || parentFile.mkdirs()) {
                SerializableOptions serializable = options.asSerializable();
                ObjectOutputStream oos = null;
                try {
                    oos = new ObjectOutputStream(new FileOutputStream(tempFile));
                    oos.writeObject(serializable);
                    oos.flush();
                } finally {
                    if (oos != null) {
                        oos.close();
                    }
                }
                setArgs(Collections.singleton(tempFile.getAbsolutePath()));
            } else {
                throw new GradleException("Unable to create output directory " + parentFile);
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
