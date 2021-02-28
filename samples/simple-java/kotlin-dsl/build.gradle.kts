plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

jmh {
    warmupIterations.set(2)
    iterations.set(2)
    fork.set(2)
}
