# Stable Android releases and in-app updates

## Permanent signing identity

The signing keystore is held outside the public repository. The release workflow reads five repository Actions secrets: `MH_UPLOAD_KEYSTORE_BASE64`, `MH_UPLOAD_STORE_PASSWORD`, `MH_UPLOAD_KEY_ALIAS`, `MH_UPLOAD_KEY_PASSWORD`, and `MH_UPLOAD_CERT_SHA256`. The workflow verifies the keystore certificate fingerprint before signing and fails closed if any secret is missing or mismatched.

Never commit the keystore, its passwords, or a base64 copy to the repository. The previously exposed signing identity is compromised and must not be used for new releases. The new signing identity is permanent: keep a secure offline backup and do not replace it after publishing the first APK.

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

- When GitHub is connected, the app refreshes its saved credential and checks for a stable update on launch. The user can also trigger **Settings → Check for updates** at any time. No periodic background polling or update notifications are scheduled.
- When an update is available, the app opens the review/install prompt.
- Downloads require HTTPS, an exact declared size, SHA-256, matching package/version and the new permanent signing certificate.
- Installation is handed to Android PackageInstaller. Android may ask for install permission or a final confirmation; ordinary apps cannot silently bypass that system step.
- Updates use the same `com.jarves.mh` package ID and new permanent signing certificate. Android installs them over the existing app, preserving app data, projects and saved credentials. The app never uninstalls itself as part of updating.
- Failed downloads are discarded before installation. If GitHub access is revoked, reconnect once; normal updates do not require a new GitHub sign-in.

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


## One-time migration from v1.0.15

The new release uses a different signing certificate. Android cannot install it over an app signed by the exposed old certificate. Before installing v1.0.16, export a backup from Settings → Backup & Restore, verify the backup file exists and keep any backup password safe. Then uninstall the old stable app, install v1.0.16, restore the backup, and reconnect GitHub if necessary. Never delete the old app until the backup has been verified. All subsequent releases signed with the new key install in place.
