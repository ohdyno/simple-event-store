# Hexagonal Architecture

This project is organized around a small application core (`domain/core`), explicit dependency contracts (`domain/ports`), and replaceable adapters under `adapters/*`. The `starter` module is a composition layer that wires a default runtime graph.

## Runtime Component View

Source: [runtime-component-view.mmd](mermaid/runtime-component-view.mmd)

## Module Dependency View

Source: [module-dependency-view.mmd](mermaid/module-dependency-view.mmd)

## Save Flow

Source: [save-flow.mmd](mermaid/save-flow.mmd)

## Enrich Flow

Source: [enrich-flow.mmd](mermaid/enrich-flow.mmd)

## Notes

- `EventStore` is the application-facing core entry point. The public use cases in the codebase are `save`, `enrich`, and `publisher`.
- `EventStorage`, `EventSerializer`, and `EventTypeConverter` are outbound dependencies. The core never depends on a concrete storage or serialization implementation.
- `EventTypeConverter` is shared across the core (`EventNamesExtractor`) and adapters (`JacksonEventSerializer`), so the composition layer is responsible for keeping those implementations consistent.
- The contract-test modules sit outside the runtime hexagon. They verify that each adapter obeys the port contract without introducing additional runtime dependencies into the core.
