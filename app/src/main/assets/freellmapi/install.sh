#!/usr/bin/env bash
set -Eeuo pipefail

UPSTREAM="https://github.com/tashfeenahmed/FreeLLMAPI.git"
PINNED_COMMIT="ca0a2741bad71fe6ca8b4f0e09ae6b918937d2fe"
ROOT="/workspace/freellmapi"
STAGE="/workspace/.freellmapi-installing"
if [ ! -d "$ROOT" ] && [ -d /workspace/freellmapi.previous ]; then
    mv /workspace/freellmapi.previous "$ROOT"
fi

# Avoid repeating a multi-minute source build when the exact pinned version
# is already complete. A missing marker or output file still triggers repair.
if [ -f "$ROOT/.mobile-harness-version" ] &&
   [ "$(cat "$ROOT/.mobile-harness-version")" = "$PINNED_COMMIT" ] &&
   [ -s "$ROOT/server/dist/index.js" ] &&
   [ -s "$ROOT/client/dist/index.html" ]; then
    echo "[MH] The pinned FreeLLMAPI build is already installed; skipping rebuild"
    exit 0
fi

retry() {
    local attempt=1
    local max_attempts=3
    until "$@"; do
        if [ "$attempt" -ge "$max_attempts" ]; then
            echo "[MH] Command failed after $attempt attempts: $*" >&2
            return 1
        fi
        echo "[MH] Network/dependency step failed; retrying ($((attempt + 1))/$max_attempts)…"
        sleep $((attempt * 2))
        attempt=$((attempt + 1))
    done
}

echo "[MH] Checking Node.js and built-in SQLite"
node -e "if (Number(process.versions.node.split('.')[0]) < 22) process.exit(1); require('node:sqlite').DatabaseSync"
echo "[MH] Downloading pinned FreeLLMAPI source"
rm -rf "$STAGE"
retry git clone --filter=blob:none --no-checkout "$UPSTREAM" "$STAGE"
retry git -C "$STAGE" fetch --depth=1 origin "$PINNED_COMMIT"
git -C "$STAGE" checkout --detach FETCH_HEAD

echo "[MH] Applying PRoot SQLite compatibility patch"
cd "$STAGE"
node <<'NODE'
const fs = require('node:fs');

const dbPath = 'server/src/db/index.ts';
const source = fs.readFileSync(dbPath, 'utf8');
const upstream = "return platform === 'android' ? nodeSqliteFactory : betterSqliteFactory;";
const patched = "return process.env.FREEAPI_USE_NODE_SQLITE === '1' || platform === 'android' ? nodeSqliteFactory : betterSqliteFactory;";
if (!source.includes(patched)) {
    if (!source.includes(upstream)) {
        throw new Error('FreeLLMAPI SQLite selector changed; refusing an unsafe compatibility patch.');
    }
    fs.writeFileSync(dbPath, source.replace(upstream, patched));
}

const packagePath = 'server/package.json';
const pkg = JSON.parse(fs.readFileSync(packagePath, 'utf8'));
if (pkg.optionalDependencies) delete pkg.optionalDependencies['better-sqlite3'];
fs.writeFileSync(packagePath, JSON.stringify(pkg, null, 2) + String.fromCharCode(10));
NODE

echo "[MH] Installing ARM64 production dependencies"
retry npm install --no-audit --no-fund --no-progress
echo "[MH] Building FreeLLMAPI server, CLI and dashboard"
NODE_OPTIONS=--max-old-space-size=1536 npm run build
test -s server/dist/index.js
test -s client/dist/index.html
printf '%s\n' "$PINNED_COMMIT" > .mobile-harness-version

echo "[MH] Cleaning package cache and source metadata"
rm -rf .git
npm cache clean --force >/dev/null 2>&1 || true
cd /workspace
rm -rf freellmapi.previous
if [ -d "$ROOT" ]; then mv "$ROOT" freellmapi.previous; fi
if mv "$STAGE" "$ROOT"; then
    rm -rf freellmapi.previous
else
    if [ -d freellmapi.previous ]; then mv freellmapi.previous "$ROOT"; fi
    exit 1
fi
echo "[MH] FreeLLMAPI installation complete"
