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
package me.champeau.gradle

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerConfiguration
import org.gradle.workers.WorkerExecutor

@CompileStatic
@CacheableTask
class JmhBytecodeGeneratorTask extends DefaultTask {
    private final transient SourceSetContainer sourceSets = project.convention.getPlugin(JavaPluginConvention).sourceSets
    private final Property<Boolean> includeTestsState = project.getObjects().property(Boolean).convention(false)

    @Classpath
    FileCollection jmhClasspath

    @Classpath
    FileCollection runtimeClasspath = sourceSets.getByName('jmh').runtimeClasspath

    @Classpath
    FileCollection testClasses = sourceSets.getByName('test').output

    @Classpath
    FileCollection testRuntimeClasspath = sourceSets.getByName('test').runtimeClasspath

    @Classpath
    FileCollection classesDirs = sourceSets.getByName('jmh').output.classesDirs

    @OutputDirectory
    File generatedClassesDir

    @OutputDirectory
    File generatedSourcesDir

    @Input
    String generatorType = 'default'

    @Input
    Property<Boolean> getIncludeTests() {
        includeTestsState
    }

    @TaskAction
    void generate() {
        def workerExecutor = getServices().get(WorkerExecutor)
        workerExecutor.submit(JmhBytecodeGeneratorRunnable) { WorkerConfiguration config ->
            config.isolationMode = IsolationMode.PROCESS
            config.classpath = jmhClasspath
            def benchmarkClasspath = runtimeClasspath.files
            if (includeTests) {
                benchmarkClasspath += testClasses.files + testRuntimeClasspath.files
            }
            config.params(benchmarkClasspath as File[], classesDirs.files as File[], generatedSourcesDir, generatedClassesDir, generatorType)
        }
    }

}
