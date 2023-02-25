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
import java.util.Date
import java.text.SimpleDateFormat

plugins {
    `maven-publish`
    `java-gradle-plugin`
    signing
    id("com.gradle.plugin-publish")
}

val buildTimeAndDate by lazy {
    if ((version as String).endsWith("SNAPSHOT")) {
        Date(0)
    } else {
        Date()
    }
}
val buildDate by lazy {
    SimpleDateFormat("yyyy-MM-dd").format(buildTimeAndDate)
}
val buildTime by lazy {
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Gradle Plugin for JMH")
                description.set(properties.getting("project_description"))
                url.set(properties.getting("project_website"))
                issueManagement {
                    system.set("GitHub")
                    url.set(properties.getting("project_issues"))
                }
                scm {
                    url.set(properties.getting("project_website"))
                    connection.set(properties.getting("project_vcs").map { "scm:git:$it" })
                    developerConnection.set("scm:git:git@github.com:melix/jmh-gradle-plugin.git")
                }
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("melix")
                        name.set("Cédric Champeau")
                        organization {
                            name.set("Personal")
                            url.set("https://melix.github.io/blog")
                        }
                    }
                }
            }
        }
    }
}

signing {
    isRequired = gradle.taskGraph.allTasks.any {
        it.name.startsWith("publish")
    }
    publishing.publications.configureEach {
        sign(this)
    }
    sign(configurations.archives.get())
    useGpgCmd()
}

tasks.withType<Sign>().configureEach {
    notCompatibleWithConfigurationCache("https://docs.gradle.org/8.0.1/userguide/configuration_cache.html#config_cache:plugins")
    onlyIf { signing.isRequired }
}

gradlePlugin {
    website.set(properties.get("project_website").toString())
    vcsUrl.set(properties.get("project_vcs").toString())

    plugins.create("jmh") {
        id = "me.champeau.jmh"
        implementationClass = "me.champeau.jmh.JMHPlugin"
        displayName = properties.get("project_description").toString()
        description = properties.get("project_description").toString()
        tags.set(listOf("jmh"))
    }
}

tasks.publishPlugins {
    notCompatibleWithConfigurationCache("https://github.com/gradle/gradle/issues/21283")
}

fun systemProp(name: String) = project
    .providers
    .systemProperty(name)
    .orNull