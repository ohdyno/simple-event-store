plugins {
  id("org.jetbrains.kotlin.jvm") version "1.9.10"
  id("com.diffplug.spotless") version "6.22.0"
  id("java-library")
}

repositories { mavenCentral() }

dependencies {
  // For Serializing Domain Events
  runtimeOnly("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.6.0")

  testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.0")

  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter-engine")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  // Cucumber Support
  testImplementation(platform("io.cucumber:cucumber-bom:7.14.0"))
  testImplementation("org.junit.platform:junit-platform-suite")
  testImplementation("io.cucumber:cucumber-junit-platform-engine")
  testImplementation("io.cucumber:cucumber-java")
  testImplementation("io.cucumber:cucumber-picocontainer")
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

tasks.test {
  useJUnitPlatform()
  systemProperty("cucumber.junit-platform.naming-strategy", "long")
  outputs.upToDateWhen { false }
}

spotless {
  ratchetFrom("origin/main")
  kotlin { ktfmt() }
  kotlinGradle { ktfmt() }
  gherkin {
    target("src/**/*.feature")
    gherkinUtils()
  }
}
