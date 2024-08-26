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
    jacoco
    id("me.champeau.buildscan-recipes") version "0.2.3"
    id("org.nosphere.apache.rat") version "0.8.1"
    id("net.nemerosa.versioning") version "3.1.0"
    id("com.github.kt3k.coveralls") version "2.12.2"
    id("me.champeau.convention-test")
    id("me.champeau.convention-funcTest")
    id("me.champeau.plugin-configuration")
}

buildScanRecipes {
    recipes("git-status", "travis-ci")
    recipe(mapOf("baseUrl" to "https://github.com/melix/jmh-gradle-plugin/tree"), "git-commit")
}

dependencies {
    val jmhVersion = "1.37"
    implementation("org.openjdk.jmh:jmh-core:$jmhVersion")

    testImplementation("org.spockframework:spock-core:2.3-groovy-3.0") {
        exclude(mapOf("group" to "org.codehaus.groovy"))
    }
    pluginsUnderTest("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")
    pluginsUnderTest("com.gradleup.shadow:shadow-gradle-plugin:8.3.0")

    testImplementation("org.openjdk.jmh:jmh-core:$jmhVersion")
    testImplementation("org.openjdk.jmh:jmh-generator-bytecode:$jmhVersion")
    testImplementation("commons-io:commons-io:2.16.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
    withSourcesJar()
    withJavadocJar()
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    group = "Reporting"
    description = "Generate Jacoco coverage reports after running tests."
    additionalSourceDirs = files(sourceSets.main.get().allSource.srcDirs)
    sourceDirectories = files(sourceSets.main.get().allSource.srcDirs)
    classDirectories = files(sourceSets.main.get().output)
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
