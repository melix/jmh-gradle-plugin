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
class JmhBytecodeGeneratorTask extends DefaultTask {
    private final SourceSetContainer sourceSets = project.convention.getPlugin(JavaPluginConvention).sourceSets
    private final Property<Boolean> includeTestsState = project.getObjects().property(Boolean)

    @InputFiles
    FileCollection runtimeClasspath = sourceSets.getByName('jmh').runtimeClasspath

    @InputFiles
    FileCollection testClasses = sourceSets.getByName('test').output

    @InputFiles
    FileCollection testRuntimeClasspath = sourceSets.getByName('test').runtimeClasspath

    @InputFiles
    FileCollection classesDirs = sourceSets.getByName('jmh').output.classesDirs

    @OutputDirectory
    File generatedClassesDir

    @OutputDirectory
    File generatedSourcesDir


    @Input
    String generatorType = 'default'

    @Input
    boolean getIncludeTests() {
        includeTestsState.get()
    }

    void setIncludeTests(Property<Boolean> state) {
        includeTestsState.set(state);
    }

    @TaskAction
    void generate() {
        def workerExecutor = getServices().get(WorkerExecutor)
        workerExecutor.submit(JmhBytecodeGeneratorRunnable) { WorkerConfiguration config ->
            config.isolationMode = IsolationMode.PROCESS
            def classpath = runtimeClasspath.files
            if (includeTests) {
                classpath += testClasses.files + testRuntimeClasspath.files
            }
            config.classpath = classpath
            config.params(classesDirs.files as File[], generatedSourcesDir, generatedClassesDir, generatorType)
        }
    }

}
