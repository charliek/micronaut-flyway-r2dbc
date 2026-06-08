#!/bin/bash
# Every-boot setup: make sure the docker daemon is up (for Testcontainers) and
# the pinned JDK is the active default. SDKMAN state persists in ~/.sdkman on
# the rootfs, so this just re-sources it.
set -euo pipefail
source "$(dirname "$0")/lib.sh"

log "=== startup: micronaut-flyway-r2dbc ==="
wait_for_docker
# Java is exposed to login shells via /etc/profile.d/zz-sdkman-java.sh (written
# by the install hook), so it is already on this hook's inherited PATH.
java -version 2>&1 | head -1 || log "WARN: java not on PATH yet"
log "=== startup complete ==="
