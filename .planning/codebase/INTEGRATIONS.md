# External Integrations

**Analysis Date:** 2026-06-12

## APIs & External Services

**Baidu Maps Platform:**
- **BaiduMapSDK Map 8.1.0** - Map rendering, POI search, geocoding
  - Integration: SDK initialization in `GoApplication.onCreate()`
  - Privacy policy: Must agree to privacy policy via `SDKInitializer.setAgreePrivacy(true)`
  - Coordinate system: BD-09LL (Baidu coordinate type)
  - API Key: Embedded in `AndroidManifest.xml` as meta-data (`com.baidu.lbsapi.API_KEY`)

- **BaiduMapSDK Location 9.6.8** - Positioning service
  - Integration: Separate privacy agreement via `LocationClient.setAgreePrivacy(true)`
  - Background service: `com.baidu.location.f` in separate process (`:remote`)

- **BaiduMapSDK Search 8.1.0** - POI search functionality
- **BaiduMapSDK Util 8.1.0** - Map utility functions

## Data Storage

**Databases:**
- Room Database
  - Database: `room.db`
  - Location: Local app storage
  - ORM: Room 2.6.1 with `fallbackToDestructiveMigration()`
  - Entity: `LocalLocationBean` (timestamp PK, address, coordinates)
  - DAO: `LocationDao` extends `BaseDao<LocalLocationBean>`
  - Access: Singleton via `AppDatabase.getInstance()`

**File Storage:**
- Local filesystem only
- FileProvider configured (`${applicationId}.fileProvider`)
- Paths: `res/xml/provider_paths.xml`

**Key-Value Storage:**
- MMKV 2.4.0
  - Initialization: `MMKVUtils.initMMKV()` in `CommonApplication.initByFrontDesk()`
  - Used for: Search history persistence

**Caching:**
- None detected (Coil handles image caching internally)

## Authentication & Identity

**Auth Provider:**
- Custom - No authentication/identity provider integrated
- Mock location app does not require user accounts

## Monitoring & Observability

**Error Tracking:**
- CustomActivityOnCrash 2.4.0 - Crash handling and restart
- Tencent Bugly integration (commented out in code, not active)
  - Configuration present in `CommonApplication.initTencentBugly()` but disabled

**Logs:**
- Android Logger v2.2.1 (com.github.journe:Android-logger)
  - Initialization: `CommonApplication.initLogger()`
  - Tag: "jour"
  - Loggable only in `BuildConfig.DEBUG` builds
  - Logs foreground/background state changes

**Network Monitoring:**
- Custom `NetworkStateClient` implementation
  - Initialized in `CommonApplication.initNetworkStateClient()`
  - Listens to network connectivity changes

## CI/CD & Deployment

**Hosting:**
- Android app (APK) - No CI/CD detected
- Manual build via Gradle

**CI Pipeline:**
- None configured

**Signing:**
- Debug and release both use same keystore: `keystore/GoGoGo.jks`
- Keystore passwords hardcoded in `app/build.gradle.kts` (security risk)

## Environment Configuration

**Required env vars:**
- None detected - All configuration is hardcoded

**Secrets location:**
- Baidu Maps API Key: `AndroidManifest.xml` (meta-data)
- Keystore credentials: `app/build.gradle.kts` (hardcoded - should use environment variables)

**Privacy compliance:**
- Baidu SDK requires privacy policy agreement
- Both `SDKInitializer.setAgreePrivacy(true)` and `LocationClient.setAgreePrivacy(true)` called at app startup

## Webhooks & Callbacks

**Incoming:**
- None

**Outgoing:**
- None

## Location Services

**Android Location Framework:**
- LocationManager (system service)
  - Test providers: `GPS_PROVIDER`, `NETWORK_PROVIDER`
  - Mock location injection via `addTestProvider()` and `setTestProviderLocation()`
  - Coordinate conversion: BD-09 → WGS84 via `MapUtils.bd2wgs()`

**Coordinate Systems:**
- **BD-09LL** - Baidu Maps SDK coordinate system
- **WGS84** - Android GPS coordinate system (required for mock location)
- Conversion: `MapUtils` provides bidirectional conversion (`bd2wgs()`, `wgs2bd09()`)

**Foreground Service:**
- WorkManager `CoroutineWorker` (`MockLocationWorker`)
  - Foreground service type: `FOREGROUND_SERVICE_TYPE_LOCATION`
  - Notification channel: "虚拟定位" (Virtual Location)
  - Update interval: 32ms loop cycle
  - Provides persistent mock location injection

---

*Integration audit: 2026-06-12*