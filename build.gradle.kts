plugins {
  kotlin("jvm") version "2.1.0"
  id("com.diffplug.spotless") version "7.0.0.BETA4"
}

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
