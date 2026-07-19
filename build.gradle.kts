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
    alias(libs.plugins.buildScanRecipes)
    alias(libs.plugins.apache.rat)
    alias(libs.plugins.versioning)
    alias(libs.plugins.coveralls)
    id("com.github.gmazzo.buildconfig") version "6.0.10"
    id("me.champeau.convention-test")
    id("me.champeau.convention-funcTest")
    id("me.champeau.plugin-configuration")
}

buildScanRecipes {
    recipes("git-status", "travis-ci")
    recipe(mapOf("baseUrl" to "https://github.com/melix/jmh-gradle-plugin/tree"), "git-commit")
}

buildConfig {
    packageName = "me.champeau.jmh"
    useJavaOutput()
    sourceSets.named("main") {
        buildConfigField("JMH_VERSION", libs.versions.jmh)
    }
}

dependencies {
    implementation(libs.jmh.core)

    testImplementation(libs.spock.core) {
        exclude(mapOf("group" to "org.apache.groovy"))
    }
    testImplementation(localGroovy())
    testImplementation(gradleTestKit())

    pluginsUnderTest(libs.foojayResolver)
    pluginsUnderTest(libs.shadow.gradlePlugin)

    testImplementation(libs.jmh.generatorBytecode)
    testImplementation(libs.apache.commonsIo)
    testImplementation(libs.shadow.gradlePlugin)
    testRuntimeOnly(libs.junit.platformLauncher)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

jacoco {
    toolVersion = "0.8.14"
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
        add("**/.kotlin/**")
        add("**/*.iws")
        add("**/*.iml")
        add("**/*.ipr")
        add("gradle.properties")
        add("gradlew")
        add("gradlew.bat")
        add("gradle/wrapper/gradle-wrapper.properties")
        add("gradle/libs.versions.toml")
    }
}
