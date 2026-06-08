# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Taibao** (太宝) is an Android virtual/mock location app. It uses Baidu Maps SDK for map display and positioning, and injects mock GPS coordinates via Android's test location provider mechanism through a foreground service backed by WorkManager.

## Build & Run

```bash
# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease

# Clean build
./gradlew clean

# Install debug to device
./gradlew installDebug
```

## Key Technologies

| Technology | Purpose |
|------------|---------|
| **Baidu Maps SDK** (7.6.2) | Map display, geocoding, POI search, location (BD09LL coordinate system) |
| **Hilt** (2.51.1) | Dependency injection with `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel` |
| **Room** (2.6.1) | Local database for location history; uses `kapt` for annotation processing |
| **Navigation** (2.8.8) | Single-activity, multi-fragment navigation with `nav_graph.xml` |
| **WorkManager** (2.10.0) | Foreground service for mock location injection |
| **MMKV** (2.2.2) | Key-value storage (search history) |
| **ViewBinding** | View binding (not DataBinding) |
| **Gradle 8.7.2 / Kotlin 1.9.24 / AGP 8.7.2** | Build toolchain targeting JVM 17 |

## Architecture

### Package Structure (3 distinct layers)

```
com.taibao.app              # App-specific: Activities, Fragments, ViewModels, Repository
com.taibao.app.utils            # Baidu Maps utilities, coordinate conversion, LocationBean
com.taibao.app.service          # MockLocationWorker (foreground mock location service)
tech.jour.template.base     # Generic base framework: BaseApplication, BaseViewModel, utils, KTX extensions
tech.jour.template.common   # Shared app layer: BaseActivity/Fragment, Room DB, navigation, constants
tech.jour.template.module   # Template module (DActivity/DFragment/DViewModel — mostly unused scaffolding)
```

### Application Lifecycle (ServiceLoader pattern)

`GoApplication` (Hilt entry point) → `BaseApplication` → `LoadModuleProxy` uses `java.util.ServiceLoader` to discover `ApplicationLifecycle` implementations. `CommonApplication` is annotated with `@AutoService(ApplicationLifecycle::class)` and handles initialization of MMKV, Logger, network monitoring, and crash reporting.

### MVVM Pattern

- **BaseActivity/BaseFragment** (`tech.jour.template.common.ui`) — generic `<VB: ViewBinding, VM: BaseViewModel>` base classes with `initView()`, `initObserve()`, `initRequestData()` lifecycle hooks
- **BaseFrameActivity/BaseFrameFragment** (`tech.jour.template.base.mvvm.v`) — auto-inflates ViewBinding via reflection (`TUtil`), sets up network state monitoring
- **BaseViewModel** — provides `stateViewLD` for loading/error/empty state management
- **ViewModels** are Hilt-injected with `@HiltViewModel` + `@Inject constructor`

### Navigation

Single-Activity (`NavMainActivity`) with a `DrawerLayout` + `NavigationView`. Three destinations in `nav_graph.xml`:
1. `NavMainFragment` (start) — location history list, mock start/stop, camera
2. `MapFragment` — Baidu map with POI search, geocoding, and location picking
3. `NavDevFragment` — opens Android developer settings

### Mock Location Injection

`MockLocationWorker` (`CoroutineWorker`) registers test GPS and Network providers via `LocationManager.addTestProvider()`, then loops continuously (every 32ms) setting mock locations. Coordinates are converted from Baidu BD-09 to WGS84 via `MapUtils.bd2wgs()` before injection. The worker runs as a foreground service with a persistent notification.

### Room Database

`AppDatabase` — single-table: `LocalLocationBean` (timestamp PK, address, sematicDescription, latitude, longitude). Uses `fallbackToDestructiveMigration()`. Accessed via `LocationDao` which extends `BaseDao<LocalLocationBean>`.

## Coordinate Systems

- **Baidu Maps SDK** uses **BD-09** (BD09LL) coordinate system
- **Android mock location** requires **WGS84** coordinates
- `MapUtils.bd2wgs()` converts BD-09 → WGS84 before injection
- `MapUtils.wgs2bd09()` converts WGS84 → BD-09 for display

## Important Notes

- The Baidu Maps API key is embedded in `AndroidManifest.xml` — do not expose publicly
- The keystore file (`keystore/GoGoGo.jks`) and its passwords are in `app/build.gradle.kts` — do not use this signing config in production
- `GoApplication` is a Java file (not Kotlin) — Baidu SDK initialization must happen in `onCreate()` before any map operations
- Hilt uses `kapt` (not KSP) for annotation processing
- Room also uses `kapt` for its compiler
- `compileSdk = 35` but `targetSdk = 33` — this is intentional for Play Store compatibility