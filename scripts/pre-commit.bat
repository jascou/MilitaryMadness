@echo off
REM Pre-commit hook for Windows: run tests and checks before committing
SETLOCAL
IF EXIST gradlew.bat (
  call gradlew.bat clean test check
) ELSE (
  echo Gradle wrapper not found. Please run tests manually.
  exit /b 1
)
ENDLOCAL
