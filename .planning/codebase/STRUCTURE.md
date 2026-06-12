# Codebase Structure

**Analysis Date:** 2026/06/12

## Directory Layout

```
taibao-android/
├── app/                            # Main application module (single-module project)
│   ├── src/main/
│   │   ├── java/
│   │   │   ├── com/taibao/app/     # App-specific implementation
│   │   │   │   ├── map/            # Map feature package
│   │   │   │   ├── service/        # Background services
│   │   │   │   ├── utils/          # App utilities
│   │   │   │   ├── GoApplication.kt
│   │   │   │   ├── NavMainActivity.kt
│   │   │   │   ├── MainViewModel.kt
│   │   │   │   └── ...
│   │   │   └── tech/jour/template/ # Reusable framework code
│   │   │       ├── app/            # Application layer
│   │   │       ├── base/           # Base classes and utilities
│   │   │       │   ├── app/        # Application lifecycle
│   │   │       │   ├── constant/   # Constants
│   │   │       │   ├── ktx/        # Kotlin extensions
│   │   │       │   ├── mvvm/       # MVVM base classes
│   │   │       │   └── utils/      # Utility classes
│   │   │       ├── common/         # Shared app components
│   │   │       │   ├── constant/   # App constants
│   │   │       │   ├── helper/     # Response handling
│   │   │       │   ├── model/      # Data models
│   │   │       │   ├── navigation/  # Navigation helpers
│   │   │       │   ├── room/       # Database setup
│   │   │       │   └── ui/         # Base UI classes
│   │   │       └── module/         # Template scaffolding (unused)
│   │   ├── res/
│   │   │   ├── navigation/         # Navigation graph
│   │   │   ├── layout/             # Layout XML files
│   │   │   └── ...
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts            # App-level build config
├── keystore/                       # Signing keystore (DO NOT USE IN PRODUCTION)
├── build.gradle.kts                # Project-level build config
├── settings.gradle.kts             # Project settings (single module: :app)
└── libs.versions.toml              # Version catalog
```

## Package Purposes

**`com.taibao.app`:**
- Purpose: App-specific business logic and UI
- Contains: Activities, Fragments, ViewModels, Repository, Worker
- Key files: `GoApplication.kt`, `NavMainActivity.kt`, `MainViewModel.kt`, `MockLocationWorker.kt`

**`com.taibao.app.map`:**
- Purpose: Map feature implementation
- Contains: MapFragment, MapViewModel, MapRepository
- Key files: `MapFragment.kt`, `MapViewModel.kt`, `MapRepository.kt`

**`com.taibao.app.service`:**
- Purpose: Background services
- Contains: WorkManager CoroutineWorker for mock location
- Key files: `MockLocationWorker.kt`

**`com.taibao.app.utils`:**
- Purpose: App-specific utilities
- Contains: Baidu Maps utilities, coordinate conversion, file handling
- Key files: `MapUtils.java`, `LocationUtil.kt`, `FileUriUtils.kt`

**`tech.jour.template.base`:**
- Purpose: Generic framework foundation
- Contains: BaseApplication, MVVM base classes, Kotlin extensions, utilities
- Key files: `BaseApplication.kt`, `BaseViewModel.kt`, `BaseFrameActivity.kt`

**`tech.jour.template.base.app`:**
- Purpose: Application lifecycle management
- Contains: ServiceLoader infrastructure
- Key files: `ApplicationLifecycle.kt`, `LoadModuleProxy.kt`

**`tech.jour.template.base.mvvm`:**
- Purpose: MVVM architecture base classes
- Contains: Generic Activity/Fragment/ViewModel bases
- Key files: `BaseFrameActivity.kt`, `BaseFrameFragment.kt`, `BaseViewModel.kt`

**`tech.jour.template.common`:**
- Purpose: Shared application layer
- Contains: Database, constants, base UI classes, models
- Key files: `CommonApplication.kt`, `BaseActivity.kt`, `BaseFragment.kt`, `AppDatabase.kt`

**`tech.jour.template.common.room`:**
- Purpose: Room database setup
- Contains: Database singleton, DAOs, type converters, Hilt module
- Key files: `AppDatabase.kt`, `LocationDao.kt`, `DIDatabaseModule.kt`

**`tech.jour.template.module`:**
- Purpose: Template scaffolding (unused in this app)
- Contains: Empty DActivity, DFragment, DViewModel, DRepository
- Note: Can be safely removed

## Key File Locations

**Entry Points:**
- `app/src/main/java/com/taibao/app/GoApplication.kt`: Hilt application entry
- `app/src/main/java/com/taibao/app/NavMainActivity.kt`: Launcher activity
- `app/src/main/java/com/taibao/app/WelcomeActivity.kt`: Splash/welcome screen
- `app/src/main/java/tech/jour/template/base/BaseApplication.kt`: Framework base application

**Configuration:**
- `app/build.gradle.kts`: App-level dependencies, build types, signing config
- `settings.gradle.kts`: Project structure (single module)
- `app/src/main/AndroidManifest.xml`: Permissions, activities, services

**Core Logic:**
- `app/src/main/java/com/taibao/app/MainViewModel.kt`: Main business logic
- `app/src/main/java/com/taibao/app/service/MockLocationWorker.kt`: Mock location injection
- `app/src/main/java/com/taibao/app/map/MapFragment.kt`: Map UI and interaction
- `app/src/main/java/com/taibao/app/map/MapRepository.kt`: Data access layer

**Navigation:**
- `app/src/main/res/navigation/nav_graph.xml`: Navigation destinations

**Database:**
- `app/src/main/java/tech/jour/template/common/room/AppDatabase.kt`: Room database
- `app/src/main/java/tech/jour/template/common/room/dao/LocationDao.kt`: Location DAO
- `app/src/main/java/tech/jour/template/common/model/db/LocalLocationBean.kt`: Location entity

**Testing:**
- `app/src/test/java/`: Unit tests (if exists)
- `app/src/androidTest/java/`: Instrumentation tests (if exists)

## Naming Conventions

**Files:**
- Activities: `*Activity.kt` (e.g., `NavMainActivity.kt`)
- Fragments: `*Fragment.kt` (e.g., `MapFragment.kt`)
- ViewModels: `*ViewModel.kt` (e.g., `MainViewModel.kt`)
- Repositories: `*Repository.kt` (e.g., `MapRepository.kt`)
- Workers: `*Worker.kt` (e.g., `MockLocationWorker.kt`)
- DAOs: `*Dao.kt` (e.g., `LocationDao.kt`)
- Database: `*Database.kt` (e.g., `AppDatabase.kt`)
- Entities: `*Bean.kt` (e.g., `LocalLocationBean.kt`)
- Utils: `*Utils.kt` or `*Util.kt` (e.g., `MapUtils.java`, `LocationUtil.kt`)

**Directories:**
- Feature packages: lowercase (e.g., `map/`, `service/`, `utils/`)
- Template packages: lowercase (e.g., `base/`, `common/`, `module/`)
- Sub-packages by layer: `mvvm/v/`, `mvvm/vm/`, `room/dao/`, `model/db/`

**Layouts:**
- Activity: `activity_*.xml` (e.g., `activity_main_nav.xml`)
- Fragment: `fragment_*.xml` (e.g., `fragment_nav_main.xml`)
- Popup: `pop_*.xml` (e.g., `pop_map_search.xml`)
- Item: `item_*.xml` (e.g., `item_location_history_search.xml`)

## Where to Add New Code

**New Feature:**
- Fragment: `app/src/main/java/com/taibao/app/<feature>/<Feature>Fragment.kt`
- ViewModel: `app/src/main/java/com/taibao/app/<feature>/<Feature>ViewModel.kt`
- Repository: `app/src/main/java/com/taibao/app/<feature>/<Feature>Repository.kt`
- Layout: `app/src/main/res/layout/fragment_<feature>.xml`
- Navigation: Add destination to `app/src/main/res/navigation/nav_graph.xml`

**New Database Table:**
- Entity: `app/src/main/java/tech/jour/template/common/model/db/<Entity>Bean.kt`
- DAO: `app/src/main/java/tech/jour/template/common/room/dao/<Entity>Dao.kt`
- Update: `AppDatabase.kt` entities array and abstract DAO method

**New Utility:**
- App-specific: `app/src/main/java/com/taibao/app/utils/<Utility>.kt`
- Generic: `app/src/main/java/tech/jour/template/base/utils/<Utility>.kt`

**New Base Class:**
- UI base: `app/src/main/java/tech/jour/template/common/ui/Base<Name>.kt`
- Framework base: `app/src/main/java/tech/jour/template/base/<category>/<Name>.kt`

**New Service/Worker:**
- Worker: `app/src/main/java/com/taibao/app/service/<Name>Worker.kt`

**New DI Module:**
- Module: `app/src/main/java/tech/jour/template/common/<scope>/DI<Scope>Module.kt`
- Database: `app/src/main/java/tech/jour/template/common/room/DIDatabaseModule.kt` (already exists)

## Special Directories

**`keystore/`:**
- Purpose: Contains signing keystore `GoGoGo.jks`
- Generated: No
- Committed: Yes (WARNING: contains hardcoded passwords in `build.gradle.kts`)
- Note: DO NOT USE IN PRODUCTION - passwords are exposed in build file

**`app/src/main/java/tech/jour/template/module/`:**
- Purpose: Template scaffolding
- Generated: No
- Committed: Yes
- Note: Contains unused DActivity, DFragment, DViewModel, DRepository - can be removed

**`app/src/main/res/navigation/`:**
- Purpose: Jetpack Navigation graph
- Generated: No
- Committed: Yes
- Key file: `nav_graph.xml` defines all fragment destinations

---

*Structure analysis: 2026/06/12*