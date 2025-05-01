plugins {
    kotlin("jvm") version "2.1.10"
    id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "net.sebyte"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli-jvm:0.3.6")
    implementation("me.tongfei:progressbar:0.10.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(23)
}

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(23))
            })
            mainClass.set("net.sebyte.MainKt")
        }
    }
}
