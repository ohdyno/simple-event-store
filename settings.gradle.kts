rootProject.name = "simple-event-store"

plugins { id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.17" }

include("lib")

gitHooks {
  preCommit {
    from {
      """
              ./gradlew spotlessApply
              git add --update
          """
          .trimIndent()
    }
  }

  commitMsg { conventionalCommits { defaultTypes() } }
  createHooks()
}
