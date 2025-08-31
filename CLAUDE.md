# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build System and Commands

This project uses Maven as the build system with Java 21.

### Common Development Commands

- `./mvnw clean compile` - Clean and compile the project
- `./mvnw test` - Run all tests (JUnit 5, Cucumber BDD tests, and ApprovalTests)
- `./mvnw spotless:apply` - Format code according to project conventions (Palantir Java format, Eclipse sorting, etc.)
- `./mvnw spotless:check` - Check code formatting without making changes
- `./mvnw clean verify` - Full build including compilation, tests, and code formatting checks

### Running Individual Tests

- `./mvnw test -Dtest=ClassName` - Run a specific test class
- `./mvnw test -pl domain/core` - Run tests in a specific module
- BDD feature files are located in `src/test/resources/features/`

### Maven Central Publishing

- `./mvnw clean verify -Ppublish` - Build with sources, javadoc, and GPG signing for publishing
- `./mvnw clean deploy -Ppublish` - Deploy to Maven Central (requires credentials in `~/.m2/settings.xml`)
- **Prerequisites**: Create account at https://central.sonatype.com/, generate user token, configure GPG keys

## Architecture Overview

This is a modular Event Store implementation using hexagonal architecture principles:

### Core Structure

- **Domain Layer**: Contains the core business logic
  - `domain/ports` - Interfaces and abstractions (Event, EventStorage, EventSerializer, etc.)
  - `domain/core` - Main implementation (EventStore, entities like Aggregate/Projection)
- **Adapters Layer**: Implementations of domain interfaces
  - `adapters/storage/` - Event storage implementations (in-memory, PostgreSQL)
  - `adapters/serializer/` - Event serialization (Jackson-based)
  - `adapters/converter/` - Event type conversion (map-backed, service-loader)
- **Starter Module**: Provides default configurations and builders

### Key Components

- **EventStore**: Main entry point for storing and retrieving events
- **Aggregate**: Event-sourced entities that maintain state through events
- **Projection**: Read models built from event streams
- **EventStorage**: Persistence layer abstraction with multiple implementations
- **EventSerializer**: Handles serialization/deserialization of events to/from JSON

### Event Sourcing Pattern

- Events are immutable and stored in append-only streams
- Aggregates are reconstructed by replaying events
- Projections are built by applying events to read models
- Supports optimistic concurrency control with version tracking

## Testing Framework

The project uses multiple testing approaches:
- **JUnit 5** for unit tests
- **Cucumber** for BDD integration tests with feature files
- **ApprovalTests** for regression testing (check `.approved.txt` files)
- **AssertJ** for fluent assertions

## Development Methodology

**CRITICAL: All development must follow strict Test-Driven Development (TDD)**
- Write tests BEFORE implementing any functionality
- Follow the Red-Green-Refactor cycle:
1. Write a failing test (Red)
2. Write minimal code to make it pass (Green)
3. Refactor while keeping tests green
- Never write production code without a corresponding test
- Use the existing testing frameworks: JUnit 5, Cucumber BDD, or ApprovalTests as appropriate

## Code Quality

- **Spotless** enforces code formatting with Palantir Java format
- **Git hooks** automatically format code on commit (`githooks/pre-commit`)
- **SLF4J** for logging throughout the application
- All formatting rules are defined in the parent `pom.xml`
- Always use conventional commit patterns when committing the changes in this repo.

