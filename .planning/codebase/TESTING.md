# Testing Patterns

Generated: 2026/06/12

## Overview

Taibao has **no test infrastructure**. The project lacks any test directories, test dependencies, or testing framework setup. This is a significant gap for production code.

## Test Infrastructure Status

### Test Directories

**Status: Not present**

Expected test directories do not exist:
- `app/src/test/` — Unit tests (missing)
- `app/src/androidTest/` — Instrumentation/UI tests (missing)

Glob search for test files returned zero results:
```bash
# No files found matching:
**/src/test/**/*.kt
**/src/androidTest/**/*.kt
```

### Test Dependencies

**Status: Not declared**

Analysis of `app/build.gradle.kts` and `gradle/libs.versions.toml`:

**Missing test dependencies:**
- No JUnit 4 or JUnit 5
- No MockK or Mockito
- No Kotlin-test
- No Espresso (declared in version catalog but not used in app module)
- No AndroidX testing libraries (TestRunner, TestRules, etc.)
- No Hilt testing (`hilt-android-testing`)
- No Room testing support
- No WorkManager testing (`work-testing`)
- No Coroutines testing (`kotlinx-coroutines-test`)
- No Turbine or other Flow testing utilities

**Version catalog contains (but unused):**
```toml
espressoCore = "3.5.1"  # Defined but not added to app dependencies
```

**App module dependencies (lines 59-110 in `build.gradle.kts`):**
- Only production dependencies (Hilt, Room, Navigation, Baidu Maps, etc.)
- Zero `testImplementation` or `androidTestImplementation` declarations

### Test Framework

**Status: None configured**

- No test runner configured
- No test rules defined
- No test utilities or helpers
- No mock/fixture infrastructure
- No test-specific configurations (e.g., `testOptions` in `build.gradle.kts`)

## Test Coverage Assessment

### Current Coverage

**0% test coverage**

No tests exist for:
- **ViewModels** — No tests for `MainViewModel`, `MapViewModel`
- **Repository** — No tests for `MapRepository`
- **Worker** — No tests for `MockLocationWorker` (critical foreground service)
- **Database** — No Room migration tests, DAO tests, or entity validation
- **Utils** — No tests for coordinate conversion (`MapUtils.bd2wgs`), permission handling
- **Extensions** — No tests for `ViewKtx`, `CommonExtensions` extension functions
- **Navigation** — No tests for custom `FragmentNavigatorHideShow`
- **Application lifecycle** — No tests for `CommonApplication`, `LoadModuleProxy`

### High-Priority Untested Areas

**Critical components lacking tests:**

1. **MockLocationWorker** (`app/src/main/java/com/taibao/app/service/MockLocationWorker.kt`)
   - Core functionality: Mock location injection loop
   - Risk: Malfunction causes app to fail primary purpose
   - Untested: Test provider registration, location setting, foreground service notification

2. **Coordinate conversion** (`app/src/main/java/com/taibao/app/utils/MapUtils.java`)
   - Functions: `bd2wgs()`, `wgs2bd09()`, `bd09togcj02()`, `gcj02towgs84()`
   - Risk: Incorrect coordinates cause location spoofing to fail
   - Untested: Mathematical transformations, edge cases

3. **Database operations** (`app/src/main/java/tech/jour/template/common/room/dao/LocationDao.kt`)
   - DAO methods: `getAll()`, `insert()`, `delete()`
   - Risk: Data loss or corruption in location history
   - Untested: Flow emission, entity serialization, deduplication logic

4. **ViewModel state management** (`app/src/main/java/com/taibao/app/MainViewModel.kt`)
   - Logic: Worker status sync, history insertion deduplication
   - Risk: State inconsistency, duplicate history entries
   - Untested: LiveData updates, coroutine cancellation, Flow operations

5. **Permission handling** (`app/src/main/java/tech/jour/template/base/ktx/CommonExtensions.kt`)
   - Extension: `actionWithPermission()`
   - Risk: Permission denial handling
   - Untested: Permission grant/deny scenarios, callback invocation

6. **ViewBinding reflection** (`app/src/main/java/tech/jour/template/base/mvvm/v/BaseFrameActivity.kt`)
   - Pattern: `TUtil.getClazz()` for generic ViewBinding inflation
   - Risk: Reflection failure causes Activity crash
   - Untested: Generic type resolution, ViewBinding inflation

## Recommended Testing Strategy

### Test Framework Setup

**Priority: Critical**

Add test dependencies to `app/build.gradle.kts`:

```kotlin
dependencies {
    // Unit testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("app.cash.turbine:turbine:1.1.0")

    // Android instrumentation testing
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.51.1")

    // Room testing
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    // WorkManager testing
    testImplementation("androidx.work:work-testing:2.10.0")
}
```

### Unit Test Priorities

**Phase 1 — Core utilities:**
1. **MapUtils tests** — Validate coordinate transformations with known test vectors
   ```kotlin
   @Test
   fun `bd2wgs should convert Baidu coordinates to WGS84`() {
       val result = MapUtils.bd2wgs(116.404, 39.915)
       assertNotNull(result)
       assertEquals(2, result.size)
   }
   ```

2. **Extension function tests** — Test View extensions, decimal formatting, permission helpers
   ```kotlin
   @Test
   fun `clickDelay should prevent rapid clicks`() {
       val view = mockk<View>(relaxed = true)
       var clickCount = 0
       view.clickDelay { clickCount++ }
       // Verify delay enforcement
   }
   ```

**Phase 2 — ViewModel tests:**
3. **MainViewModel tests** — Worker lifecycle, LiveData emission, Flow subscription
   ```kotlin
   @Test
   fun `insertHistory should deduplicate identical locations`() = runTest {
       val viewModel = MainViewModel(mockRepository)
       val bean = LocalLocationBean(latitude = 1.0, longitude = 2.0, ...)
       viewModel.insertHistory(bean)
       viewModel.insertHistory(bean) // Duplicate
       verify(exactly = 1) { mockRepository.insert(bean) }
   }
   ```

4. **MapViewModel tests** — Geocoding callbacks, search history management

**Phase 3 — Database tests:**
5. **LocationDao tests** — Room DAO CRUD operations, Flow emission
   ```kotlin
   @Test
   fun `getAll should emit Flow of location list`() = runTest {
       val dao = database.locationDao()
       dao.insert(testBean)
       dao.getAll().test {
           val list = awaitItem()
           assertEquals(1, list.size)
           awaitComplete()
       }
   }
   ```

### Instrumentation Test Priorities

**Phase 1 — Worker tests:**
1. **MockLocationWorker tests** — Foreground service, location injection
   ```kotlin
   @Test
   fun doWork_shouldInjectMockLocation() = runTest {
       val worker = TestListenableWorkerBuilder<MockLocationWorker>(context)
           .setInputData(workDataOf(KEY_LATITUDE to "39.9", KEY_LONGITUDE to "116.4"))
           .build()
       val result = worker.doWork()
       assertEquals(ListenableWorker.Result.success(), result)
   }
   ```

**Phase 2 — UI tests:**
2. **Activity/Fragment tests** — Navigation, ViewBinding inflation, lifecycle
3. **Database migration tests** — Room schema migrations

### Test File Organization

**Create directories:**
```
app/src/test/java/
├── com.taibao.app/
│   ├── MainViewModelTest.kt
│   ├── MapViewModelTest.kt
│   └── utils/
│       └── MapUtilsTest.kt
└── tech.jour.template.base/
    ├── ktx/
    │   ├── ViewKtxTest.kt
    │   └── CommonExtensionsTest.kt
    └── utils/
        ├── MMKVUtilsTest.kt
        └── TUtilTest.kt

app/src/androidTest/java/
├── com.taibao.app/
│   ├── service/
│   │   └── MockLocationWorkerTest.kt
│   └── NavMainActivityTest.kt
└── tech.jour.template.common.room/
    └── LocationDaoTest.kt
```

### Test Naming Convention

**Follow Kotlin test naming:**
- Test class: `{ClassName}Test` (e.g., `MainViewModelTest`)
- Test method: Use backticks for descriptive names
  ```kotlin
  @Test
  fun `startWorker should enqueue WorkManager with correct coordinates`() { }
  ```

### Test Utilities Needed

**Create test utilities:**
- `TestBeans.kt` — Factory methods for test entities
  ```kotlin
  object TestBeans {
      fun locationBean() = LocalLocationBean(
          latitude = 39.915,
          longitude = 116.404,
          address = "test address"
      )
  }
  ```

- `MainCoroutineRule` — Standard coroutine test rule
  ```kotlin
  @ExperimentalCoroutinesApi
  class MainCoroutineRule : TestWatcher() {
      val testDispatcher = UnconfinedTestDispatcher()
      override fun starting(description: Description?) {
          Dispatchers.setMain(testDispatcher)
      }
      override fun finished(description: Description?) {
          Dispatchers.resetMain()
      }
  }
  ```

- `HiltTestApplication` — Custom application for Hilt testing

## Mocking Strategy

### What to Mock

- **Repository**: Mock `MapRepository` in ViewModel tests
- **Database**: Use in-memory Room database (`Room.inMemoryDatabaseBuilder`)
- **WorkManager**: Use `WorkManager` test wrapper
- **Baidu Maps SDK**: Mock SDK callbacks (complex due to SDK limitations)
- **Context**: Mock for extension function tests

### What NOT to Mock

- **Room entities** — Use real `LocalLocationBean` instances
- **Data classes** — Use test factories
- **Extension functions on primitives** — Test directly (e.g., `Float.keepTwoDecimals()`)

## Test Execution Commands

**Not applicable** — No tests currently exist.

**Future commands after setup:**
```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run all instrumentation tests
./gradlew connectedDebugAndroidTest

# Run specific test class
./gradlew test --tests "com.taibao.app.MainViewModelTest"

# Generate coverage report (requires Jacoco setup)
./gradlew testDebugUnitTestCoverage
```

## Coverage Requirements

**Current: No requirements enforced**

**Recommendations:**
- Minimum coverage target: 60% for critical components (Worker, ViewModel, Repository)
- Use Jacoco plugin for coverage tracking:
  ```kotlin
  plugins { id("jacoco") }
  android {
      buildTypes {
          debug {
              enableUnitTestCoverage = true
              enableAndroidTestCoverage = true
          }
      }
  }
  ```

## Test Documentation

**Status: No test documentation**

**Need to add:**
- Test strategy document in project wiki
- README section explaining how to run tests
- Coverage badge in README once tests exist

## Testing Gaps Summary

| Category | Status | Priority |
|----------|--------|----------|
| Test directories | Missing | Critical |
| Test dependencies | Missing | Critical |
| Unit tests | 0% | Critical |
| Integration tests | 0% | High |
| UI tests | 0% | Medium |
| Worker tests | 0% | Critical |
| Database tests | 0% | High |
| Coverage tool | Not configured | Medium |
| Mock infrastructure | Missing | Critical |

---

Testing analysis: 2026/06/12