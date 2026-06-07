#!/bin/bash
# Nothing to stop: Testcontainers spins up ephemeral Postgres containers per
# test run and tears them down with the test JVM. Kept for lifecycle symmetry.
set -euo pipefail
echo "[shed-provision] shutdown: no long-running services for micronaut-flyway-r2dbc"
