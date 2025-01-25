import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  kotlin("jvm") version "2.1.0"
  kotlin("plugin.serialization") version "2.1.0"
  id("java-library")
  id("maven-publish")
  id("com.diffplug.spotless") version "7.0.0.BETA4"
  id("com.github.jmongard.git-semver-plugin") version "0.14.0"
}

group = "me.xingzhou"

version = semver.version

publishing {
  publications {
    create<MavenPublication>("simpleEventStore") {
      artifactId = "simple-event-store"
      from(components["java"])
    }
  }

  repositories { mavenCentral() }
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
  implementation(kotlin("reflect"))
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
  implementation("org.springframework:spring-jdbc:6.2.1")

  testImplementation("org.postgresql:postgresql:42.7.4")
  testImplementation("com.zaxxer:HikariCP:6.2.1")

  testImplementation(platform("io.strikt:strikt-bom:0.35.1")) {
    testImplementation("io.strikt:strikt-core")
  }
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  // Cucumber Support
  testImplementation(platform("io.cucumber:cucumber-bom:7.20.1"))
  testImplementation("io.cucumber:cucumber-junit-platform-engine")
  testImplementation("io.cucumber:cucumber-java")
  testImplementation("io.cucumber:cucumber-picocontainer")
  testImplementation("org.junit.platform:junit-platform-suite")

  testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4")) {
    constraints {
      testImplementation("org.apache.commons:commons-compress:1.27.1") {
        because(
            """
        TestContainers 1.20.4 depends on a commons-compression with vulnerabilities which are fixed in 1.27.1.
        Issue tracked here: https://github.com/testcontainers/testcontainers-java/issues/8338"""
                .trimIndent())
      }
    }
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.slf4j:slf4j-simple:2.0.16") { because("remove Slf4j loggers warnings") }
  }
}

tasks.withType<Test> { useJUnitPlatform() }

spotless {
  kotlin { ktfmt() }
  sql {
    target("**/*.sql")
    dbeaver()
  }
}
