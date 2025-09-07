#!/usr/bin/env bash
# Pre-commit hook for Unix-like systems: run tests and checks before committing
set -euo pipefail
if [[ -f ./gradlew ]]; then
  ./gradlew clean test check
else
  echo "Gradle wrapper not found. Please run tests manually." >&2
  exit 1
fi
