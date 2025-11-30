plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.gitlab.arturbosch.detekt") version("1.23.8")
}

group = "jp.unaguna"
version = "0.1.0-SNAPSHOT"

sourceSets {
    main {
        output.dir(
            mapOf("builtBy" to "generateVersionProperties"),
            layout.buildDirectory.dir("generated/resources"),
        )
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
    testImplementation(kotlin("test"))
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}

tasks {
    shadowJar {
        manifest {
            attributes["Main-Class"] = "jp.unaguna.massgit.Main"
            archiveBaseName = archiveBaseName.get().removeSuffix("-j")
            archiveClassifier = ""
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    autoCorrect = true
}

tasks.register("generateVersionProperties") {
    val outputDir = file(layout.buildDirectory.dir("generated/resources"))
    inputs.property("version", version)
    outputs.dir(outputDir)

    doLast {
        val versionProperties = file("$outputDir/massgit-version.properties")
        versionProperties.parentFile.mkdirs()
        versionProperties.writeText("version=$version")
    }
}.also {
    tasks.processResources { dependsOn(it) }
}
