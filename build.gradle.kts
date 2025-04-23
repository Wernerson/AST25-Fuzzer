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
