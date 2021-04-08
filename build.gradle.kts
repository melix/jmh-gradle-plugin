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

plugins {
    id("me.champeau.buildscan-recipes") version "0.2.3"
    id("org.nosphere.apache.rat") version "0.7.0"
    id("net.nemerosa.versioning") version "2.6.1"
    id("com.github.ben-manes.versions") version "0.17.0"
    id("com.github.kt3k.coveralls") version "2.8.2"
    id("me.champeau.plugin-configuration")
    id("jacoco")
    id("groovy")
}

buildScanRecipes {
    recipes("git-status", "travis-ci")
    recipe(mapOf("baseUrl" to "https://github.com/melix/jmh-gradle-plugin/tree"), "git-commit")
}

apply(from = "gradle/test.gradle")
apply(from = "gradle/funcTest.gradle")

val jmhVersion: String by project
val spockVersion: String by project
val shadowVersion: String by project
val jacocoVersion: String by project

dependencies {
    "implementation"("org.openjdk.jmh:jmh-core:$jmhVersion")

    testImplementation("org.spockframework:spock-core:$spockVersion") {
        exclude(mapOf("group" to "org.codehaus.groovy"))
    }
    "pluginsUnderTest"("com.github.jengelman.gradle.plugins:shadow:$shadowVersion")

    "testImplementation"("org.openjdk.jmh:jmh-core:$jmhVersion")
    "testImplementation"("org.openjdk.jmh:jmh-generator-bytecode:$jmhVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withSourcesJar()
    withJavadocJar()
}

jacoco {
    toolVersion = jacocoVersion
}

tasks.jacocoTestReport {
    group = "Reporting"
    description = "Generate Jacoco coverage reports after running tests."
    additionalSourceDirs.setFrom(project.files(sourceSets.main.get().allSource.srcDirs))
    sourceDirectories.setFrom(project.files(sourceSets.main.get().allSource.srcDirs))
    classDirectories.setFrom(project.files(sourceSets.main.get().output))
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        html.isEnabled = true
    }
}

tasks.withType<GroovyCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.rat {
    excludes.apply {
        add("README.adoc")
        add("**/build/**")
        add(".github/**")
        add(".idea/**")
        add("**/*.iws")
        add("**/*.iml")
        add("**/*.ipr")
        add("gradle.properties")
        add("gradlew")
        add("gradlew.bat")
        add("gradle/wrapper/gradle-wrapper.properties")
    }

}