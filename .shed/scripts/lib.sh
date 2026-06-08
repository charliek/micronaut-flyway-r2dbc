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

# The `full`/`extensions` image ships docker with credsStore=shed, which
# brokers registry auth to the host. For public registries that the host does
# not allow-list, the helper returns an error instead of "no credentials",
# which aborts anonymous Docker Hub pulls (Testcontainers, docker compose).
# Neutralize it so public images pull anonymously. Idempotent; backs up the
# original. (If you need a private registry, configure credHelpers instead.)
enable_public_image_pulls() {
  local cfg="$HOME/.docker/config.json" tmp
  if [ -f "$cfg" ] && grep -q '"credsStore"[[:space:]]*:[[:space:]]*"shed"' "$cfg" 2>/dev/null; then
    log "removing docker credsStore=shed so public images pull anonymously"
    cp "$cfg" "$cfg.shed-bak" 2>/dev/null || true
    tmp="$(mktemp)"
    if jq 'del(.credsStore)' "$cfg" >"$tmp" 2>/dev/null; then
      mv "$tmp" "$cfg"        # keep any auths/credHelpers; drop only credsStore
    else
      rm -f "$tmp"; echo '{}' >"$cfg"   # jq unavailable: fall back to empty config
    fi
  fi
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

# The stock `full` image ships dockerd with `bridge: none`, which removes the
# default docker0 network. docker compose works (it creates its own user-defined
# network), but Testcontainers launches containers on the DEFAULT bridge — so
# without docker0 they get no IP / no published ports and the wait strategy
# times out. Re-enable the default bridge (the guest kernel supports it; a
# user-defined bridge already gets full networking). Idempotent: only edits the
# config + restarts docker when the bridge is genuinely missing.
enable_docker_default_bridge() {
  if docker network inspect bridge >/dev/null 2>&1; then
    log "docker default bridge present"
    return
  fi
  log "enabling docker default bridge (Testcontainers needs it) ..."
  local cfg=/etc/docker/daemon.json tmp
  if [ -f "$cfg" ]; then
    tmp="$(mktemp)"
    if jq 'del(.bridge) | del(.iptables)' "$cfg" >"$tmp" 2>/dev/null; then
      sudo cp "$cfg" "$cfg.shed-bak" 2>/dev/null || true
      sudo install -m 0644 "$tmp" "$cfg"
    fi
    rm -f "$tmp"
  fi
  sudo systemctl restart docker
  local i
  for i in $(seq 1 30); do
    docker info >/dev/null 2>&1 && break
    sleep 1
  done
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
