# Stable Android releases and in-app updates

## Permanent signing identity

The first tag-triggered release workflow automatically generates the permanent RSA-4096 APK signing identity. No Termux commands, local JDK, or manual GitHub Actions secret setup are required.

**Security trade-off:** because this is a personal project and the signing setup must be hands-off, the PKCS#12 keystore is stored as an asset in a private GitHub Release named `mobile-harness-signing-v1`. Its passphrase is present in the private workflow source, so repository access—not the keystore password—is the security boundary. The release workflow refuses to run unless the repository is private. Anyone with read access to this private repository can retrieve the signing identity and could sign a malicious APK. Only grant repository access to people trusted with the ability to publish app updates. For stronger isolation, use a locally-held keystore and GitHub Actions Secrets instead.

The certificate SHA-256 fingerprint is pinned in `release-signing-cert.sha256`. The workflow verifies the key against that pin on every release and fails closed if the private signing asset is missing or changed. **Never delete or replace the signing release or fingerprint.** Losing the signing identity permanently prevents future in-place updates.

## Release process

1. Merge reviewed code to `main`.
2. Increment both `versionCode` and `versionName` in `app/build.gradle.kts`.
3. Create a matching tag, for example `v1.0.11`.
4. Push the tag. `.github/workflows/release-android.yml` runs tests, lint, bootstraps/restores the permanent signing identity, builds the signed online APK, verifies its package/version/signature, generates the SHA-256 manifest and publishes a GitHub Release.
5. The app reads `app-update.json` through the private repository Contents API using the GitHub credential stored in Android Keystore.

The release workflow is tag-only and fails closed if the repository is not private, the signing identity is missing or mismatched, the tag does not match Gradle's version, or APK verification fails.

## Private update authentication

Connect GitHub once in Mobile Harness using the existing device sign-in flow; the signed-in account must have read access to this repository. The app copies the GitHub CLI token into a separate Android Keystore-encrypted store for update requests. It does not ask for GitHub sign-in on every update. FreeLLMAPI/provider authentication remains separate and can be completed manually. Disconnecting GitHub clears the updater credential; revoking the token requires reconnecting.

## Update behavior

- Updates are checked only when the user taps **Settings → Check for updates**. No background polling or update notifications are scheduled.
- When an update is available, the app opens the review/install prompt.
- Downloads require HTTPS, an exact declared size, SHA-256, matching package/version and the installed signing certificate.
- Installation is handed to Android PackageInstaller. Android/OEM policy can still show a system confirmation; ordinary apps cannot guarantee silent installation.
- Failed downloads are discarded. The existing installed app is never uninstalled as part of updating.

## CI builds and debug package

The normal CI workflow runs unit tests, release lint, and both debug/release APK compilation. The debug package ID remains `com.jarves.mh.custom`; the stable release package ID is `com.jarves.mh`.

## One-time transition from the current debug installation

The current CI debug APK uses the `.custom` package suffix. Android cannot convert it into the stable release package through an update.

Before switching to the first stable release:

1. Open Mobile Harness → Settings → Backup & Restore.
2. Export a backup. Include credentials only in a password-protected backup and keep that password private.
3. Install the first signed stable release alongside the old `.custom` debug app. The package IDs differ, so Android can keep both installed.
4. Restore the backup into the stable app and confirm projects, settings and credentials.
5. After verifying the stable app, optionally remove the old debug app. If the GitHub connection did not restore, sign in once in the stable app; its updater token is encrypted with that app's Android Keystore and cannot be copied from the debug package. Later stable updates retain the same package ID, signing key, app data and GitHub session and install in place.
