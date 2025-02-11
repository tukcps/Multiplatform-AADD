group = "com.github.tukcps"
version = "0.1.7"

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("idea")
    id("org.jetbrains.dokka") version "0.10.1"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "19"
            java.targetCompatibility = JavaVersion.VERSION_19
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    val hostOs = System.getProperty("os.name")
    val procArch = System.getProperty("os.arch")
    val isWindows = hostOs.startsWith("Windows")
    val isLinux = hostOs.startsWith("Linux")
    val isMacOs = hostOs.startsWith("Mac")
    val isArm64 = procArch.startsWith("aarch64")
    val isX64 = procArch.startsWith("x86_64") || procArch.startsWith("x64") || procArch.startsWith("amd64")

    val nativeTarget = when {
        isMacOs -> {
            when {
                isArm64 -> macosArm64("native"){
                    binaries {
                        sharedLib {
                            baseName = "native"
                        }
                    }
                }

                isX64 -> macosX64("native") {
                    binaries {
                        sharedLib {
                            baseName = "native"
                        }
                    }
                }

                else -> throw GradleException("Mac Processor Architecture not supported")
            }
        }

        isLinux -> {
            when {

                isArm64 -> linuxArm64("native") {
                    binaries {
                        sharedLib {
                            baseName = "native"
                        }
                    }
                }

                isX64 -> linuxX64("native") {
                    binaries {
                        sharedLib {
                            baseName = "native"
                        }
                    }
                }

                else -> throw GradleException("Linux Processor Architecture not supported")
            }
        }

        isWindows -> mingwX64("native"){
            binaries{
                sharedLib {
                    baseName = "libnative"
                }
            }
        }

        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies{
                implementation("org.jetbrains.kotlin:kotlin-bom")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("org.jetbrains.kotlin:kotlin-reflect")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val nativeMain by getting {
            dependencies{
                implementation("org.jetbrains.kotlin:kotlin-bom")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("org.jetbrains.kotlin:kotlin-reflect")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            }
        }
        val nativeTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/tukcps/Multiplatform-AADD")
            credentials {
                username = System.getenv("GITHUB_USER") ?: "enter-github-username"
                password = System.getenv("GITHUB_TOKEN") ?: "enter-github-token"
            }
        }
    }
    publications {
        create<MavenPublication>("gpr"){
            from(components["kotlin"])
            groupId = "com.github.tukcps"
            artifactId = "multiplatform-aadd"
            version = "0.1.7"
        }
    }
}