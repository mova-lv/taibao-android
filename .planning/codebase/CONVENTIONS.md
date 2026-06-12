# Coding Conventions

Generated: 2026/06/12

## Overview

Taibao uses Kotlin 1.9.24 with a template-based MVVM architecture. The codebase follows established Android conventions with Hilt dependency injection, ViewBinding, and extension functions.

## Naming Patterns

### Classes

- **Activities**: Named with `Activity` suffix (e.g., `NavMainActivity`, `WelcomeActivity`, `FakeCameraActivity`)
- **Fragments**: Named with `Fragment` suffix (e.g., `NavMainFragment`, `MapFragment`, `NavDevFragment`)
- **ViewModels**: Named with `ViewModel` suffix (e.g., `MainViewModel`, `MapViewModel`)
- **Repositories**: Named with `Repository` suffix (e.g., `MapRepository`)
- **Workers**: Named with `Worker` suffix (e.g., `MockLocationWorker`)
- **Data classes**: Named with `Bean` suffix for database entities (e.g., `LocalLocationBean`, `SearchLocationBean`)
- **Base classes**: Prefix with `Base` (e.g., `BaseActivity`, `BaseFragment`, `BaseViewModel`, `BaseDao`)
- **Singleton utilities**: Use `object` declaration (e.g., `TUtil`, `MMKVUtils`, `MapUtils`)
- **Constants**: Use `companion object` within classes

### Files

- **Kotlin files**: Match class name exactly (e.g., `MainViewModel.kt` contains `MainViewModel` class)
- **Java utility files**: Legacy utilities retained as `.java` (e.g., `MapUtils.java`, `BarUtils.java`)
- **Package structure**: Lowercase package names following convention (e.g., `com.taibao.app.map`, `tech.jour.template.common.ui`)

### Functions

- **Lifecycle methods**: Use `init` prefix (e.g., `initView()`, `initObserve()`, `initRequestData()`)
- **Event handlers**: Use descriptive verbs (e.g., `startWorker()`, `stopWorker()`, `updateMyLocation()`)
- **Extension functions**: Named as action verbs (e.g., `clickDelay`, `gone()`, `visible()`)
- **Private helpers**: Use descriptive names without prefix (e.g., `syncWorkerStatus()`, `createPhotoFile()`)
- **Repository methods**: Simple CRUD verbs (e.g., `getAll()`, `insert()`, `delete()`)

### Variables

- **Member variables**: Prefix with `m` for clarity in base classes (e.g., `mBinding`, `mViewModel`, `mRepository`), but not enforced in app code
- **LiveData**: Named with `Livedata` suffix (e.g., `selectedLocationLivedata`, `isMockServStart`)
- **Companion object constants**: Use `KEY_` prefix for keys (e.g., `KEY_LATITUDE`, `KEY_LONGITUDE`)
- **Private mutable state**: No prefix in modern Kotlin code (e.g., `currentPhotoUri`, `fakeLatLng`)

### Packages

- **App-specific**: `com.taibao.app` namespace for production code
- **Template framework**: `tech.jour.template` namespace for reusable base classes
- **Subpackages**: Organize by feature/architecture layer (e.g., `.map`, `.service`, `.utils`, `.room.dao`)

## Kotlin Idioms

### Extension Functions

Extensively used throughout the codebase:

- **View extensions**: `app/src/main/java/tech/jour/template/base/ktx/ViewKtx.kt`
  ```kotlin
  fun View.gone() { visibility = View.GONE }
  fun View.visible() { visibility = View.VISIBLE }
  infix fun View.clickDelay(clickAction: () -> Unit) { ... }
  ```

- **Common extensions**: `app/src/main/java/tech/jour/template/base/ktx/CommonExtensions.kt`
  ```kotlin
  fun Float.keepTwoDecimals(): String = DecimalFormat("0.00").format(this)
  fun Any?.d() { Logger.d(this) } // Logging shorthand
  fun Fragment.actionWithPermission(permissions: List<String>, action: () -> Unit)
  ```

### Scope Functions

- **`apply`**: Used for ViewBinding initialization (e.g., `mBinding.apply { ... }`)
- **`let`**: Used for null-safe operations (e.g., `prefs.getString(KEY_CACHED_LAT, "")?.let { ... }`)
- **`also`**: Not commonly used

### Coroutines

- **viewModelScope**: Standard pattern in ViewModels for coroutine launch
  ```kotlin
  viewModelScope.launch { mRepository.insert(bean) }
  ```
- **lifecycleScope**: Used in Fragments for coroutine operations
  ```kotlin
  lifecycleScope.launch { ... }
  ```
- **Dispatchers.IO**: Explicitly used for background work in Workers
  ```kotlin
  withContext(Dispatchers.IO) { mockLocation() }
  ```
- **Flow**: Used for reactive data streams from Room
  ```kotlin
  fun getAll(): Flow<List<LocalLocationBean>>
  ```

### Null Safety

- **Safe calls**: `?.` used extensively for nullable operations
- **Elvis operator**: `?:` for default values (e.g., `inputData.getString(KEY_LATITUDE) ?: return Result.failure()`)
- **Non-null assertions**: Rarely used, prefer safe calls
- **Nullable return types**: Explicit in function signatures (e.g., `fun getString(key: String, defValue: String): String?`)

### Companion Objects

Used for constants and factory methods:

```kotlin
companion object {
    private const val KEY_CACHED_LAT = "KEY_CACHED_LAT"
    const val UNIQUE_WORK_NAME = "虚拟定位"
    fun getInstance(context: Context): AppDatabase { ... }
}
```

### Inline Functions

Used for reified type parameters:

```kotlin
inline fun <reified T> get(key: String): T? {
    val json = MMKV.defaultMMKV().decodeString(key)
    return Gson().fromJson(json, T::class.java)
}
```

## ViewBinding Usage

### Pattern

All Activities and Fragments use ViewBinding via generic base classes:

- **BaseActivity**: `app/src/main/java/tech/jour/template/common/ui/BaseActivity.kt`
  ```kotlin
  abstract class BaseActivity<VB : ViewBinding, VM : BaseViewModel> : BaseFrameActivity<VB, VM>()
  ```

- **BaseFrameActivity**: Auto-inflates ViewBinding via reflection
  ```kotlin
  mBinding = clazzBD.getMethod("inflate", LayoutInflater::class.java)
      .invoke(null, layoutInflater) as VB
  setContentView(mBinding.root)
  ```

- **Usage in subclasses**:
  ```kotlin
  mBinding.apply {
      navMapBtn.clickDelay { findNavController().navigate(R.id.navMapFragment) }
      latitudeEt.setText(it.latitude.toString())
  }
  ```

### Binding Access

- **Member variable**: `mBinding` (lateinit) accessible in all subclasses
- **No DataBinding**: Project uses ViewBinding only, not DataBinding (confirmed in `build.gradle.kts`: `viewBinding = true`, no `dataBinding`)

## Error Handling

### Patterns

- **try-catch blocks**: Used sparingly, primarily for:
  - Reflective operations (e.g., `BaseFrameActivity.createBinding()`)
  - External SDK exceptions (e.g., `BaiduMapSDKException` in `GoApplication.kt`)
  - Location manager operations (e.g., `MockLocationWorker` location injection)

- **Empty catch blocks**: Common pattern for expected failures
  ```kotlin
  try {
      mLocManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)
  } catch (_: IllegalArgumentException) { }
  ```

- **Result type from WorkManager**: Used for Worker success/failure
  ```kotlin
  return Result.failure() // or Result.success()
  ```

- **Exception propagation**: Not used. No sealed class Result wrapper like `Result<S, E>`.

### Error Response Handler

- **Exception handler**: `app/src/main/java/tech/jour/template/common/helper/ExceptionHandler.kt`
  ```kotlin
  @Throws(ResponseException::class)
  suspend fun responseCodeExceptionHandler(code: Int, msg: String?, successBlock: suspend () -> Unit)
  ```
  - Used for API response code handling (but not actively used in current codebase)

### State Management

- **StateLayoutEnum**: `app/src/main/java/tech/jour/template/base/utils/StateLayoutEnum.kt`
  - States: `HIDE`, `LOADING`, `ERROR`, `NO_DATA`
  - Managed via `stateViewLD` in `BaseViewModel`

## Logging

### Framework

- **Logger library**: Uses `com.orhanobut.logger.Logger` (Android-logger v2.2.1)
- **Extension shorthand**: `Any?.d()` function in `app/src/main/java/tech/jour/template/base/ktx/CommonExtensions.kt`
  ```kotlin
  fun Any?.d() { Logger.d(this) }
  ```
- **Android Log**: Also used directly for specific cases
  ```kotlin
  Log.d("ActivityLifecycle", "ActivityStack: ${ActivityStackManager.activityStack}")
  Log.d("BaseApplication", "初始化完成 $allTimeMillis ms")
  ```

### Usage Patterns

- **Debug logging**: Used in development (e.g., application initialization timing, activity lifecycle tracking)
- **Production logging**: Logger initialized in `CommonApplication.onCreate()`
- **Conditional logging**: No conditional compilation for debug/release

## Comments & Documentation

### KDoc/Javadoc

- **Chinese comments**: Most documentation comments are in Chinese
  ```kotlin
  /**
   * Activity基类
   *
   * @author Qu Yunshuo
   * @since 8/27/20
   */
  ```

- **Author attribution**: Uses `@author` and `@since` tags in base classes
- **Function documentation**: Well-documented in utility classes
  ```kotlin
  /**
   * 保留两位，包含0
   */
  fun Float.keepTwoDecimals(): String = DecimalFormat("0.00").format(this)
  ```

### Inline Comments

- **Inline explanations**: Used for complex logic (e.g., coordinate conversion, initialization sequence)
- **TODO comments**: Only one found in `app/src/main/java/tech/jour/template/common/navigation/FragmentNavigatorHideShow.kt`
  ```kotlin
  // TODO Build first class singleUp behavior for fragments
  ```

- **Code comments**: Minimal inline comments, code is generally self-explanatory

## Dependency Injection

### Hilt Patterns

- **Application**: `@HiltAndroidApp` on `GoApplication`
- **Activities**: `@AndroidEntryPoint` on all Activities
- **Fragments**: `@AndroidEntryPoint` on all Fragments
- **ViewModels**: `@HiltViewModel` with `@Inject constructor`
  ```kotlin
  @HiltViewModel
  class MainViewModel @Inject constructor(private val mRepository: MapRepository) : BaseViewModel()
  ```
- **Repositories**: Field injection with `@Inject lateinit`
  ```kotlin
  @Inject lateinit var database: AppDatabase
  @Inject lateinit var locationDao: LocationDao
  ```
- **Modules**: Database module uses Hilt (`app/src/main/java/tech/jour/template/common/room/DIDatabaseModule.kt`)

## Architecture Patterns

### MVVM Structure

- **Three-layer**: Activity/Fragment → ViewModel → Repository
- **Lifecycle hooks**: `initView()`, `initObserve()`, `initRequestData()` in base classes
- **LiveData**: Primary reactive mechanism (not StateFlow)
- **Flow**: Used only for Room database queries

### Repository Pattern

- **Simple CRUD**: No complex data mapping or caching strategy
- **Direct DAO access**: Repositories delegate directly to Room DAOs
  ```kotlin
  fun getAll() = locationDao.getAll()
  fun insert(bean: LocalLocationBean) = locationDao.insert(bean)
  ```

### Singleton Pattern

- **Database**: Singleton via companion object `getInstance()` with double-checked locking
- **Utilities**: Kotlin `object` declaration for static utilities

## Code Organization

### Package Structure

- **App-specific code**: `com.taibao.app` — production code for this app
- **Template framework**: `tech.jour.template` — reusable base classes split into:
  - `.base` — core utilities, MVVM base classes, extensions
  - `.common` — shared app layer (navigation, Room, constants)
  - `.module` — unused scaffolding (DActivity/DFragment)

### Import Organization

Imports are organized by:
1. Android SDK packages
2. Third-party libraries (alphabetical)
3. Project-specific packages

## Linting & Formatting

### Tools

- **No Detekt**: Not configured in project
- **No .editorconfig**: Not present
- **No lint options**: Not explicitly configured in `build.gradle.kts`
- **Kotlin style**: Follows default Kotlin coding conventions

### Code Style Enforcement

- **No explicit enforcement**: No ktlint, Detekt, or Android lint custom rules
- **IDE formatting**: Relies on Android Studio default formatter
- **kapt correctErrorTypes**: Enabled in `build.gradle.kts` for Hilt error handling
  ```kotlin
  kapt { correctErrorTypes = true }
  ```

## Key Files

- `app/build.gradle.kts` — Build configuration, dependencies, Hilt/Room setup
- `app/src/main/java/tech/jour/template/base/mvvm/v/BaseFrameActivity.kt` — ViewBinding reflection pattern
- `app/src/main/java/tech/jour/template/base/mvvm/vm/BaseViewModel.kt` — State management pattern
- `app/src/main/java/tech/jour/template/base/ktx/ViewKtx.kt` — View extension functions
- `app/src/main/java/tech/jour/template/base/ktx/CommonExtensions.kt` — Common utilities and logging
- `app/src/main/java/tech/jour/template/base/utils/MMKVUtils.kt` — MMKV wrapper pattern
- `app/src/main/java/tech/jour/template/common/room/AppDatabase.kt` — Room singleton pattern
- `app/src/main/java/com/taibao/app/MainViewModel.kt` — ViewModel coroutine usage pattern
- `app/src/main/java/com/taibao/app/NavMainFragment.kt` — Fragment ViewBinding and lifecycle hooks pattern

---

Convention analysis: 2026/06/12