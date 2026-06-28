# Contributing to Hexis

Thanks for your interest in contributing to Hexis! 

## How to Contribute

1. **Fork & Branch:** Fork the repository and create a new branch from `dev` (e.g., `feat/add-new-feature` or `fix/resolve-issue-123`).
2. **Make Changes:** Implement your feature or bug fix.
3. **Submit a PR:** Open a Pull Request from your branch to our `dev` branch. Reference any related issues in your PR description.

## Code Guidelines

- **Style:** Run `./gradlew spotlessApply` to format your code before committing.
- **Compose:** 
  - Always expose a `modifier: Modifier = Modifier` parameter for your composables.
  - Write `@Preview` functions for significant components (keep them private in the same file).
- **Quality:** Write clear commit messages and test your changes.

## Project Structure

- `:androidApp` — Android-specific implementations, widgets, and app entry point.
- `:shared:ui` — Multiplatform UI (Compose), presentation logic, and shared components.
- `:shared:core` — Multiplatform core (Domain models, repositories, business logic).
- `:webDemo` — Web/Desktop demo (deployed via CI). 
  - *Tip for rapid UI testing:* run `./gradlew :webDemo:hotRunJvm --auto` for hot-reloading on Desktop.

I will review your PR as soon as possible. Thanks for helping make Hexis better!
