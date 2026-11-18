#!/usr/bin/env bash
set -euo pipefail

REPO="550ff/Mobile-Harness-Private"
WORKDIR="${HOME}/Mobile-Harness-Private"

if [[ -z "${PREFIX:-}" || ! -d "${PREFIX:-/nonexistent}/etc/termux" ]]; then
  echo "Run this script inside the official Termux app." >&2
  exit 1
fi

echo "Installing the signing tools (JDK 17, GitHub CLI, Git)..."
pkg update
pkg install -y openjdk-17 gh git

command -v keytool >/dev/null
command -v gh >/dev/null
command -v git >/dev/null
java -version

if ! gh auth status -h github.com >/dev/null 2>&1; then
  echo
  echo "Sign in to GitHub. Choose HTTPS and complete the browser/device prompt."
  gh auth login -h github.com -p https -w
fi

gh auth status -h github.com
gh auth refresh -h github.com -s repo

if [[ -d "$WORKDIR/.git" ]]; then
  git -C "$WORKDIR" remote set-url origin "https://github.com/$REPO.git"
  git -C "$WORKDIR" fetch origin main
  git -C "$WORKDIR" checkout main
  git -C "$WORKDIR" pull --ff-only origin main
else
  gh repo clone "$REPO" "$WORKDIR"
fi

cd "$WORKDIR"
bash scripts/setup-release-signing.sh

echo
echo "Signing setup finished. Do not delete the keystore:"
echo "  $HOME/.config/mobile-harness/keys/mobile-harness-upload.p12"
echo "Back it up securely in two separate private locations."
