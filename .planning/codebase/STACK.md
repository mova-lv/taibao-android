# Technology Stack

**Analysis Date:** 2026-06-12

## Languages

**Primary:**
- Kotlin 1.9.24 - Main development language for Android app and all business logic
- Java 17 (JVM target) - Used for legacy utility classes (`MapUtils.java`, `BarUtils.java`, `JsonUtil.java`, `SpannableStringUtils.java`)

**Secondary:**
- Java (some template/base classes) - `GoApplication.java` exists but actual file is `GoApplication.kt`, Java present only in utility modules

## Runtime

**Environment:**
- Android SDK
- Min SDK: 26 (Android 8.0 Oreo)
- Target SDK: 33 (Android 13)
- Compile SDK: 35 (Android 15)

**Package Manager:**
- Gradle 8.9 with Kotlin DSL
- Gradle Wrapper present
- Version catalog: `gradle/libs.versions.toml`

## Frameworks

**Core:**
- Android Gradle Plugin 8.7.2 - Build system
- Hilt 2.51.1 - Dependency injection framework
- AndroidX Navigation 2.8.8 - Single-activity navigation with Fragment destinations

**Testing:**
- Not configured in dependencies - No test libraries detected

**Build/Dev:**
- Kotlin KAPT - Annotation processing (Hilt, Room, AutoService)
- KSP (Google KSP Plugin) - Additional annotation processing alongside KAPT
- ViewBinding - Enabled in build features

## Key Dependencies

**Critical:**
- Baidu Map SDK 8.1.0 (Map, Util, Search) + Location 9.6.8 - Core map functionality and positioning
- Room 2.6.1 - Local database ORM for location history
- WorkManager 2.10.0 - Foreground service for mock location injection
- MMKV 2.4.0 - Key-value storage for search history
- Lifecycle 2.7.0 - ViewModel and LiveData for MVVM architecture
- Gson 2.10 - JSON serialization

**Infrastructure:**
- AndroidX Core KTX 1.13.1 - Kotlin extensions
- AndroidX AppCompat 1.7.0 - Backward compatibility
- Material 1.12.0 - Material Design components
- ConstraintLayout 2.2.1 - UI layout
- Paging 3.3.2 - Data pagination support
- Coil 2.7.0 - Image loading
- PermissionX 1.8.1 - Runtime permission handling
- XPopup 2.10.0 - Popup dialogs
- UtilCodeX 1.31.1 - Android utility library
- CustomActivityOnCrash 2.4.0 - Crash handling
- Android Logger v2.2.1 - Logging framework

**Annotation Processing:**
- Google Auto Service 1.0 - ServiceLoader code generation for `ApplicationLifecycle`
- Hilt Compiler 2.51.1 - DI code generation
- Room Compiler 2.6.1 - Database code generation

## Configuration

**Environment:**
- JVM Target: Java 17
- Kotlin JVM Target: 17
- Android namespace: `com.taibao.app`
- Application ID: `com.taibao.app`
- Version: 1.0 (versionCode 1)

**Build:**
- Build config: `app/build.gradle.kts`
- Version catalog: `gradle/libs.versions.toml`
- Settings: `settings.gradle.kts`
- Proguard: `proguard-rules.pro` (minify disabled)
- Signing: Debug and release both use `keystore/GoGoGo.jks` (embedded passwords - security risk)

**Build Features:**
- ViewBinding: Enabled
- BuildConfig: Enabled
- DataBinding: Not used

## Platform Requirements

**Development:**
- Android Studio (minimum version not specified)
- JDK 17
- Gradle 8.9
- Kotlin 1.9.24 plugin

**Production:**
- Android 8.0+ (API 26+)
- Target: Android 13 (API 33)
- Foreground service with location type
- Mock location permission required
- Background location permission required

---

*Stack analysis: 2026-06-12*