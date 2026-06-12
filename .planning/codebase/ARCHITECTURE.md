# Architecture

**Analysis Date:** 2026/06/12

## System Overview

```text
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│   Activities & Fragments (ViewBinding + ViewModels)         │
├──────────────────────────┬──────────────────────────────────┤
│   NavMainActivity        │   NavMainFragment                │
│   `com.taibao.app`       │   MapFragment                   │
│                          │   `com.taibao.app.map`           │
└──────────────────────────┴──────────────────────────────────┘
         │                  │                     │
         ▼                  ▼                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      ViewModel Layer                         │
│   Hilt-injected ViewModels with LiveData                    │
├──────────────────────────┬──────────────────────────────────┤
│   MainViewModel          │   MapViewModel                   │
│   `com.taibao.app`       │   `com.taibao.app.map`           │
└──────────────────────────┴──────────────────────────────────┘
         │                  │                     │
         ▼                  ▼                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      Repository Layer                        │
│   Data access abstraction with Room + MMKV                   │
├─────────────────────────────────────────────────────────────┤
│   MapRepository                                             │
│   `com.taibao.app.map`                                       │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
│   Room Database + MMKV + Baidu Maps SDK                     │
├──────────────────────────┬──────────────────────────────────┤
│   AppDatabase            │   LocationDao                    │
│   LocalLocationBean      │   MMKVUtils                      │
│   `tech.jour.template.   │   Baidu Map/Location SDK         │
│    common.room`          │                                  │
└──────────────────────────┴──────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│                      Service Layer                           │
│   WorkManager Foreground Service                            │
├─────────────────────────────────────────────────────────────┤
│   MockLocationWorker                                        │
│   `com.taibao.app.service`                                  │
└─────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| GoApplication | Hilt entry point, Baidu SDK initialization | `app/src/main/java/com/taibao/app/GoApplication.kt` |
| BaseApplication | Application lifecycle, ServiceLoader discovery | `app/src/main/java/tech/jour/template/base/BaseApplication.kt` |
| LoadModuleProxy | ServiceLoader-based module initialization | `app/src/main/java/tech/jour/template/base/app/LoadModuleProxy.kt` |
| CommonApplication | MMKV, Logger, Network monitoring setup | `app/src/main/java/tech/jour/template/common/CommonApplication.kt` |
| BaseActivity | ViewBinding setup, status bar, lifecycle hooks | `app/src/main/java/tech/jour/template/common/ui/BaseActivity.kt` |
| BaseFragment | ViewBinding setup, lifecycle hooks | `app/src/main/java/tech/jour/template/common/ui/BaseFragment.kt` |
| BaseViewModel | State management (loading/error/empty) | `app/src/main/java/tech/jour/template/base/mvvm/vm/BaseViewModel.kt` |
| MainViewModel | Worker management, location history | `app/src/main/java/com/taibao/app/MainViewModel.kt` |
| MapViewModel | Geocoding, POI search, search history | `app/src/main/java/com/taibao/app/map/MapViewModel.kt` |
| MapRepository | Room database access | `app/src/main/java/com/taibao/app/map/MapRepository.kt` |
| MockLocationWorker | Foreground service for mock GPS injection | `app/src/main/java/com/taibao/app/service/MockLocationWorker.kt` |
| NavMainActivity | Single-activity host with Navigation drawer | `app/src/main/java/com/taibao/app/NavMainActivity.kt` |

## Pattern Overview

**Overall:** MVVM with Repository Pattern

**Key Characteristics:**
- Single-Activity architecture with Navigation Component
- ViewBinding (not DataBinding) for view binding
- Hilt dependency injection with `@AndroidEntryPoint` and `@HiltViewModel`
- ServiceLoader pattern for modular application initialization
- Generic base classes with reflection-based ViewBinding inflation
- Coroutines and Flow for async operations
- WorkManager for foreground service

## Layers

**Presentation Layer:**
- Purpose: UI rendering, user interaction handling
- Location: `com.taibao.app`, `com.taibao.app.map`
- Contains: Activities, Fragments, ViewModels, Repository
- Depends on: Hilt, Navigation, ViewBinding, Baidu Maps SDK
- Used by: User interaction

**ViewModel Layer:**
- Purpose: Business logic, state management, UI-Data coordination
- Location: `com.taibao.app`, `com.taibao.app.map`
- Contains: ViewModels with LiveData/Flow
- Depends on: Repository, Hilt, WorkManager, Baidu Maps SDK
- Used by: Activities/Fragments

**Repository Layer:**
- Purpose: Data access abstraction, single source of truth
- Location: `com.taibao.app.map`
- Contains: Repository classes
- Depends on: Room, MMKV, Baidu Maps Geocoding API
- Used by: ViewModels

**Data Layer:**
- Purpose: Persistent storage, local database
- Location: `tech.jour.template.common.room`, `tech.jour.template.common.model.db`
- Contains: Room entities, DAOs, database singleton
- Depends on: Room, Hilt
- Used by: Repository

**Service Layer:**
- Purpose: Background mock location injection
- Location: `com.taibao.app.service`
- Contains: WorkManager CoroutineWorker
- Depends on: Android LocationManager, WorkManager
- Used by: ViewModels (via WorkManager enqueue)

## Data Flow

### Primary Request Path (Mock Location Injection)

1. User selects location on map (`MapFragment.kt:99` - map click listener)
2. MapViewModel performs geocoding (`MapViewModel.kt:66` - `getLocationByGeo()`)
3. Location saved to Room database (`MapViewModel.kt:47` - `mRepository.insert()`)
4. User navigates back to main fragment (`NavMainFragment.kt:56`)
5. User enables mock location switch (`NavMainFragment.kt:82`)
6. MainViewModel starts Worker (`MainViewModel.kt:46` - `startWorker()`)
7. MockLocationWorker registered with coordinates (`MainViewModel.kt:63-70`)
8. Worker runs as foreground service (`MockLocationWorker.kt:46` - `doWork()`)
9. Worker injects mock GPS/Network locations (`MockLocationWorker.kt:80-93`)

### Location History Flow

1. User views history list (`NavMainFragment` observes `getHistoryLocation()`)
2. MainViewModel queries Repository (`MainViewModel.kt:78` - `getHistoryLocation()`)
3. Repository returns Flow from DAO (`MapRepository.kt:16` - `getAll()`)
4. LocationDao queries Room database (`LocationDao.kt:14` - `@Query SELECT *`)
5. Flow emits updates to UI automatically

**State Management:**
- ViewModels expose `MutableLiveData` for one-time events
- Repository returns `Flow` for reactive data streams
- BaseViewModel provides `stateViewLD` for loading/error/empty states

## Key Abstractions

**FrameView Interface:**
- Purpose: Standardized View lifecycle hooks
- Examples: `BaseActivity`, `BaseFragment`
- Pattern: Template Method pattern with `initView()`, `initObserve()`, `initRequestData()`

**ApplicationLifecycle Interface:**
- Purpose: Modular initialization via ServiceLoader
- Examples: `CommonApplication`
- Pattern: ServiceLoader discovery with `@AutoService` annotation

**BaseDao Pattern:**
- Purpose: Generic CRUD operations for Room
- Examples: `LocationDao extends BaseDao<LocalLocationBean>`
- Pattern: Generic DAO with type parameter

## Entry Points

**Application Entry:**
- Location: `GoApplication.kt`
- Triggers: Android framework on app launch
- Responsibilities: Hilt initialization (via `@HiltAndroidApp`), Baidu Maps SDK setup

**Activity Entry:**
- Location: `NavMainActivity.kt`
- Triggers: Launcher activity from manifest
- Responsibilities: Navigation host setup, drawer configuration

**ServiceLoader Discovery:**
- Location: `BaseApplication.onCreate()` → `LoadModuleProxy.onCreate()`
- Triggers: Application startup
- Responsibilities: Discovers and initializes all `ApplicationLifecycle` implementations

## Architectural Constraints

- **Threading:** Single-threaded UI (main thread), coroutines with `Dispatchers.IO` for database/SDK operations
- **Global state:** `BaseApplication.context` and `BaseApplication.application` static singletons
- **Circular imports:** None detected
- **Coordinate system conversion:** Baidu SDK uses BD-09, Android mock location requires WGS84 (`MapUtils.bd2wgs()` conversion required)
- **WorkManager uniqueness:** Mock location worker uses `UNIQUE_WORK_NAME` to ensure single instance

## Anti-Patterns

### Reflection-based ViewBinding Inflation

**What happens:** `BaseFrameActivity` and `BaseFrameFragment` use `TUtil.getClazz()` to reflectively instantiate ViewBinding classes
**Why it's wrong:** Slower than direct inflation, lacks compile-time safety, harder to debug
**Do this instead:** Use direct ViewBinding inflation: `ActivityMainNavBinding.inflate(layoutInflater)`

**Files:** `app/src/main/java/tech/jour/template/base/mvvm/v/BaseFrameActivity.kt:42`, `app/src/main/java/tech/jour/template/base/mvvm/v/BaseFrameFragment.kt:41`

### Singleton Database Access

**What happens:** `AppDatabase.getInstance()` uses companion object singleton with `@Volatile` and `synchronized`
**Why it's wrong:** Redundant with Hilt `@Singleton` database provider in `DIDatabaseModule`
**Do this instead:** Rely solely on Hilt singleton scope, remove manual singleton pattern

**Files:** `app/src/main/java/tech/jour/template/common/room/AppDatabase.kt:30-36`

### Direct WorkManager Access in ViewModel

**What happens:** `MainViewModel` directly accesses `WorkManager.getInstance()` without dependency injection
**Why it's wrong:** Harder to test, couples ViewModel to WorkManager singleton
**Do this instead:** Inject `WorkManager` via Hilt `@Inject constructor`

**Files:** `app/src/main/java/com/taibao/app/MainViewModel.kt:28`

## Error Handling

**Strategy:** Exception swallowing with empty catch blocks

**Patterns:**
- `try-catch` with empty catch block in `MockLocationWorker` for location provider operations (`MockLocationWorker.kt:136-138`, `MockLocationWorker.kt:158`)
- `Result.failure()` return for invalid Worker input data (`MockLocationWorker.kt:51-53`)
- No global error handling beyond `BaseViewModel.stateViewLD`

## Cross-Cutting Concerns

**Logging:** Logger library initialized in `CommonApplication` with debug-only filter
**Validation:** Input validation in UI layer (e.g., `NavMainFragment.kt:91-94` checks empty coordinates)
**Authentication:** None - app does not require user authentication

---

*Architecture analysis: 2026/06/12*