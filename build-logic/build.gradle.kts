plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.nexfusion.build"
version = providers.gradleProperty("nexfusionBuildLogicVersion").orElse("1.0.0").get()

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    // The Android Gradle Plugin brings the Kotlin Gradle plugin at runtime.
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.2.10")
}

publishing {
    repositories {
        maven {
            name = "release"
            url = uri(rootProject.layout.projectDirectory.dir("../build/maven"))
        }
    }
}
