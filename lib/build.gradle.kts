import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  kotlin("jvm") version "2.1.0"
  id("com.diffplug.spotless") version "7.0.0.BETA4"
  id("com.gradle.cucumber.companion") version "1.3.0"
  id("java-library")
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
  // For Serializing Domain Events
  runtimeOnly("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.0-RC")

  testImplementation("io.kotest:kotest-assertions-core-jvm:6.0.0.M1")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  // Cucumber Support
  testImplementation(platform("io.cucumber:cucumber-bom:7.20.1"))
  testImplementation("io.cucumber:cucumber-junit-platform-engine")
  testImplementation("io.cucumber:cucumber-java")
  testImplementation("io.cucumber:cucumber-picocontainer")
  testImplementation("org.junit.platform:junit-platform-suite-engine")
}

tasks.withType<Test> { useJUnitPlatform() }

spotless {
  ratchetFrom("origin/main")

  kotlin { ktfmt() }

  kotlinGradle { ktfmt() }

  flexmark {
    target("**/*.md")
    flexmark()
  }

  gherkin {
    target("src/**/*.feature")
    gherkinUtils()
  }

  json {
    target("**/*.json")
    simple()
  }

  yaml {
    target("**/*.yml")
    jackson()
  }
}
