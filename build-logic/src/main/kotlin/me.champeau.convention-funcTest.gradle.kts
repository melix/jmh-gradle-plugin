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

import org.gradle.api.internal.artifacts.dsl.dependencies.DependencyFactoryInternal
import org.gradle.internal.component.local.model.OpaqueComponentIdentifier

plugins {
    groovy
    `java-gradle-plugin`
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
    groovy.srcDir("src/funcTest/groovy")
    resources.srcDir("src/funcTest/resources")
}

configurations {
    getByName("functionalTestImplementation").extendsFrom(testImplementation.get())
    getByName("functionalTestRuntimeOnly").extendsFrom(testRuntimeOnly.get())
    val pluginsUnderTest by creating {
        isCanBeConsumed = false
        isCanBeResolved = false
    }
    val pluginClasspath by creating {
        isCanBeConsumed = false
        isCanBeResolved = true
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>(Usage.JAVA_RUNTIME))
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named<Category>(Category.LIBRARY))
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named<Bundling>(Bundling.EXTERNAL))
        }
        extendsFrom(implementation.get(), runtimeOnly.get(), pluginsUnderTest)
    }
    testImplementation.get().extendsFrom(pluginsUnderTest)
}

gradlePlugin {
    testSourceSets(functionalTestSourceSet)
}

val classpathWithoutDevelopmentLibs: ArtifactView = configurations["pluginClasspath"].incoming.artifactView {
    componentFilter { componentId ->
        if (componentId is OpaqueComponentIdentifier) {
            val classPathNotation = componentId.classPathNotation
            return@componentFilter classPathNotation != DependencyFactoryInternal.ClassPathNotation.GRADLE_API &&
                classPathNotation != DependencyFactoryInternal.ClassPathNotation.LOCAL_GROOVY
        }
        true
    }
}

tasks.pluginUnderTestMetadata {
    pluginClasspath.from(classpathWithoutDevelopmentLibs.files.elements)
}

val functionalTest by tasks.registering(Test::class) {
    description = "Runs the functional tests."
    group = "verification"
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    mustRunAfter(tasks.test)

    reports {
        html.outputLocation = project.file("${html.outputLocation.asFile.get().path}/functional")
        junitXml.outputLocation = project.file("${html.outputLocation.asFile.get().path}/functional")
    }
}

tasks.check {
    dependsOn(functionalTest)
}
