rootProject.name = "simple-event-store"

plugins { id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.20" }

gitHooks {
  preCommit {
    from {
      """
              ./gradlew spotlessApply && git add --update
          """
          .trimIndent()
    }
  }

  commitMsg { conventionalCommits { defaultTypes() } }
  createHooks()
}
