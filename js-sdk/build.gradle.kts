plugins {
    kotlin("multiplatform") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
}

tasks.wrapper {
    gradleVersion = "8.14.3"
    distributionType = Wrapper.DistributionType.BIN
}

kotlin {
    js(IR) {
        compilerOptions {
            freeCompilerArgs.add("-Xir-minimized-member-names=false")
        }
        browser {
            webpackTask {
                mainOutputFileName = "ecomm-sdk.js"
            }
        }
        nodejs()
        binaries.executable()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("../shared/src")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
                implementation("io.ktor:ktor-client-core:3.1.1")
                implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
            }
        }

        jsMain {
            kotlin.srcDir("../shared/src@js")
            dependencies {
                implementation("io.ktor:ktor-client-js:3.1.1")
            }
        }
    }
}