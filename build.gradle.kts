plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    antlr
    idea
}

val kotlinxSerializationVersion: String by project
val cliktVersion: String by project
val commonsTextVersion: String by project
val antlrVersion: String by project
val junitVersion: String by project
val mockkVersion: String by project

group = "v2.format"
version = "1.0"

repositories {
    mavenCentral()
}

val genDir = file("${project.buildDir}/generated-src/antlr/main/v2/format/antlr")

idea {
    module {
        generatedSourceDirs.add(genDir)
    }
}

sourceSets {
    main {
        java {
            srcDirs.add(genDir)
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")

    antlr("org.antlr:antlr4:$antlrVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileJava {
        options.encoding = "cp1252"
    }

    compileKotlin {
        dependsOn(generateGrammarSource)
        kotlinOptions.jvmTarget = "1.8"

    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    generateGrammarSource {
        arguments.addAll(arrayOf("-visitor", "-package", "v2.format.antlr"))
        outputDirectory = genDir
    }

    test {
        useJUnitPlatform()
    }

    jar {
        manifest {
            attributes["Main-Class"] = "v2.format.MainKt"
        }

        from(sourceSets.main.get().output)

        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })

        archiveFileName.set("v2format.jar")
    }
}