#!/usr/bin/env bash
set -euo pipefail

REPO="550ff/Mobile-Harness-Private"
KEY_DIR="${HOME}/.config/mobile-harness/keys"
KEYSTORE="${KEY_DIR}/mobile-harness-upload.p12"
KEY_ALIAS="mobile-harness-upload"
CERT_PATH="release-signing-cert.sha256"

command -v keytool >/dev/null 2>&1 || { echo "keytool is required (install JDK 17+)." >&2; exit 1; }
command -v gh >/dev/null 2>&1 || { echo "GitHub CLI (gh) is required." >&2; exit 1; }
gh auth status -h github.com >/dev/null
mkdir -p "$KEY_DIR"
chmod 700 "$KEY_DIR"

read -rsp "Enter the keystore password (choose a new one only on first run): " STORE_PASSWORD
echo
if [ ! -e "$KEYSTORE" ]; then
  if [ "${#STORE_PASSWORD}" -lt 16 ]; then
    echo "Password must be at least 16 characters." >&2
    exit 1
  fi
  export STORE_PASSWORD
  keytool -genkeypair -v \\
    -keystore "$KEYSTORE" \\
    -storetype PKCS12 \\
    -alias "$KEY_ALIAS" \\
    -keyalg RSA \\
    -keysize 4096 \\
    -validity 10000 \\
    -dname "CN=Mobile Harness Release, O=550ff, C=IN" \\
    -storepass:env STORE_PASSWORD \\
    -keypass:env STORE_PASSWORD
  chmod 600 "$KEYSTORE"
fi
export STORE_PASSWORD
keytool -list -keystore "$KEYSTORE" -storetype PKCS12 -alias "$KEY_ALIAS" -storepass:env STORE_PASSWORD >/dev/null

# Pin the certificate before publishing secrets. A different key must never
# silently replace the identity used by already-installed stable applications.
CERT_FINGERPRINT="$(keytool -list -v -keystore "$KEYSTORE" -storetype PKCS12 -alias "$KEY_ALIAS" -storepass:env STORE_PASSWORD | awk -F': ' '/SHA256:/{gsub(":","",$2); print tolower($2); exit}')"
if [[ ! "$CERT_FINGERPRINT" =~ ^[a-f0-9]{64}$ ]]; then
  echo "Could not calculate a valid SHA-256 signing-certificate fingerprint." >&2
  exit 1
fi

EXISTING_SHA="$(gh api "repos/$REPO/contents/$CERT_PATH" --jq '.sha' 2>/dev/null || true)"
if [ -n "$EXISTING_SHA" ]; then
  PINNED_FINGERPRINT="$(gh api "repos/$REPO/contents/$CERT_PATH" --jq '.content | @base64d' | tr -d '[:space:]' | tr '[:upper:]' '[:lower:]')"
  if [ "$PINNED_FINGERPRINT" != "$CERT_FINGERPRINT" ]; then
    echo "ERROR: This keystore does not match the release certificate already pinned in $REPO." >&2
    echo "Refusing to replace the permanent signing identity or upload different secrets." >&2
    exit 1
  fi
  echo "Signing certificate matches the existing pinned release identity."
else
  CONTENT="$(printf '%s\\n' "$CERT_FINGERPRINT" | base64 | tr -d '\\n')"
  gh api --method PUT "repos/$REPO/contents/$CERT_PATH" \\
    -f message="chore(signing): pin permanent release certificate" \\
    -f content="$CONTENT" \\
    -f branch=main >/dev/null
  echo "Pinned the permanent signing-certificate fingerprint in $REPO/$CERT_PATH."
fi

# Only upload secrets after the fingerprint has been safely pinned/verified.
base64 "$KEYSTORE" | tr -d '\\n' | gh secret set MH_UPLOAD_KEYSTORE_BASE64 --repo "$REPO"
printf '%s' "$STORE_PASSWORD" | gh secret set MH_UPLOAD_STORE_PASSWORD --repo "$REPO"
printf '%s' "$KEY_ALIAS" | gh secret set MH_UPLOAD_KEY_ALIAS --repo "$REPO"
printf '%s' "$STORE_PASSWORD" | gh secret set MH_UPLOAD_KEY_PASSWORD --repo "$REPO"
unset STORE_PASSWORD

echo
echo "Signing secrets configured for $REPO."
echo "IMPORTANT: Back up this keystore securely in at least two private locations:"
echo "  $KEYSTORE"
echo "Losing this file permanently prevents future in-place APK updates."
