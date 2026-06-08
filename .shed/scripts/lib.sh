#!/bin/bash
# Shared shed provisioning helpers, sourced by install/startup/shutdown hooks.
#
# Each ensure_* helper is idempotent: it installs a tool only when missing, so
# it is a no-op on the `full` image (which already ships mise + bun) while
# remaining portable to the `base`/`extensions` images.

log() { echo "[shed-provision $(date +%H:%M:%S)] $*"; }

# mise: present in `full`; the guard installs it on leaner images.
ensure_mise() {
  if command -v mise >/dev/null 2>&1; then
    log "mise present ($(mise --version 2>/dev/null))"
    return
  fi
  log "installing mise"
  curl -fsSL https://mise.run | sh
  export PATH="$HOME/.local/bin:$HOME/.local/share/mise/shims:$PATH"
}

# uv: not in any image — installs to ~/.local/bin (already on the login PATH).
ensure_uv() {
  if command -v uv >/dev/null 2>&1; then
    log "uv present ($(uv --version 2>/dev/null))"
    return
  fi
  log "installing uv"
  curl -LsSf https://astral.sh/uv/install.sh | sh
  export PATH="$HOME/.local/bin:$PATH"
}

# SDKMAN: install if absent, then source it so `sdk`/`java` work in this hook.
# The installer also appends its init to ~/.bashrc, so login shells started by
# `shed exec`/`shed console` pick up the global default JDK automatically.
ensure_sdkman() {
  if [ ! -s "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    log "installing sdkman"
    export SDKMAN_DIR="$HOME/.sdkman"
    curl -fsSL "https://get.sdkman.io" | bash
  else
    log "sdkman present"
  fi
  export sdkman_auto_answer=true
  export sdkman_selfupdate_feature=false
  set +u
  # shellcheck disable=SC1091
  source "$HOME/.sdkman/bin/sdkman-init.sh"
  set -u
}

# Wait for the docker daemon (auto-started in `full`) and confirm compose is
# available. Never hard-fails — docker compose is assumed present.
wait_for_docker() {
  local i
  for i in $(seq 1 30); do
    docker info >/dev/null 2>&1 && break
    sleep 1
  done
  if docker info >/dev/null 2>&1; then
    log "docker daemon ready"
  else
    log "WARN: docker daemon not ready (the 'full' image is required for Docker)"
  fi
  docker compose version >/dev/null 2>&1 || log "WARN: 'docker compose' unavailable (needs the 'full' image)"
}

# Persist static KEY=VALUE env into every exec/console session (and hook) via
# the shed agent's /etc/environment.d injection, which it reads per-exec.
# Usage: persist_session_env <name> KEY=VALUE [KEY=VALUE ...]
persist_session_env() {
  local name="$1"
  shift
  sudo mkdir -p /etc/environment.d
  printf '%s\n' "$@" | sudo tee "/etc/environment.d/90-${name}.conf" >/dev/null
  log "persisted session env -> /etc/environment.d/90-${name}.conf"
}
