group = "io.github.tukcps"
version = "0.9.3"

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)

    id("idea")
    id("maven-publish")
    signing
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    jvm {}

    val hostOs = System.getProperty("os.name")
    val procArch = System.getProperty("os.arch")
    val isWindows = hostOs.startsWith("Windows")
    val isLinux = hostOs.startsWith("Linux")
    val isMacOs = hostOs.startsWith("Mac")
    val isArm64 = procArch.startsWith("aarch64")
    val isX64 = procArch.startsWith("x86_64") || procArch.startsWith("x64") || procArch.startsWith("amd64")

    @Suppress("UNUSED_VARIABLE")
    val nativeTarget = when {
        isMacOs -> {
            when {
                isArm64 -> macosArm64("native"){ binaries { sharedLib { baseName = "aadd" } } }
                isX64 -> macosX64("native") { binaries { sharedLib { baseName = "aadd" } } }
                else -> throw GradleException("Mac Processor Architecture not supported")
            }
        }

        isLinux -> {
            when {
                isX64 -> linuxX64("native") { binaries { sharedLib { baseName = "aadd" } } }
                isArm64 -> linuxArm64("native") { binaries { sharedLib { baseName = "aadd" } } }
                else -> throw GradleException("Linux Processor Architecture not supported")
            }
        }

        isWindows -> mingwX64("native"){
            binaries{ sharedLib { baseName = "aadd" } }
        }

        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies { implementation(kotlin("test")) }
        }
        val jvmMain by getting
        val jvmTest by getting
        val nativeMain by getting { dependencies{} }
        val nativeTest by getting {
            dependencies { implementation(kotlin("test")) }
        }
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
    }
}

val javadocJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Javadoc JAR"
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaGeneratePublicationHtml"))
}

publishing {
    publications {
        create<MavenPublication>("aadd") {
            groupId = "io.github.tukcps"
            artifactId = "aadd"
            version = project.version.toString()

            artifact(tasks["jvmJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            pom{
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                // pom {
                    name.set(project.name)
                    description.set("Affine Arithmetic Decision Diagram Library for Kotlin Multiplatform")
                    url.set("https://github.com/tukcps/Multiplatform-AADD")
                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("https://opensource.org/licenses/Apache-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("heermann")
                            name.set("Hagen Heermann")
                            organization.set("University of Kaiserslautern-Landau")
                            organizationUrl.set("https://github.com/tukcps/")
                        }
                        developer {
                            id.set("kwasigroch")
                            name.set("Sören Kwasigroch")
                            organization.set("University of Kaiserslautern-Landau")
                            organizationUrl.set("https://github.com/tukcps/")
                        }
                        developer {
                            id.set("zivkovic")
                            name.set("Carna Zivkovic")
                            organization.set("University of Kaiserslautern-Landau")
                            organizationUrl.set("https://github.com/tukcps/")
                        }
                        developer {
                            id.set("grimm")
                            name.set("Christoph Grimm")
                            organization.set("University of Kaiserslautern-Landau")
                            organizationUrl.set("https://github.com/tukcps/")
                        }
                        developer {
                            id.set("ratzke")
                            name.set("Axel Ratzke")
                            organization.set("University of Kaiserslautern-Landau")
                            organizationUrl.set("https://github.com/tukcps/")
                        }
                        developer {
                            id.set("herzog")
                            name.set("Moritz Herzog")
                            organization.set("University of Kaiserslautern-Landau")
                            organizationUrl.set("https://github.com/tukcps/")
                        }
                    }
                    scm {
                        url.set("https://github.com/tukcps/Multiplatform-AADD.git")
                        connection.set("scm:git:git://github.com/tukcps/Multiplatform-AADD.git")
                        developerConnection.set("scm:git:git://github.com/tukcps/Multiplatform-AADD.git")
                    }
                    issueManagement {
                        url.set("https://github.com/tukcps/Multiplatform-AADD/issues")
                    }
                // }
            }
        }
    }
    repositories {
        maven("https://cpsgit.informatik.uni-kl.de/api/v4/projects/152/packages/maven") {
            name = "GitLab"
            credentials(HttpHeaderCredentials::class) {
                name = "Deploy-Token"
                value = System.getenv("DEPLOY_TOKEN")
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}

signing {
    val signingKey = "${System.getenv("SIGNING_KEY")}"
    val signingPassphrase = "${System.getenv("SIGNING_PASSPHRASE")}"

    useInMemoryPgpKeys(signingKey, signingPassphrase)
    sign(publishing.publications["aadd"])
}