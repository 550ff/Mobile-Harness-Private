#!/usr/bin/env bash
set -euo pipefail

REPO="550ff/Mobile-Harness-Private"
KEY_DIR="${HOME}/.config/mobile-harness/keys"
KEYSTORE="${KEY_DIR}/mobile-harness-upload.p12"
KEY_ALIAS="mobile-harness-upload"

command -v keytool >/dev/null 2>&1 || { echo "keytool is required (install JDK 17+)." >&2; exit 1; }
command -v gh >/dev/null 2>&1 || { echo "GitHub CLI (gh) is required." >&2; exit 1; }
gh auth status >/dev/null
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
  keytool -genkeypair -v \
    -keystore "$KEYSTORE" \
    -storetype PKCS12 \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 4096 \
    -validity 10000 \
    -dname "CN=Mobile Harness Release, O=550ff, C=IN" \
    -storepass:env STORE_PASSWORD \
    -keypass:env STORE_PASSWORD
  chmod 600 "$KEYSTORE"
fi
export STORE_PASSWORD
keytool -list -keystore "$KEYSTORE" -storetype PKCS12 -alias "$KEY_ALIAS" -storepass:env STORE_PASSWORD >/dev/null
base64 "$KEYSTORE" | tr -d '\n' | gh secret set MH_UPLOAD_KEYSTORE_BASE64 --repo "$REPO"
printf '%s' "$STORE_PASSWORD" | gh secret set MH_UPLOAD_STORE_PASSWORD --repo "$REPO"
printf '%s' "$KEY_ALIAS" | gh secret set MH_UPLOAD_KEY_ALIAS --repo "$REPO"
printf '%s' "$STORE_PASSWORD" | gh secret set MH_UPLOAD_KEY_PASSWORD --repo "$REPO"
unset STORE_PASSWORD

echo
echo "Signing secrets configured for $REPO."
echo "IMPORTANT: Back up this keystore securely in at least two private locations:"
echo "  $KEYSTORE"
echo "Losing this file permanently prevents future in-place APK updates."
