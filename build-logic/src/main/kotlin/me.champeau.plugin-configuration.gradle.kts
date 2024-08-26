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
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
    id("com.vanniktech.maven.publish")
}

val buildTimeAndDate: Date by lazy {
    if ((version as String).endsWith("SNAPSHOT")) {
        Date(0)
    } else {
        Date()
    }
}
val buildDate: String by lazy {
    SimpleDateFormat("yyyy-MM-dd").format(buildTimeAndDate)
}
val buildTime: String by lazy {
    SimpleDateFormat("HH:mm:ss.SSSZ").format(buildTimeAndDate)
}

tasks.jar {
    manifest {
        attributes(
            "Built-By" to systemProp("user.name"),
            "Created-By" to systemProp("java.version") + " (" + systemProp("java.vendor") + " " + systemProp("java.vm.version") + ")",
            "Build-Date" to buildDate,
            "Build-Time" to buildTime,
//            "Build-Revision" to versioning.info.commit,
            "Specification-Title" to project.name,
            "Specification-Version" to project.version,
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
    metaInf {
        from(files(rootProject.rootDir)) {
            include("LICENSE*")
        }
    }
}

gradlePlugin {
    website = providers.gradleProperty("POM_URL")
    vcsUrl = providers.gradleProperty("POM_URL")

    plugins.create("jmh") {
        id = "me.champeau.jmh"
        implementationClass = "me.champeau.jmh.JMHPlugin"
        displayName = providers.gradleProperty("POM_NAME").get()
        description = providers.gradleProperty("POM_DESCRIPTION").get()
        tags = listOf("jmh")
    }
}

fun systemProp(name: String): String? = providers.systemProperty(name).orNull