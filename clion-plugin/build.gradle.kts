plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.cph"
version = "1.0.0"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Using IntelliJ Community (IC) as base platform for broader compatibility
// The plugin works with any JetBrains IDE that supports the platform dependencies
intellij {
    version.set("2024.1")
    type.set("IC") // IntelliJ Community - provides base platform without CLion-specific dependencies
    updateSinceUntilBuild.set(false)
    downloadSources.set(false)
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        // sinceBuild 241 corresponds to IntelliJ Platform 2024.1
        sinceBuild.set("241")
        // untilBuild is intentionally omitted to support all future IDE versions.
        // The plugin uses stable platform APIs (com.intellij.modules.platform, com.intellij.modules.lang).
        // Note: Future IDE updates may require testing to ensure compatibility.
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    
    buildSearchableOptions {
        enabled = false
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
}
