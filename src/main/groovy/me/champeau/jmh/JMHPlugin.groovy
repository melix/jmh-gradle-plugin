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
package me.champeau.jmh

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.eclipse.EclipseWtpPlugin
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.util.GradleVersion

/**
 * Configures the JMH Plugin.
 */
class JMHPlugin implements Plugin<Project> {
    private static boolean IS_GRADLE_MIN = GradleVersion.current() >= GradleVersion.version('6.8')

    public static final String JMH_CORE_DEPENDENCY = 'org.openjdk.jmh:jmh-core:'
    public static final String JMH_GENERATOR_DEPENDENCY = 'org.openjdk.jmh:jmh-generator-bytecode:'
    public static final String JMH_GROUP = 'jmh'
    public static final String JMH_NAME = 'jmh'
    public static final String JMH_JAR_TASK_NAME = 'jmhJar'
    public static final String JMH_TASK_COMPILE_GENERATED_CLASSES_NAME = 'jmhCompileGeneratedClasses'
    public static final String JHM_RUNTIME_CLASSPATH_CONFIGURATION = 'jmhRuntimeClasspath'

    void apply(Project project) {
        assertMinimalGradleVersion()
        project.plugins.apply(JavaPlugin)
        final JmhParameters extension = project.extensions.create(JMH_NAME, JmhParameters)
        DefaultsConfigurer.configureDefaults(extension, project)
        final Configuration configuration = project.configurations.create(JMH_NAME)
        final Configuration runtimeConfiguration = createJmhRuntimeClasspathConfiguration(project, extension)

        DependencyHandler dependencyHandler = project.getDependencies()
        dependencyHandler.addProvider(JMH_NAME, project.providers.provider { "${JMH_CORE_DEPENDENCY}${extension.jmhVersion.get()}" }) {}
        dependencyHandler.addProvider(JMH_NAME, project.providers.provider { "${JMH_GENERATOR_DEPENDENCY}${extension.jmhVersion.get()}" }) {}

        def hasShadow = project.plugins.findPlugin('com.github.johnrengelman.shadow') != null

        createJmhSourceSet(project)

        registerBuildListener(project, extension)

        def jmhGeneratedSourcesDir = project.layout.buildDirectory.dir("jmh-generated-sources")
        def jmhGeneratedClassesDir = project.layout.buildDirectory.dir("jmh-generated-classes")
        def jmhGeneratedResourcesDir = project.layout.buildDirectory.dir("jmh-generated-resources")
        def runtimeBytecodeGeneraratorTask = createJmhRunBytecodeGeneratorTask(project, jmhGeneratedSourcesDir, extension, jmhGeneratedResourcesDir)
        def jmhCompileGenerated = createJmhCompileGeneratedClassesTask(project, jmhGeneratedSourcesDir, jmhGeneratedClassesDir, extension)

        def metaInfExcludes = ['module-info.class', 'META-INF/*.SF', 'META-INF/*.DSA', 'META-INF/*.RSA']
        TaskProvider<Jar> jmhJar = null
        if (hasShadow) {
            jmhJar = createShadowJmhJar(project, extension, jmhGeneratedResourcesDir, jmhGeneratedClassesDir, metaInfExcludes, runtimeConfiguration)
        } else {
            jmhJar = createStandardJmhJar(project, extension, metaInfExcludes, jmhGeneratedResourcesDir, jmhGeneratedClassesDir, runtimeConfiguration)
        }

        project.tasks.withType(JMHTask).configureEach {
            DefaultsConfigurer.configureConvention(extension, it)
            usesService(ConcurrentExecutionControlBuildService.restrict(JMHTask, project.gradle))
        }

        project.tasks.register(JMH_NAME, JMHTask) {
            it.group JMH_GROUP
            it.jmhClasspath.from(configuration)
            it.testRuntimeClasspath.from(runtimeConfiguration)
            it.jarArchive.set(jmhJar.flatMap { it.archiveFile })
            it.resultsFile.convention(extension.resultsFile)
            it.humanOutputFile.convention(extension.humanOutputFile)
        }


        configureIDESupport(project)
    }

    private static void assertMinimalGradleVersion() {
        if (!IS_GRADLE_MIN) {
            throw new RuntimeException("This version of the JMH Gradle plugin requires Gradle 6+, you are using ${GradleVersion.current().version}. Please upgrade Gradle or use an older version of the plugin.");
        }
    }

    private configureIDESupport(Project project) {
        project.afterEvaluate {
            def hasIdea = project.plugins.findPlugin(IdeaPlugin)
            if (hasIdea) {
                project.idea {
                    module {
                        scopes.TEST.plus += [project.configurations.jmh]
                    }
                }
                project.idea {
                    module {
                        project.sourceSets.jmh.java.srcDirs.each {
                            testSourceDirs += project.file(it)
                        }
                    }
                }
            }
            def hasEclipsePlugin = project.plugins.findPlugin(EclipsePlugin)
            def hasEclipseWtpPlugin = project.plugins.findPlugin(EclipseWtpPlugin)
            if (hasEclipsePlugin != null || hasEclipseWtpPlugin != null) {
                project.eclipse {
                    classpath.plusConfigurations += [project.configurations.jmh]
                }
            }
        }
    }

    private static TaskProvider<JavaCompile> createJmhCompileGeneratedClassesTask(Project project,
                                                                                  Provider<Directory> jmhGeneratedSourcesDir,
                                                                                  Provider<Directory> jmhGeneratedClassesDir,
                                                                                  JmhParameters extension) {
        project.tasks.register(JMH_TASK_COMPILE_GENERATED_CLASSES_NAME, JavaCompile) {
            it.group JMH_GROUP
            it.dependsOn 'jmhRunBytecodeGenerator'

            it.classpath = project.sourceSets.jmh.runtimeClasspath
            if (extension.includeTests.get()) {
                it.classpath += project.sourceSets.test.output + project.sourceSets.test.runtimeClasspath
            }
            it.source(jmhGeneratedSourcesDir)
            it.destinationDirectory.set(jmhGeneratedClassesDir)
        }
    }

    private static TaskProvider<JmhBytecodeGeneratorTask> createJmhRunBytecodeGeneratorTask(Project project,
                                                                                            Provider<Directory> jmhGeneratedSourcesDir,
                                                                                            JmhParameters extension,
                                                                                            Provider<Directory> jmhGeneratedResourcesDir) {
        project.tasks.register('jmhRunBytecodeGenerator', JmhBytecodeGeneratorTask) {
            it.group JMH_GROUP
            it.jmhClasspath.from(project.configurations.jmh)
            it.generatorType.convention('default')
            it.generatedClassesDir.set(jmhGeneratedResourcesDir)
            it.generatedSourcesDir.set(jmhGeneratedSourcesDir)
            it.runtimeClasspath.from(project.sourceSets.jmh.runtimeClasspath)
            it.classesDirsToProcess.from(project.sourceSets.jmh.output.classesDirs)
            if (extension.includeTests.get()) {
                it.runtimeClasspath.from(project.sourceSets.test.runtimeClasspath)
                it.classesDirsToProcess.from(project.sourceSets.test.output.classesDirs)
            }
        }
    }

    private void createJmhSourceSet(Project project) {
        project.sourceSets {
            jmh {
                java.srcDir 'src/jmh/java'
                resources.srcDir 'src/jmh/resources'
                compileClasspath += main.output
                runtimeClasspath += main.output
            }
        }
        project.configurations.with {
            // the following line is for backwards compatibility
            // no one should really add directly to the "jmh" configuration
            jmhImplementation.extendsFrom(jmh)

            jmhCompileClasspath.extendsFrom(implementation, compileOnly)
            jmhRuntimeClasspath.extendsFrom(implementation, runtimeOnly)
        }
    }

    private TaskProvider<Jar> createShadowJmhJar(Project project, JmhParameters extension,
                                                 Provider<Directory> jmhGeneratedResourcesDir,
                                                 Provider<Directory> jmhGeneratedClassesDir,
                                                 List<String> metaInfExcludes,
                                                 FileCollection runtimeConfiguration) {
        project.tasks.register(JMH_JAR_TASK_NAME, Class.forName('com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar', true, JMHPlugin.classLoader)) {
            it.group = JMH_GROUP
            it.dependsOn(JMH_TASK_COMPILE_GENERATED_CLASSES_NAME)
            it.description = 'Create a combined JAR of project and runtime dependencies'
            it.conventionMapping.with {
                map('classifier') {
                    JMH_NAME
                }
            }
            it.manifest.inheritFrom project.tasks.jar.manifest
            it.manifest.attributes 'Main-Class': 'org.openjdk.jmh.Main'
            it.from(runtimeConfiguration)
            FileCollection shadowConfiguration = project.configurations.shadow
            def testSourceSetOutput = project.sourceSets.test.output
            it.doFirst { task ->
                def processLibs = { files ->
                    if (files) {
                        def libs = [task.manifest.attributes.get('Class-Path')]
                        libs.addAll files.collect { it.name }
                        task.manifest.attributes 'Class-Path': libs.unique().join(' ')
                    }
                }
                processLibs runtimeConfiguration.files
                processLibs shadowConfiguration.files

                if (extension.includeTests.get()) {
                    task.from(testSourceSetOutput)
                }
                task.eachFile { FileCopyDetails f ->
                    if (f.name.endsWith('.class')) {
                        f.setDuplicatesStrategy(extension.duplicateClassesStrategy.get())
                    }
                }
            }
            it.from(project.sourceSets.jmh.output)
            it.from(project.sourceSets.main.output)
            it.from(project.file(jmhGeneratedClassesDir))
            it.from(project.file(jmhGeneratedResourcesDir))

            it.exclude(metaInfExcludes)
            it.configurations = []
            it.zip64 = extension.zip64.get()
        }
    }

    private TaskProvider<Jar> createStandardJmhJar(Project project,
                                                   JmhParameters extension,
                                                   List<String> metaInfExcludes,
                                                   Provider<Directory> jmhGeneratedResourcesDir,
                                                   Provider<Directory> jmhGeneratedClassesDir,
                                                   Configuration runtimeConfiguration) {
        project.tasks.register(JMH_JAR_TASK_NAME, Jar) {
            it.group JMH_GROUP
            it.dependsOn JMH_TASK_COMPILE_GENERATED_CLASSES_NAME
            it.inputs.files project.sourceSets.jmh.output
            it.inputs.files project.sourceSets.main.output
            it.duplicatesStrategy = extension.duplicateClassesStrategy.get()
            if (extension.includeTests.get()) {
                it.inputs.files project.sourceSets.test.output
            }
            it.from {
                runtimeConfiguration.asFileTree.collect { File f ->
                    f.isDirectory() ? f : project.zipTree(f)
                }
            }.exclude(metaInfExcludes)
            def jmhSourceSetOutput = project.sourceSets.jmh.output
            def mainSourceSetOutput = project.sourceSets.main.output
            def testSourceSetOutput = project.sourceSets.test.output

            it.from(jmhSourceSetOutput)
            it.from(mainSourceSetOutput)
            it.from(jmhGeneratedClassesDir)
            it.from(jmhGeneratedResourcesDir)
            if (extension.includeTests.get()) {
                it.from(testSourceSetOutput)
            }

            it.manifest {
                attributes 'Main-Class': 'org.openjdk.jmh.Main'
            }

            it.archiveClassifier = JMH_NAME
            it.zip64 = extension.zip64.get()
        }
    }

    private void registerBuildListener(
            final Project project, final JmhParameters extension) {
        project.gradle.projectsEvaluated {
            if (extension.includeTests.get()) {
                project.sourceSets {
                    jmh {
                        compileClasspath += test.output + project.configurations.testCompileClasspath
                        runtimeClasspath += test.output + project.configurations.testRuntimeClasspath
                    }
                }
            }
        }
    }

    @CompileStatic
    private static Configuration createJmhRuntimeClasspathConfiguration(Project project, JmhParameters extension) {
        def newConfig = project.configurations.create(JHM_RUNTIME_CLASSPATH_CONFIGURATION)
        newConfig.setCanBeConsumed(false)
        newConfig.setCanBeResolved(true)
        newConfig.setVisible(false)
        newConfig.extendsFrom(project.configurations.getByName('jmh'))
        newConfig.extendsFrom(project.configurations.getByName('runtimeClasspath'))
        project.afterEvaluate {
            if (extension.includeTests.get()) {
                newConfig.extendsFrom(project.configurations.getByName('testRuntimeClasspath'))
            }
        }
        newConfig
    }
}