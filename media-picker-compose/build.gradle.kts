import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    `maven-publish`
}

group = "com.github.gargpadmakar"
version = "1.0.0"

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "MediaPickerCompose"
            isStatic = true
        }
    }

    android {
        namespace = "io.github.padmakargarg.mediapicker.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":media-picker"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("MediaPickerKMP Compose")
            description.set("Compose Multiplatform UI Components and State Management for MediaPickerKMP.")
            url.set("https://github.com/gargpadmakar/MediaPickerKMP")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("gargpadmakar")
                    name.set("Padmakar Garg")
                    url.set("https://github.com/gargpadmakar")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/gargpadmakar/MediaPickerKMP.git")
                developerConnection.set("scm:git:ssh://github.com/gargpadmakar/MediaPickerKMP.git")
                url.set("https://github.com/gargpadmakar/MediaPickerKMP")
            }
        }
    }
}
