plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.graalvm.buildtools.native") version "0.11.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

group = "jp.unaguna"
version = "0.5.0"

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
    implementation("ch.qos.logback:logback-classic:1.5.23")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
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

graalvmNative {
    binaries.named("main") {
        imageName.set("massgit")
        mainClass.set("jp.unaguna.massgit.Main")
        buildArgs.add("--initialize-at-build-time")
        buildArgs.add("-O3")
        resources.includedPatterns.add("massgit-.+\\.properties")
        resources.includedPatterns.add("massgit-.+\\.json")
        resources.includedPatterns.add("logback.xml")
    }
}

tasks.register<Copy>("copyNativeExe") {
    val nativeCompile = tasks.named("nativeCompile")
    dependsOn(nativeCompile)

    from(nativeCompile.get().outputs)
    include("**/*.exe", "**/*")
    into(file("$rootDir/graalvm/release"))
}

tasks.named("nativeCompile") {
    finalizedBy("copyNativeExe")
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
