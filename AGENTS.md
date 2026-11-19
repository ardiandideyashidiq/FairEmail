# FairEmail — agent notes

## What this is
Large, single-module Android email client. Package `eu.faircode.email`. Java only (no Kotlin, no Compose), View-based XML UI. Flat package under `app/src/main/java/eu/faircode/email` (`Activity*`, `Fragment*`, `Service*`, `Worker*`, `Adapter*`, `Entity*`, `Dao*`, `Helper*`, `*Ex`/`Fixed*` subclasses). Manifest application class is flavor-specific `.ApplicationSecure` (in `src/<flavor>/java`) which extends the shared `ApplicationEx` in `src/main/java` — start there when tracing app init.

## Toolchain (verified from config)
- Wrapper `./gradlew` → Gradle 9.7.1; AGP 9.4.1; JDK toolchain 21.
- `compileSdk`/`targetSdk` 37, `minSdk` 23. NDK pinned to **27.3.13750724 (r27d)**, CMake **3.22.1** — native SQLite via `app/CMakeLists.txt` (`app/src/main/jni`), so a working NDK/CMake is required (Gradle downloads the pinned NDK if absent).
- Repos are declared only in `settings.gradle` (`google`, `mavenCentral`, `jitpack`) with `FAIL_ON_PROJECT_REPOS` — don't add repositories to module `build.gradle` files.
- Gradle configuration cache is on in `gradle.properties`, but CI disables it; see below.

## Modules and flavors
- Modules in `settings.gradle`: `:app` (the app), `:colorpicker` and `:openpgp-api` (vendored library modules, old `compileSdk 34`; source is checked in locally, don't "fix" their SDK versions in passing).
- Flavors: `github` (default), `large`, `fdroid`, `play`, `amazon`; build types `debug`/`release`.
- `androidComponents.beforeVariants` disables **debug for all flavors except `github`**. So local debug builds/installs are `githubDebug` (e.g. `./gradlew :app:assembleGithubDebug`). `./gradlew installDebug` will not do what you expect.
- Flavor-specific Java lives in `src/<flavor>/java` (+ `src/extra/java` for github/large/fdroid/amazon; `src/dummy/java` for play). Shared code is `src/main/java`. Adding a class used by all flavors → put it in `src/main/java`.
- Output APKs: `app/build/outputs/apk/<flavor>/<type>/FairEmail-v1.<versionCode><revision>-<flavor>-<type>.apk`.

## Commands
- Verify (what CI runs): `./gradlew lintGithubRelease`, then `./gradlew assembleGithubRelease assembleLargeRelease assemblePlayRelease`.
- CI flags for reproducible builds (use when reproducing CI): `--no-daemon --max-workers=1 --no-build-cache --no-configuration-cache` plus `JAVA_TOOL_OPTIONS="-XX:ActiveProcessorCount=1 -XX:+UnlockExperimentalVMOptions -XX:hashCode=3"`.
- **There are no unit or instrumented tests** in this repo (`app/src/test` / `androidTest` absent). Lint + assemble are the verification.
- Lint: `abortOnError=true`, `checkReleaseBuilds=false`, `checkOnly` the three `StringFormat*` rules, `MissingTranslation` disabled — so don't expect broad lint coverage.

## Generated/copied files — edit the source, not the copy
- `preBuild` copies root `CHANGELOG.md`, `SETUP.md`, `ATTRIBUTION.md` into `app/src/main/assets/` and writes `metadata/en-US/changelogs/<versionCode>.txt` from `CHANGELOG.md`. Edit the root files; the asset copies are generated.
- Room schemas are generated to `app/schemas` (`room.schemaLocation`).
- `app/src/main/resExtra` is added to main `res` sources; don't assume `res/` is the only resource dir.
- Asset refresh tasks exist: `downloadPSL`, `downloadAdguard`, `downloadBrave*`, `downloadSmime`, `downloadCrowdin`. Doc generation `updateFAQ`/`updatePrivacy` requires `pandoc`.

## Versioning / release conventions
- Version is hardcoded in `app/build.gradle`: `getVersionCode()` (currently `2341`), `getRevision()` (`"a"`), `getReleaseName()` (a dinosaur genus, e.g. `"Elopteryx"`). `versionName = "1." + versionCode`. Update these together when cutting a release.
- Release signing reads root `keystore.properties` (optional; unsigned if absent). Debug-only `BuildConfig` values come from root `local.properties` keys `paypal.uri`, `gpa.uri`, `info.uri`, `dev.domain` (empty if absent); `upload` task also uses `bb.pwd`. Neither properties file is checked in.
- `./gradlew upload -Ptarget=<flavor>[-suffix]` publishes to the BitBucket test downloads repo (see task for URL/format).
- Dependency patches live in `patches/`; several versions are force-pinned in `app/build.gradle` `resolutionStrategy` (Room 2.4.3, lifecycle 2.10.0, POI 3.17, outlook-message-parser 1.7.13, flatbuffers 2.0.0). POI and outlook-parser are marked "Do not update" — check `patches/` before bumping any of these.

## Contribution / process
- README requires **prior contact** before submitting pull requests (abuse prevention); PR template asks you to confirm reading contributing/support and agreeing to GPL-3 + copyright.
- Sensitive build secrets are encrypted in CI (`.github/workflows/android.yml`) via a `workflow_dispatch` password — never commit real `keystore.properties` / `local.properties`.
