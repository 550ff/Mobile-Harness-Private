# Stable Android releases and in-app updates

## Signing identity (one time only)

The release workflow intentionally refuses to publish an APK until a permanent signing key is configured.

From a trusted machine, or Android Termux, with JDK 17+ and GitHub CLI authenticated to `550ff/Mobile-Harness-Private`, run:

```bash
bash scripts/setup-release-signing.sh
```

The script creates the key outside the repository at:

`~/.config/mobile-harness/keys/mobile-harness-upload.p12`

It uploads the keystore and passwords to GitHub Actions Secrets:

- `MH_UPLOAD_KEYSTORE_BASE64`
- `MH_UPLOAD_STORE_PASSWORD`
- `MH_UPLOAD_KEY_ALIAS`
- `MH_UPLOAD_KEY_PASSWORD`

Keep at least two secure backups of the keystore. Never commit it, upload it to a public issue, or paste its contents/password into chat. A lost or replaced signing key breaks in-place updates.

## Release process

1. Merge reviewed code to `main`.
2. Increment both `versionCode` and `versionName` in `app/build.gradle.kts`.
3. Create a matching tag, for example `v1.0.11`.
4. Push the tag. `.github/workflows/release-android.yml` runs tests, lint, builds the signed online APK, verifies its package/version/signature, generates the SHA-256 manifest and publishes a GitHub Release.
5. The app reads `app-update.json` through the private repository Contents API using the GitHub credential stored in Android Keystore.

The release workflow is tag-only and fails closed if signing secrets are missing, the tag does not match Gradle's version, or APK verification fails.

## Private update authentication

The in-app updater reads the manifest and APK from this private repository. Connect GitHub once in Mobile Harness using the existing device sign-in flow; the signed-in account must have read access to this repository. The app copies the GitHub CLI token into a separate Android Keystore-encrypted store for update requests. No personal access token or OAuth client secret is embedded in the APK. Disconnecting GitHub clears the updater credential; revoking the token requires reconnecting.

## CI builds and debug package

The normal CI workflow runs unit tests, release lint, and both debug/release APK compilation. It does not publish a debug APK artifact. Only the tag-triggered release workflow publishes an installable stable APK, and it refuses to proceed without the permanent signing secrets. The debug package ID remains `com.jarves.mh.custom`; the stable release package ID is `com.jarves.mh`.

## One-time transition from the current debug installation

The current CI debug APK uses the `.custom` package suffix. Android cannot convert it into the stable release package through an update.

Before switching to the first stable release:

1. Open Mobile Harness → Settings → Backup & Restore.
2. Export a backup. Include credentials only in a password-protected backup and keep that password private.
3. Install the first signed stable release alongside the old `.custom` debug app. The package IDs differ, so Android can keep both installed.
4. Restore the backup into the stable app and confirm projects, settings and credentials.
5. After verifying the stable app, optionally remove the old debug app. If the GitHub connection did not restore, sign in once in the stable app; its updater token is encrypted with that app's Android Keystore and cannot be copied from the debug package. Later stable updates retain the same package ID, signing key, app data and GitHub session and install in place.

## Runtime behavior

- Foreground checks happen when the app starts; checks are throttled to six hours.
- WorkManager performs a network-constrained background check approximately every 12 hours. Android may defer it due to battery/network policy.
- When notifications are permitted, a new stable version produces a notification that opens the in-app update prompt.
- Downloads require HTTPS, an exact declared size, SHA-256, matching package/version and the installed signing certificate.
- Installation is handed to Android PackageInstaller. Android/OEM policy can still show a system confirmation; ordinary apps cannot guarantee silent installation.
- Failed downloads are discarded. The existing installed app is never uninstalled as part of updating.
