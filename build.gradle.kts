plugins {
    `java-library`
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    kotlin("jvm") version "1.8.0"
}

group = "org.auth"
version = "0.0.7"

repositories {
    mavenCentral()
    google()
}

apply {
    plugin("org.jlleitschuh.gradle.ktlint")
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

dependencies {
    implementation("commons-codec:commons-codec:1.15")
    implementation("de.taimos:totp:1.0")
    implementation("com.google.zxing:javase:3.4.1")

    testImplementation(kotlin("test"))
    testImplementation("org.assertj:assertj-core:3.6.1")
}

tasks.test {
    useJUnitPlatform()
    dependsOn(tasks.ktlintCheck)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    verbose.set(true)
    disabledRules.addAll("import-ordering", "no-wildcard-imports", "filename", "indent", "parameter-list-wrapping")
}

kotlin {
    jvmToolchain(11)
}
