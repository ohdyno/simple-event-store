import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  kotlin("jvm") version "2.1.0"
  kotlin("plugin.serialization") version "2.1.0"
  id("java-library")
  id("com.diffplug.spotless") version "7.0.0.BETA4"
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
    apiVersion.set(KotlinVersion.KOTLIN_2_1)
    languageVersion.set(KotlinVersion.KOTLIN_2_1)
  }
}

configurations { compileOnly { extendsFrom(configurations.annotationProcessor.get()) } }

repositories { mavenCentral() }

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

  testImplementation("io.kotest:kotest-assertions-core-jvm:6.0.0.M1")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  // Cucumber Support
  testImplementation(platform("io.cucumber:cucumber-bom:7.20.1"))
  testImplementation("io.cucumber:cucumber-junit-platform-engine")
  testImplementation("io.cucumber:cucumber-java")
  testImplementation("io.cucumber:cucumber-picocontainer")
  testImplementation("org.junit.platform:junit-platform-suite")
}

tasks.withType<Test> { useJUnitPlatform() }

spotless { kotlin { ktfmt() } }
