# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Clean
./gradlew clean
```

Build output APK: `yiliao-{versionName}-{buildType}.apk` in `app/build/outputs/apk/`.

Version and code are set in `gradle.properties`: `releaseVersion` / `releaseCode`.

## Architecture

This is an **Android medical device management system** (医疗设备管理系统) — a graduation project. It supports device archival, inspection, repair tracking, inventory, and statistics. An offline-first design queues local changes and syncs to a Spring Boot backend when online.

### Tech stack

- **Language**: Java 17
- **Build**: Gradle with AGP 8.12.0, `./gradlew`
- **Local DB**: Room (entities + DAOs, accessed via `MedicalApp` Application singleton)
- **HTTP**: Retrofit + Gson, base URL `http://10.0.2.2:8080/` (emulator → host loopback)
- **QR scanning**: ZXing embedded (`com.journeyapps:zxing-android-embedded`)
- **UI**: AppCompat + Material Components, plain XML layouts, programmatic view binding (no data binding/view binding lib)

### Data layer (`data/`, `db/`, `network/`, `sync/`)

- **Room entities** — `DeviceEntity`, `InspectionEntity`, `RepairOrderEntity`, `SyncTaskEntity`. Each has a matching `*Dao` interface with SQL queries.
- **AppDatabase** (`db/AppDatabase.java`) — Room database, version 3, uses `fallbackToDestructiveMigration()`. Created in `MedicalApp.onCreate()`.
- **SyncManager** (`sync/SyncManager.java`) — processes the `sync_task` table queue. Two task types: `IMAGE_UPLOAD` (multipart file upload) and generic tasks (JSON payload posted to `/api/sync/task`). Call `syncPending()` to drain the queue.
- **ApiClient** (`network/ApiClient.java`) — singleton Retrofit instance, exposes `SyncApiService`.
- **SyncApiService** (`network/SyncApiService.java`) — Retrofit interface: `POST /api/sync/task` and `POST /api/image/upload`.
- **DTOs** (`network/dto/`) — `SyncRequest`, `SyncResponse`, `ImageUploadResponse`.

### Activity flow & roles

1. **LoginActivity** → hardcoded credentials (admin/inspector/repair, password `123456`), passes `role` + `username` to MainActivity.
2. **MainActivity** — role-based dashboard. Three roles:
   - `ADMIN` — all 6 modules visible
   - `INSPECTOR` — DeviceList, Inspection, Statistics
   - `REPAIR` — DeviceList, Repair, Statistics
3. **Modules** (`module/`):
   - `DeviceArchiveActivity` — QR code scan (JSON or pipe-delimited) → Room insert
   - `DeviceListActivity` — search/filter devices
   - `DeviceDetailActivity` — view single device
   - `InspectionActivity` — mark NORMAL/ABNORMAL with optional camera/gallery photo. ABRNORMAL auto-creates a `RepairOrderEntity` + sync tasks
   - `RepairActivity` — manage repair orders
   - `InventoryActivity` — manage spare parts inventory
   - `StatisticsActivity` — read-only stats
   - `SyncLogActivity` — view sync task queue

### Offline-first pattern

Activities write to Room first, then insert `SyncTaskEntity` records (status `PENDING`). `SyncManager.syncPending()` iterates pending tasks, calls the backend API, and marks them `DONE` or `FAILED`.

### Utilities

- **`DeviceQrParser`** — parses QR content in 3 formats: JSON (`name/code/dept` with alias support), pipe-delimited (`名称|编号|科室`), or plain text (code-only).
- **`ImageCompressor`** — downsamples camera/gallery images to max 1024px width, JPEG quality 80, saves to `{externalFilesDir}/fault_images/`.

### Backend reference

`backend-reference/` contains reference Spring Boot code:
- `ImageController.java` — image upload/download/delete endpoints
- `database-schema.sql` — optional `fault_image` DDL

The Android app talks to the backend at `http://10.0.2.2:8080/` (`BASE_URL` in `ApiClient.java`).

### Key config

- `AndroidManifest.xml` — LoginActivity is the launcher; uses `FileProvider` for camera photo sharing; custom `network_security_config.xml` for cleartext HTTP to the backend.
- Room DB version 3, schema export disabled.
- Min SDK 26 (Android 8.0), target/compile SDK 34.
