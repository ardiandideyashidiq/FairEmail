# Repository Guidelines

## Project Overview

FairEmail is a privacy-oriented, fully-featured, offline-first email client for Android supporting IMAP, POP3, SMTP, S/MIME, OpenPGP, and Microsoft Graph. It is written in pure Java (Java 8+) with a flat package structure (`eu.faircode.email`) and licensed under GPLv3.

---

## Architecture & Data Flow

### Architectural Patterns
- **Flat Package Structure**: All main application classes live directly in `app/src/main/java/eu/faircode/email/` without nested domain/layer packages.
- **Single Source of Truth**: Room SQLite database (`DB.java`, schema v301) using Write-Ahead Logging (WAL) and Full-Text Search (FTS4/FTS5).
- **Separation of Metadata and Payloads**:
  - Metadata, headers, and state are stored in SQLite (`EntityMessage`, `EntityFolder`, `EntityOperation`).
  - Bodies and attachments reside on the internal filesystem:
    - Bodies: `context.getFilesDir()/messages/D{id/1000}/{id}` (`EntityMessage.getFile(context)`)
    - Attachments: `context.getFilesDir()/attachments/{id}` (`EntityAttachment.getFile(context)`)
- **Reactive UI**: Room DAOs expose `LiveData<...>` directly to Activities/Fragments. ViewModels are reserved for paging and state retention (`ViewModelMessages`, `ViewModelSelected`).
- **Paging**: Jetpack Paging 2/3 via `PagedList` and `LivePagedListBuilder`, driven by Room's `LimitOffsetDataSource` and `BoundaryCallbackMessages`.
- **Foreground Services for Networking**: Background synchronization and message dispatch run in long-lived foreground services (`ServiceSynchronize`, `ServiceSend`).

### Data Flow

```
[Remote Server]
       ▲
       │ IMAP IDLE / Poll / Fetch
       ▼
[ServiceSynchronize] ──► [Core.java] ──► [DaoMessage] (Room DB)
                                                │
                                                ▼ LiveData / PagedList
                                      [FragmentMessages]
                                                │
                                                ▼ Open / Scroll
                                      [AdapterMessage] ◄── Read from disk (getFile)
```

1. **Inbound Email**:
   - `ServiceSynchronize.java` monitors network changes and holds persistent IMAP IDLE connections per account.
   - Pushed events or periodic polls invoke `Core.onSynchronizeMessages()`, reconciling server UIDs against `DaoMessage.getUids()`.
   - New headers are evaluated against rules (`Core.runRules()`) and inserted into Room.
   - Message bodies are downloaded lazily on display or eagerly via queued `EntityOperation.BODY` commands handled by `Core.onBody()`.
2. **Outbound Email**:
   - `ActivityCompose.java` creates `EntityMessage` records in `EntityFolder.OUTBOX` and enqueues `EntityOperation.SEND`.
   - `ServiceSend.java` observes `db.operation().liveSend()`, processes the queue via a single-thread background executor under a wake lock, constructs standard `MimeMessage` objects (`MessageHelper.from()`), signs/encrypts (PGP/S-MIME), and dispatches via SMTP or Microsoft Graph.
   - On success, the message is uploaded/moved to the Sent folder (`EntityOperation.ADD`) and the Outbox record is cleared.
3. **Optimistic Updates**:
   - User actions (mark read, delete, archive, star) immediately update local SQLite flags (`ui_seen`, `ui_flagged`, `ui_hide`) and enqueue an `EntityOperation` for background synchronization.

---

## Key Directories

| Directory | Purpose |
|:---|:---|
| `app/src/main/java/eu/faircode/email/` | Core application source code (Activities, Fragments, DAOs, Entities, Services, Workers). |
| `app/src/main/java/javax/mail/` & `com/sun/mail/` | Vendored, customized JavaMail/Jakarta Mail protocol implementations (`patches/JavaMail.patch`). |
| `app/src/main/jni/` | Native C++ sources compiled into `libfairemail.so` (Compact Encoding Detection, CLD3, Protobuf Lite). |
| `app/src/main/res/` | Android UI layouts, menus, drawables, and strings. |
| `app/src/main/assets/` | Downloaded runtime assets (Brave/Adguard filter rules, Public Suffix List, root CAs, Markdown docs). |
| `app/src/github/`, `src/play/`, `src/fdroid/`, `src/large/`, `src/amazon/` | Flavor-specific source sets (in-app updater, Play Billing, F-Droid build flags, large heap). |
| `app/src/debug/` vs `app/src/release/` | Debug vs. release classes (e.g. `CoalMine.java` for LeakCanary integration vs. no-op). |
| `openpgp-api/` | Submodule for OpenPGP integration. |
| `metadata/` | Fastlane and F-Droid store listings, screenshots, and changelogs (`<versionCode>.txt`). |
| `patches/` | Historical diff patches applied to upstream libraries (Room, JavaMail, etc.). |

---

## Development Commands

### Building

```bash
# Clean project
./gradlew clean

# Build debug APK (githubDebug is the ONLY active debug variant)
./gradlew assembleGithubDebug

# Build release APKs
./gradlew assembleGithubRelease
./gradlew assembleLargeRelease
./gradlew assemblePlayRelease
./gradlew assembleFdroidRelease

# Build Google Play App Bundle (AAB)
./gradlew bundlePlayRelease
```

> **Variant Constraint**: `androidComponents.beforeVariants` in `app/build.gradle` disables the `debug` build type for `amazon`, `fdroid`, `large`, and `play`. Only `githubDebug` is enabled.

### Static Analysis & CI Checks

```bash
# Official CI lint verification (string format safety checks)
./gradlew --no-daemon --max-workers=1 --no-build-cache --no-configuration-cache lintGithubRelease

# Full lint run
./gradlew lint
```

### Pre-build & Asset Management

```bash
# Download Public Suffix List
./gradlew downloadPSL

# Download tracker and debounce rule lists
./gradlew downloadBraveClean downloadBraveDebounce downloadAdguard

# Download Mozilla Root CA certificates for S/MIME
./gradlew downloadSmime

# Sync translations from Crowdin CLI
./gradlew downloadCrowdin

# Recompile Markdown documentation to HTML using Pandoc
./gradlew updateFAQ updatePrivacy
```

---

## Code Conventions & Common Patterns

### Language & Structure
- **Pure Java**: Application logic in `eu.faircode.email` is 100% Java (Java 8+). Kotlin is used only in vendored Jetpack support libraries.
- **Single Flat Package**: Avoid creating sub-packages under `eu.faircode.email`.

### Naming Patterns
| Pattern | Examples | Role |
|:---|:---|:---|
| `Entity*` | `EntityAccount`, `EntityMessage`, `EntityFolder`, `EntityOperation` | Room `@Entity` data classes. |
| `Dao*` | `DaoAccount`, `DaoMessage`, `DaoOperation` | Room `@Dao` data access interfaces. |
| `Tuple*` | `TupleMessageEx`, `TupleFolderEx`, `TupleAccountView` | Room query projections and database views. |
| `Activity*` | `ActivityMain`, `ActivityView`, `ActivityCompose`, `ActivitySetup` | Screen containers. |
| `Fragment*` | `FragmentMessages`, `FragmentFolder`, `FragmentDialog*` | UI views and modal dialogs. |
| `Adapter*` | `AdapterMessage`, `AdapterFolder` | RecyclerView adapters (`PagedListAdapter`). |
| `Service*` | `ServiceSynchronize`, `ServiceSend`, `ServiceUI` | Android Services. |
| `Worker*` | `WorkerCleanup`, `WorkerFts`, `WorkerDailyRules` | WorkManager periodic jobs. |
| `*Ex` | `ApplicationEx`, `SimpleTask`, `ToastEx`, `RunnableEx`, `IMAPFolderEx` | Extensions and enhanced wrappers. |
| `ui_*` | `ui_seen`, `ui_flagged`, `ui_hide`, `ui_snoozed` | Optimistic UI fields on `EntityMessage`. |

### Dependency Injection
- **No DI Framework**: Do not introduce Dagger, Hilt, or Koin.
- Access shared components via thread-safe static singletons or context getters:
  - Database: `DB.getInstance(context)`
  - Application: `ApplicationEx.getInstance()`
  - Main thread handler: `ApplicationEx.getMainHandler()`

### Threading & Concurrency
- **No RxJava or Coroutines**: Concurrency is managed via Java standard executors and synchronization primitives (`Semaphore`, `CountDownLatch`).
- **Centralized Executors (`Helper.java`)**:
  - `Helper.getBackgroundExecutor(int threads, String name)`: Creates background `ThreadPoolExecutorEx`.
  - `Helper.getSerialExecutor()`, `Helper.getParallelExecutor()`, `Helper.getUIExecutor()`.
- **`SimpleTask<T>`**: Standard lifecycle-aware task runner for UI operations:
  ```java
  new SimpleTask<ResultType>() {
      @Override
      protected ResultType onExecute(Context context, Bundle args) throws Throwable {
          // Runs on background executor
          return doDatabaseOrNetworkWork();
      }

      @Override
      protected void onExecuted(Bundle args, ResultType result) {
          // Runs on main UI thread; delivery skipped if lifecycle is destroyed
          updateUi(result);
      }

      @Override
      protected void onException(Bundle args, Throwable ex) {
          Log.e(ex);
      }
  }.execute(this, new Bundle(), "task_id");
  ```
- **Service Lifecycle**: Background services utilize `TwoStateOwner` to observe Room `LiveData` without an Activity/Fragment context.

### Networking & HTTP
- **No OkHttp, Retrofit, or Volley**: HTTP/REST requests (e.g. update checks, Microsoft Graph API, OAuth) use `java.net.URL` and `javax.net.ssl.HttpsURLConnection`.

### Logging & Error Handling
- Use `Log.i()`, `Log.w()`, `Log.e()`, and `Log.breadcrumb()`.
- Critical diagnostic events are persisted to the database table `log` via `EntityLog`.
- Persistent errors are recorded directly on entities (`EntityOperation.error`, `EntityMessage.error`).

---

## Important Files

- `app/build.gradle`: Core build definitions, product flavors, asset download tasks, and dependencies.
- `app/src/main/AndroidManifest.xml`: Application manifest, permissions, service definitions, and intent filters.
- `app/src/main/java/eu/faircode/email/ApplicationEx.java`: `Application` subclass, global error handling, and component initialization.
- `app/src/main/java/eu/faircode/email/DB.java`: Central Room database definition, migrations, and instance provider.
- `app/src/main/java/eu/faircode/email/Core.java`: Core protocol, sync, rules engine, and message management (~6k LOC).
- `app/src/main/java/eu/faircode/email/ServiceSynchronize.java`: Persistent foreground service for IMAP synchronization and push (IDLE).
- `app/src/main/java/eu/faircode/email/ServiceSend.java`: Outbox monitoring and email sending foreground service.
- `app/src/main/java/eu/faircode/email/Helper.java`: Central utility class for concurrency executors, intent handlers, and formatters.
- `app/src/main/java/eu/faircode/email/ActivityMain.java`: Root navigation activity.
- `app/src/main/java/eu/faircode/email/ActivityView.java`: Message viewer and thread browser.
- `app/src/main/java/eu/faircode/email/ActivityCompose.java`: Message composer and editor.
- `.github/workflows/android.yml`: GitHub Actions workflow definition for linting and release builds.

---

## Runtime & Tooling Preferences

- **Java Development Kit**: **JDK 21** required (`JavaLanguageVersion.of(21)`).
- **Gradle Version**: **9.7.1** (via Gradle wrapper).
- **Android Gradle Plugin (AGP)**: **9.4.1**.
- **Android SDK**: `compileSdk 37`, `targetSdk 37`, `minSdk 23` (submodules set `minSdk 21`).
- **Android NDK**: **27.3.13750724 (r27d)** with **CMake 3.22.1**.
- **Memory & Daemon Settings**: CI and local builds run with `org.gradle.jvmargs=-Xmx3072m`. On low-memory environments, pass `--no-daemon --max-workers=1 --no-build-cache --no-configuration-cache` and `JAVA_TOOL_OPTIONS="-XX:ActiveProcessorCount=1"`.
- **Contribution Policy**:
  - The maintainer requires contact via `https://contact.faircode.eu/` **before** opening a Pull Request.
  - Contributions must be licensed under **GPLv3** with copyright assigned to Marcel Bokhorst.

---

## Testing & QA

- **Automated Test Suites**: The repository contains **no automated unit tests (`app/src/test`) or instrumentation tests (`app/src/androidTest`)**.
- **Static Quality Gate**:
  - CI enforces Android Lint checks via `./gradlew lintGithubRelease`.
  - Lint strictly fails on string formatting errors: `StringFormatInvalid`, `StringFormatMatches`, `StringFormatCount`.
- **Runtime Leak Detection**:
  - `com.squareup.leakcanary:leakcanary-android:2.14` is integrated in `debugImplementation`.
  - `CoalMine.java` monitors lifecycle leaks for `SimpleTask`, `TwoStateOwner`, and `RunnableEx` in debug builds.
- **Ad-Hoc Diagnostic Tests**:
  - In-code test routines run when `BuildConfig.DEBUG` is true:
    - Subnet matching: `ConnectionHelper.testInSubnet()`
    - URI/domain parsing: `UriHelper.test(context)` in `ApplicationEx.onCreate()`
    - DNS resolution: `DnsHelper.test(context)` in `ActivityView` update checks.
