# ADR-0003: Centralize Resource Loading and Image Caching

Date: 2025-09-04

Status: Accepted

## Context
Repeated disk IO (ImageIO.read) from rendering code caused stutters and non-deterministic test behavior.
We need a single point for resource IO, with classpath-first loading and placeholders on failure.

## Decision
Introduce `military.util.ResourceLoader` and `military.util.ImageCache`:
- ResourceLoader loads images/text with classpath-first strategy, falling back to filesystem, and returns
  placeholder images on failure to keep UI stable.
- ImageCache stores BufferedImages keyed by path, delegating actual loads via an `ImageLoader` interface; it
  supports overriding the loader in tests for determinism.

## Consequences
- Fewer image loads during painting; smoother rendering.
- Deterministic tests that don't touch disk/network.
- Centralized error handling with graceful fallbacks.

## Alternatives Considered
- Direct ImageIO calls in each view: rejected due to duplication and performance issues.
- Heavyweight asset pipeline: overkill for current scope.
