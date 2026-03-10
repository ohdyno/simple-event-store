# Simple Event Store

A simple Event Store in Java.

## Architecture

Hexagonal architecture diagrams for the runtime components, module dependencies, and core event flows live in [docs/hexagonal-architecture.md](docs/hexagonal-architecture.md).

Render the standalone Mermaid sources under [docs/mermaid](docs/mermaid) with [render.sh](docs/mermaid/render.sh):

```bash
./docs/mermaid/render.sh --diagram save-flow --stdout
```

Use `./docs/mermaid/render.sh --help` for more information. The script depends on `mise` being available on `PATH`.

## Todos

- Generate versions based on conventional commits: https://maven.basjes.nl/
- Generate changelog based on conventional commits: https://github.com/tomasbjerre/git-changelog-maven-plugin
- Integrate publishing plugin for maven central repository: https://central.sonatype.org/publish/publish-portal-maven/

