plugins {
  kotlin("jvm") version "2.1.0"
  id("java")
  id("com.diffplug.spotless") version "7.0.0.BETA4"
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

repositories { mavenCentral() }

spotless {
  kotlinGradle {
    target("**/*.gradle.kts")
    ktfmt()
  }

  flexmark {
    target("**/*.md")
    flexmark()
  }

  gherkin {
    target("**/*.feature")
    gherkinUtils()
  }

  json {
    target("**/*.json")
    simple()
  }

  shell { shfmt("3.10.0") }

  yaml {
    target("**/*.yml")
    jackson()
  }
}
