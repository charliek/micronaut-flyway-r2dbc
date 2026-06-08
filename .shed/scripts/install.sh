#!/bin/bash
# One-time setup: install the JDK pinned by .sdkmanrc via SDKMAN, then warm the
# Gradle build. Integration tests use Testcontainers (Postgres 16), which needs
# the docker daemon from the `full` image.
set -euo pipefail
source "$(dirname "$0")/lib.sh"
cd "${SHED_WORKSPACE:-$(cd "$(dirname "$0")/../.." && pwd)}"

log "=== install: micronaut-flyway-r2dbc ==="

ensure_mise                 # baked into `full`; guard keeps the script portable
ensure_uv                   # used by site-docs (uv run mkdocs build)
wait_for_docker             # Testcontainers needs the daemon
ensure_sdkman

# Install the exact JDK from .sdkmanrc (java=<version>). Fall back to a Temurin
# 21 build — still via SDKMAN — if the pinned GraalVM build is unavailable for
# this architecture. The test suite needs only Java 21, not native-image.
#
# SDKMAN drives `sdk` through sourced scripts that assume `nounset` is OFF, so
# disable -u around every sdk invocation (re-enabled afterward).
set +u
java_ver="$(awk -F= '/^java=/{gsub(/[[:space:]]/,"",$2); print $2}' .sdkmanrc 2>/dev/null)"
java_ver="${java_ver:-21.0.9-graal}"
if sdk install java "$java_ver" </dev/null; then
  sdk default java "$java_ver" </dev/null || true
  log "java pinned to $java_ver"
else
  fallback="21.0.5-tem"
  log "WARN: '$java_ver' unavailable; installing fallback '$fallback' via SDKMAN"
  sdk install java "$fallback" </dev/null || true
  sdk default java "$fallback" </dev/null || true
fi
set -u
java -version

# Expose the SDKMAN default JDK to every login shell (shed exec/console/hooks).
# /etc/profile.d is sourced by login shells, unlike SDKMAN's ~/.bashrc snippet
# which is skipped for non-interactive shells (e.g. `shed exec ... ./gradlew`).
sudo tee /etc/profile.d/zz-sdkman-java.sh >/dev/null <<'PROFILE'
if [ -d "$HOME/.sdkman/candidates/java/current/bin" ]; then
  export JAVA_HOME="$HOME/.sdkman/candidates/java/current"
  export PATH="$JAVA_HOME/bin:$PATH"
fi
PROFILE

# Make the Testcontainers Ryuk setting available to all test sessions.
persist_session_env micronaut TESTCONTAINERS_RYUK_DISABLED=true

# Pre-pull the Testcontainers image (slow on first use under the VZ vfs driver).
docker pull postgres:16-alpine >/dev/null 2>&1 || log "WARN: could not pre-pull postgres:16-alpine"

# Warm the Gradle wrapper + dependency cache.
./gradlew --no-daemon help >/dev/null

log "=== install complete: ./gradlew build is ready ==="
