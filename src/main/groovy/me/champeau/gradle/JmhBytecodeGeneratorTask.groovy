package me.champeau.gradle

import groovy.transform.CompileStatic
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ConventionTask
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.*
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerConfiguration
import org.gradle.workers.WorkerExecutor

@CompileStatic
class JmhBytecodeGeneratorTask extends ConventionTask {
    private final SourceSetContainer sourceSets = project.convention.getPlugin(JavaPluginConvention).sourceSets

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
    boolean includeTests

    @Input
    String generatorType = 'default'

    @TaskAction
    void generate() {
        def workerExecutor = getServices().get(WorkerExecutor)
        workerExecutor.submit(JmhBytecodeGeneratorRunnable) { WorkerConfiguration config ->
            config.isolationMode = IsolationMode.PROCESS
            def classpath = runtimeClasspath.files
            if (getIncludeTests()) {
                classpath += testClasses.files + testRuntimeClasspath.files
            }
            config.classpath = classpath
            config.params(classesDirs.files as File[], generatedSourcesDir, generatedClassesDir, generatorType)
        }
    }

}
