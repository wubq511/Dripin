# Dripin MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a pure local Android MVP that receives shared links/text/images, saves them in a local Room database with simple rule-based tags and categories, and pushes a daily “Today” recommendation notification without any server or reader feature.

**Architecture:** Create one Android app module with strong package boundaries under `com.dripin.app`: `core` for shared models/utilities/theme, `data` for Room/DataStore/repositories, `feature` for capture/home/detail/recommendation/settings, and `worker` for scheduling and notifications. Use Hilt for dependency wiring, Navigation Compose for in-app routing, Room for content storage, DataStore for preferences, and WorkManager for daily batch generation.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Navigation Compose 2.9.7, Activity Compose 1.12.4, Lifecycle 2.10.0, Room 2.8.4, DataStore 1.2.1, WorkManager 2.11.1, Dagger Hilt 2.59.2 with AndroidX Hilt 1.3.0, Coil 3.4.0, OkHttp 5.3.0, Jsoup 1.22.1.

---

## File Structure

### Root build files

- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/proguard-rules.pro`

### Application shell

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/dripin/app/DripinApplication.kt`
- `app/src/main/java/com/dripin/app/MainActivity.kt`
- `app/src/main/java/com/dripin/app/DripinApp.kt`
- `app/src/main/java/com/dripin/app/navigation/DripinNavGraph.kt`
- `app/src/main/java/com/dripin/app/navigation/DripinDestination.kt`

### Core

- `app/src/main/java/com/dripin/app/core/model/ContentType.kt`
- `app/src/main/java/com/dripin/app/core/model/TagType.kt`
- `app/src/main/java/com/dripin/app/core/model/RecommendationSortMode.kt`
- `app/src/main/java/com/dripin/app/core/model/HomeFilterState.kt`
- `app/src/main/java/com/dripin/app/core/common/UrlCanonicalizer.kt`
- `app/src/main/java/com/dripin/app/core/common/SourcePlatformClassifier.kt`
- `app/src/main/java/com/dripin/app/core/common/TopicClassifier.kt`
- `app/src/main/java/com/dripin/app/core/common/ClockProvider.kt`
- `app/src/main/java/com/dripin/app/core/designsystem/theme/Color.kt`
- `app/src/main/java/com/dripin/app/core/designsystem/theme/Theme.kt`
- `app/src/main/java/com/dripin/app/core/designsystem/theme/Typography.kt`
- `app/src/main/java/com/dripin/app/core/designsystem/component/FilterChipRow.kt`
- `app/src/main/java/com/dripin/app/core/designsystem/component/SectionCard.kt`

### Data

- `app/src/main/java/com/dripin/app/data/local/AppDatabase.kt`
- `app/src/main/java/com/dripin/app/data/local/Converters.kt`
- `app/src/main/java/com/dripin/app/data/local/entity/SavedItemEntity.kt`
- `app/src/main/java/com/dripin/app/data/local/entity/TagEntity.kt`
- `app/src/main/java/com/dripin/app/data/local/entity/ItemTagCrossRef.kt`
- `app/src/main/java/com/dripin/app/data/local/entity/DailyRecommendationEntity.kt`
- `app/src/main/java/com/dripin/app/data/local/entity/DailyRecommendationItemEntity.kt`
- `app/src/main/java/com/dripin/app/data/local/dao/SavedItemDao.kt`
- `app/src/main/java/com/dripin/app/data/local/dao/TagDao.kt`
- `app/src/main/java/com/dripin/app/data/local/dao/DailyRecommendationDao.kt`
- `app/src/main/java/com/dripin/app/data/preferences/UserPreferences.kt`
- `app/src/main/java/com/dripin/app/data/preferences/UserPreferencesRepository.kt`
- `app/src/main/java/com/dripin/app/data/repository/SavedItemRepository.kt`
- `app/src/main/java/com/dripin/app/data/repository/RecommendationRepository.kt`
- `app/src/main/java/com/dripin/app/data/repository/SettingsRepository.kt`
- `app/src/main/java/com/dripin/app/data/metadata/LinkMetadataFetcher.kt`
- `app/src/main/java/com/dripin/app/data/metadata/LinkMetadata.kt`

### Features

- `app/src/main/java/com/dripin/app/feature/capture/ShareReceiverActivity.kt`
- `app/src/main/java/com/dripin/app/feature/capture/IncomingSharePayload.kt`
- `app/src/main/java/com/dripin/app/feature/capture/ShareIntentParser.kt`
- `app/src/main/java/com/dripin/app/feature/capture/SaveItemUiState.kt`
- `app/src/main/java/com/dripin/app/feature/capture/SaveItemViewModel.kt`
- `app/src/main/java/com/dripin/app/feature/capture/SaveItemScreen.kt`
- `app/src/main/java/com/dripin/app/feature/home/HomeViewModel.kt`
- `app/src/main/java/com/dripin/app/feature/home/HomeScreen.kt`
- `app/src/main/java/com/dripin/app/feature/home/SavedItemCard.kt`
- `app/src/main/java/com/dripin/app/feature/detail/DetailViewModel.kt`
- `app/src/main/java/com/dripin/app/feature/detail/DetailScreen.kt`
- `app/src/main/java/com/dripin/app/feature/recommendation/TodayViewModel.kt`
- `app/src/main/java/com/dripin/app/feature/recommendation/TodayScreen.kt`
- `app/src/main/java/com/dripin/app/feature/recommendation/RecommendationCard.kt`
- `app/src/main/java/com/dripin/app/feature/settings/SettingsViewModel.kt`
- `app/src/main/java/com/dripin/app/feature/settings/SettingsScreen.kt`

### Worker / notifications

- `app/src/main/java/com/dripin/app/worker/DailyRecommendationWorker.kt`
- `app/src/main/java/com/dripin/app/worker/DailyRecommendationScheduler.kt`
- `app/src/main/java/com/dripin/app/worker/RecommendationNotifier.kt`

### Tests

- `app/src/test/java/com/dripin/app/core/common/UrlCanonicalizerTest.kt`
- `app/src/test/java/com/dripin/app/core/common/SourcePlatformClassifierTest.kt`
- `app/src/test/java/com/dripin/app/core/common/TopicClassifierTest.kt`
- `app/src/test/java/com/dripin/app/feature/capture/ShareIntentParserTest.kt`
- `app/src/test/java/com/dripin/app/feature/capture/SaveItemViewModelTest.kt`
- `app/src/test/java/com/dripin/app/feature/home/HomeViewModelTest.kt`
- `app/src/test/java/com/dripin/app/feature/detail/DetailViewModelTest.kt`
- `app/src/test/java/com/dripin/app/data/preferences/UserPreferencesRepositoryTest.kt`
- `app/src/test/java/com/dripin/app/data/repository/RecommendationRepositoryTest.kt`
- `app/src/androidTest/java/com/dripin/app/data/local/AppDatabaseTest.kt`
- `app/src/androidTest/java/com/dripin/app/feature/capture/ShareCaptureFlowTest.kt`
- `app/src/androidTest/java/com/dripin/app/feature/recommendation/TodayNotificationDeepLinkTest.kt`

### Task 1: Bootstrap the Android project and dependency baseline

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`
- Create: `app/proguard-rules.pro`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/dripin/app/DripinApplication.kt`
- Create: `app/src/main/java/com/dripin/app/MainActivity.kt`
- Create: `app/src/main/java/com/dripin/app/DripinApp.kt`
- Test: `app/src/test/java/com/dripin/app/SmokeTest.kt`

- [ ] **Step 1: Initialize the repo and create the Android/Kotlin DSL skeleton**

Run:

```powershell
git init
New-Item -ItemType Directory -Force gradle, app, app\src\main\java\com\dripin\app, app\src\test\java\com\dripin\app
```

Write the root settings and plugin catalog using these versions:

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("com.android.application") version "9.1.0"
        id("com.android.legacy-kapt") version "9.1.0"
        id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
        id("com.google.dagger.hilt.android") version "2.59.2"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Dripin"
include(":app")
```

```toml
# gradle/libs.versions.toml
[versions]
agp = "9.1.0"
hilt = "2.59.2"
composeBom = "2026.03.00"
activityCompose = "1.12.4"
lifecycle = "2.10.0"
navigation = "2.9.7"
room = "2.8.4"
work = "2.11.1"
datastore = "1.2.1"
androidxHilt = "1.3.0"
coil = "3.4.0"
okhttp = "5.3.0"
jsoup = "1.22.1"
```

- [ ] **Step 2: Add the app module build script and manifest**

Use an `app/build.gradle.kts` that enables Compose, Hilt, Room, WorkManager, DataStore, Coil, OkHttp, Jsoup, and the test stacks:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.android.legacy-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.dripin.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dripin.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.03.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.03.00"))
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.7")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    kapt("androidx.room:room-compiler:2.8.4")
    implementation("androidx.work:work-runtime-ktx:2.11.1")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("io.coil-kt.coil3:coil-compose:3.4.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.0")
    implementation("org.jsoup:jsoup:1.22.1")
    implementation("com.google.dagger:hilt-android:2.59.2")
    kapt("com.google.dagger:hilt-compiler:2.59.2")
}
```

Note: AGP 9.1 enables built-in Kotlin support, so this baseline should not wire `org.jetbrains.kotlin.android` or `org.jetbrains.kotlin.kapt` directly. Use `com.android.legacy-kapt` as the temporary bridge for Room/Hilt annotation processing in the MVP, then plan a later KSP migration once the data layer stabilizes.

Add an `AndroidManifest.xml` with:

```xml
<application
    android:name=".DripinApplication"
    android:allowBackup="false"
    android:supportsRtl="true"
    android:theme="@style/Theme.Dripin">
    <activity android:name=".MainActivity" android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

- [ ] **Step 3: Add a failing smoke test**

```kotlin
class SmokeTest {
    @Test
    fun applicationClassExists() {
        assertEquals("com.dripin.app.DripinApplication", DripinApplication::class.qualifiedName)
    }
}
```

- [ ] **Step 4: Run the smoke test to verify the scaffold is still incomplete**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.SmokeTest"
```

Expected: FAIL because `DripinApplication` and the main Compose app shell are not implemented yet.

- [ ] **Step 5: Implement the minimal app entrypoint**

```kotlin
@HiltAndroidApp
class DripinApplication : Application()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DripinApp() }
    }
}

@Composable
fun DripinApp() {
    MaterialTheme {
        Box(Modifier.fillMaxSize()) {
            Text("Dripin bootstrap")
        }
    }
}
```

- [ ] **Step 6: Verify the baseline app builds**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.SmokeTest"
.\gradlew.bat :app:assembleDebug
```

Expected: both commands PASS.

- [ ] **Step 7: Commit the bootstrap**

```powershell
git add .
git commit -m "chore: bootstrap Android app baseline"
```

### Task 2: Build the shared theme, navigation shell, and first-pass feature routes

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/java/com/dripin/app/DripinApp.kt`
- Create: `app/src/main/java/com/dripin/app/navigation/DripinDestination.kt`
- Create: `app/src/main/java/com/dripin/app/navigation/DripinNavGraph.kt`
- Create: `app/src/main/java/com/dripin/app/core/designsystem/theme/Color.kt`
- Create: `app/src/main/java/com/dripin/app/core/designsystem/theme/Typography.kt`
- Create: `app/src/main/java/com/dripin/app/core/designsystem/theme/Theme.kt`
- Create: `app/src/main/java/com/dripin/app/feature/home/HomeScreen.kt`
- Create: `app/src/main/java/com/dripin/app/feature/recommendation/TodayScreen.kt`
- Create: `app/src/main/java/com/dripin/app/feature/settings/SettingsScreen.kt`
- Test: `app/src/androidTest/java/com/dripin/app/navigation/DripinNavGraphTest.kt`

- [ ] **Step 1: Write the failing app shell navigation test**

```kotlin
@RunWith(AndroidJUnit4::class)
class DripinNavGraphTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeSettingsAndTodayDestinationsRender() {
        composeRule.onNodeWithTag("nav-home").assertIsDisplayed()
        composeRule.onNodeWithTag("nav-today").assertIsDisplayed()
        composeRule.onNodeWithTag("nav-settings").assertIsDisplayed()
    }
}
```

Before compiling the test, extend `app/build.gradle.kts` with the Android instrumentation stack required by Compose testing:

```kotlin
androidTestImplementation("androidx.test.ext:junit:1.3.0")
androidTestImplementation("androidx.test:rules:1.7.0")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

- [ ] **Step 2: Run the instrumentation test and watch it fail**

Run:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.dripin.app.navigation.DripinNavGraphTest
```

Expected: FAIL because the shell routes and first-pass screens do not exist.

- [ ] **Step 3: Implement the visual system and navigation host**

```kotlin
sealed interface DripinDestination {
    data object Home : DripinDestination
    data object Today : DripinDestination
    data object Settings : DripinDestination
    data object Save : DripinDestination
    data class Detail(val itemId: Long) : DripinDestination
}

@Composable
fun DripinApp() {
    DripinTheme {
        val navController = rememberNavController()
        DripinNavGraph(navController = navController)
    }
}

@Composable
fun DripinNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(onOpenToday = { navController.navigate("today") }) }
        composable("today") { TodayScreen() }
        composable("settings") { SettingsScreen() }
        composable("save") { Text("Save draft") }
    }
}
```

- [ ] **Step 4: Add first-pass screens with final label text**

```kotlin
@Composable
fun HomeScreen(onOpenToday: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Home") }) }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Button(onClick = onOpenToday) { Text("Today") }
            Text("Home")
            Text("Settings")
        }
    }
}
```

Mirror the same approach for `TodayScreen` and `SettingsScreen` so the three labels are visible immediately.

- [ ] **Step 5: Re-run the instrumentation shell test**

Run:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.dripin.app.navigation.DripinNavGraphTest
```

Expected: PASS.

- [ ] **Step 6: Commit the shell**

```powershell
git add .
git commit -m "feat: add themed app shell and initial navigation"
```

### Task 3: Implement Room storage, duplicate-safe link persistence, and classification primitives

**Files:**
- Create: `app/src/main/java/com/dripin/app/core/model/ContentType.kt`
- Create: `app/src/main/java/com/dripin/app/core/model/TagType.kt`
- Create: `app/src/main/java/com/dripin/app/core/model/HomeFilterState.kt`
- Create: `app/src/main/java/com/dripin/app/core/common/UrlCanonicalizer.kt`
- Create: `app/src/main/java/com/dripin/app/core/common/SourcePlatformClassifier.kt`
- Create: `app/src/main/java/com/dripin/app/core/common/TopicClassifier.kt`
- Create: `app/src/main/java/com/dripin/app/data/local/Converters.kt`
- Create: `app/src/main/java/com/dripin/app/data/local/AppDatabase.kt`
- Create: `app/src/main/java/com/dripin/app/data/local/entity/SavedItemEntity.kt`
- Create: `app/src/main/java/com/dripin/app/data/local/entity/TagEntity.kt`
- Create: `app/src/main/java/com/dripin/app/data/local/entity/ItemTagCrossRef.kt`
- Create: `app/src/main/java/com/dripin/app/data/local/dao/SavedItemDao.kt`
- Create: `app/src/main/java/com/dripin/app/data/local/dao/TagDao.kt`
- Create: `app/src/main/java/com/dripin/app/data/repository/SavedItemRepository.kt`
- Test: `app/src/test/java/com/dripin/app/core/common/UrlCanonicalizerTest.kt`
- Test: `app/src/test/java/com/dripin/app/core/common/SourcePlatformClassifierTest.kt`
- Test: `app/src/test/java/com/dripin/app/core/common/TopicClassifierTest.kt`
- Test: `app/src/androidTest/java/com/dripin/app/data/local/AppDatabaseTest.kt`

- [ ] **Step 1: Write failing tests for URL normalization**

```kotlin
class UrlCanonicalizerTest {
    @Test
    fun strips_fragment_and_tracking_params() {
        val input = "https://GitHub.com/openai/openai?utm_source=x&tab=readme#top"
        assertEquals(
            "https://github.com/openai/openai?tab=readme",
            UrlCanonicalizer.canonicalize(input)
        )
    }

    @Test
    fun preserves_meaningful_query_params() {
        val input = "https://mp.weixin.qq.com/s?id=123&scene=1"
        assertEquals(
            "https://mp.weixin.qq.com/s?id=123&scene=1",
            UrlCanonicalizer.canonicalize(input)
        )
    }
}
```

- [ ] **Step 2: Run the canonicalizer tests to verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.core.common.UrlCanonicalizerTest"
```

Expected: FAIL because `UrlCanonicalizer` does not exist.

- [ ] **Step 3: Implement the canonicalizer**

```kotlin
object UrlCanonicalizer {
    private val ignoredPrefixes = listOf("utm_")
    private val ignoredNames = setOf("spm", "si")

    fun canonicalize(raw: String): String {
        val uri = Uri.parse(raw.trim())
        val kept = uri.queryParameterNames
            .filterNot { name -> ignoredNames.contains(name) || ignoredPrefixes.any(name::startsWith) }
            .sorted()
            .flatMap { name -> uri.getQueryParameters(name).map { value -> name to value } }

        return uri.buildUpon()
            .scheme(uri.scheme?.lowercase())
            .authority(uri.authority?.lowercase())
            .fragment(null)
            .clearQuery()
            .apply { kept.forEach { (name, value) -> appendQueryParameter(name, value) } }
            .build()
            .toString()
    }
}
```

- [ ] **Step 4: Re-run the canonicalizer tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.core.common.UrlCanonicalizerTest"
```

Expected: PASS.

- [ ] **Step 5: Write failing tests for platform and topic inference**

```kotlin
class SourcePlatformClassifierTest {
    @Test
    fun maps_known_packages() {
        assertEquals("微信", SourcePlatformClassifier.classify("com.tencent.mm", null))
        assertEquals("GitHub", SourcePlatformClassifier.classify("com.github.android", null))
    }
}

class TopicClassifierTest {
    @Test
    fun infers_development_from_title() {
        assertEquals("开发", TopicClassifier.classify("OpenAI repo release note", "github.com"))
    }
}
```

- [ ] **Step 6: Run the classifier tests to verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.core.common.SourcePlatformClassifierTest"
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.core.common.TopicClassifierTest"
```

Expected: both commands FAIL.

- [ ] **Step 7: Implement the classifiers**

```kotlin
object SourcePlatformClassifier {
    fun classify(packageName: String?, domain: String?): String? = when {
        packageName == "com.tencent.mm" -> "微信"
        packageName == "com.github.android" -> "GitHub"
        packageName == "tv.danmaku.bili" -> "B站"
        packageName == "com.ss.android.ugc.aweme" -> "抖音"
        domain == "github.com" -> "GitHub"
        domain == "x.com" -> "X"
        else -> null
    }
}

object TopicClassifier {
    fun classify(title: String?, domain: String?): String? {
        val haystack = listOfNotNull(title, domain).joinToString(" ").lowercase()
        return when {
            listOf("repo", "release", "issue", "github").any(haystack::contains) -> "开发"
            listOf("video", "bilibili", "youtube").any(haystack::contains) -> "视频"
            listOf("blog", "article", "newsletter", "medium").any(haystack::contains) -> "文章"
            else -> null
        }
    }
}
```

- [ ] **Step 8: Add a failing Room integration test**

```kotlin
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    @Test
    fun canonical_url_is_unique() = runBlocking {
        val db = buildInMemoryDb()
        val dao = db.savedItemDao()
        val item = SavedItemEntity(
            id = 0,
            contentType = ContentType.LINK,
            title = "One",
            rawUrl = "https://github.com/openai/openai",
            canonicalUrl = "https://github.com/openai/openai",
            textContent = null,
            imageUri = null,
            sourceAppPackage = null,
            sourceAppLabel = null,
            sourcePlatform = "GitHub",
            sourceDomain = "github.com",
            topicCategory = "开发",
            note = null,
            createdAt = Instant.parse("2026-04-13T12:00:00Z"),
            updatedAt = Instant.parse("2026-04-13T12:00:00Z"),
            isRead = false,
            readAt = null,
            pushCount = 0,
            lastPushedAt = null,
            lastRecommendedDate = null,
        )
        dao.insert(item)
        var threwConstraint = false
        try {
            dao.insert(item.copy(id = 0, title = "Two"))
        } catch (_: SQLiteConstraintException) {
            threwConstraint = true
        }
        assertTrue(threwConstraint)
    }
}
```

- [ ] **Step 9: Run the database test to verify failure**

Run:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.dripin.app.data.local.AppDatabaseTest
```

Expected: FAIL because the entities/DAO/database are not implemented.

- [ ] **Step 10: Implement the entities, DAO, and repository**

Key schema requirements:

```kotlin
@Entity(
    tableName = "saved_items",
    indices = [Index(value = ["canonicalUrl"], unique = true)]
)
data class SavedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contentType: ContentType,
    val title: String?,
    val rawUrl: String?,
    val canonicalUrl: String?,
    val textContent: String?,
    val imageUri: String?,
    val sourceAppPackage: String?,
    val sourceAppLabel: String?,
    val sourcePlatform: String?,
    val sourceDomain: String?,
    val topicCategory: String?,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isRead: Boolean,
    val readAt: Instant?,
    val pushCount: Int,
    val lastPushedAt: Instant?,
    val lastRecommendedDate: LocalDate?,
)

@Dao
interface SavedItemDao {
    @Insert suspend fun insert(item: SavedItemEntity): Long
    @Update suspend fun update(item: SavedItemEntity)
    @Query("SELECT * FROM saved_items WHERE canonicalUrl = :canonicalUrl LIMIT 1")
    suspend fun findByCanonicalUrl(canonicalUrl: String): SavedItemEntity?
}
```

The repository should expose:

```kotlin
data class LinkSaveRequest(
    val rawUrl: String,
    val title: String? = null,
    val textContent: String? = null,
    val note: String? = null,
    val sourceAppPackage: String? = null,
    val sourceAppLabel: String? = null,
    val tags: List<String> = emptyList(),
)

suspend fun upsertSharedLink(request: LinkSaveRequest): SaveResult
suspend fun saveText(
    text: String,
    title: String?,
    note: String?,
    sourceAppPackage: String?,
    tags: List<String>,
): Long
suspend fun saveImage(
    imageUri: String,
    title: String?,
    note: String?,
    sourceAppPackage: String?,
    tags: List<String>,
): Long
```

with duplicate handling returning either `Created(itemId)` or `UpdatedExisting(itemId)`.

- [ ] **Step 11: Re-run the classifier and database tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.core.common.*"
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.dripin.app.data.local.AppDatabaseTest
```

Expected: PASS.

- [ ] **Step 12: Commit persistence and classifier support**

```powershell
git add .
git commit -m "feat: add persistence, url canonicalization, and classifiers"
```

### Task 4: Implement share parsing, metadata fetching, and the editable save flow

**Files:**
- Create: `app/src/main/java/com/dripin/app/feature/capture/IncomingSharePayload.kt`
- Create: `app/src/main/java/com/dripin/app/feature/capture/ShareIntentParser.kt`
- Create: `app/src/main/java/com/dripin/app/data/metadata/LinkMetadata.kt`
- Create: `app/src/main/java/com/dripin/app/data/metadata/LinkMetadataFetcher.kt`
- Create: `app/src/main/java/com/dripin/app/feature/capture/SaveItemUiState.kt`
- Create: `app/src/main/java/com/dripin/app/feature/capture/SaveItemViewModel.kt`
- Create: `app/src/main/java/com/dripin/app/feature/capture/SaveItemScreen.kt`
- Create: `app/src/main/java/com/dripin/app/feature/capture/ShareReceiverActivity.kt`
- Test: `app/src/test/java/com/dripin/app/feature/capture/ShareIntentParserTest.kt`
- Test: `app/src/test/java/com/dripin/app/feature/capture/SaveItemViewModelTest.kt`

- [ ] **Step 1: Write failing share-intent parsing tests**

```kotlin
class ShareIntentParserTest {
    @Test
    fun parses_plain_url_text_as_link() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://github.com/openai/openai")
        }
        val parsed = ShareIntentParser.parse(intent, "com.github.android", "GitHub")
        assertEquals(ContentType.LINK, parsed.contentType)
        assertEquals("https://github.com/openai/openai", parsed.sharedUrl)
    }

    @Test
    fun parses_image_share_as_image_payload() {
        val uri = Uri.parse("content://media/external/images/media/1")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        val parsed = ShareIntentParser.parse(intent, "tv.danmaku.bili", "B站")
        assertEquals(ContentType.IMAGE, parsed.contentType)
        assertEquals(uri.toString(), parsed.sharedImageUri)
    }
}
```

- [ ] **Step 2: Run the parser test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.feature.capture.ShareIntentParserTest"
```

Expected: FAIL.

- [ ] **Step 3: Implement the parser**

```kotlin
data class IncomingSharePayload(
    val contentType: ContentType,
    val sharedText: String? = null,
    val sharedUrl: String? = null,
    val sharedImageUri: String? = null,
    val sourceAppPackage: String? = null,
    val sourceAppLabel: String? = null,
)

object ShareIntentParser {
    fun parse(intent: Intent, sourcePackage: String?, sourceLabel: String?): IncomingSharePayload {
        val type = intent.type.orEmpty()
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.toString()
        return when {
            type.startsWith("image/") && imageUri != null ->
                IncomingSharePayload(ContentType.IMAGE, sharedImageUri = imageUri, sourceAppPackage = sourcePackage, sourceAppLabel = sourceLabel)
            text != null && text.startsWith("http") ->
                IncomingSharePayload(ContentType.LINK, sharedUrl = text, sourceAppPackage = sourcePackage, sourceAppLabel = sourceLabel)
            else ->
                IncomingSharePayload(ContentType.TEXT, sharedText = text, sourceAppPackage = sourcePackage, sourceAppLabel = sourceLabel)
        }
    }
}
```

- [ ] **Step 4: Write a failing save-flow ViewModel test**

```kotlin
class SaveItemViewModelTest {
    @Test
    fun link_without_title_fetches_metadata_and_keeps_manual_override() = runTest {
        val metadataFetcher = FakeMetadataFetcher(LinkMetadata(title = "Fetched Title"))
        val vm = SaveItemViewModel(
            initialPayload = IncomingSharePayload(
                contentType = ContentType.LINK,
                sharedUrl = "https://github.com/openai/openai",
                sourceAppPackage = "com.github.android",
                sourceAppLabel = "GitHub",
            ),
            metadataFetcher = metadataFetcher,
            repository = FakeSavedItemRepository(),
            sourcePlatformClassifier = SourcePlatformClassifier,
            topicClassifier = TopicClassifier,
            savedStateHandle = SavedStateHandle(),
        )

        vm.onTitleChanged("Manual Title")
        advanceUntilIdle()

        assertEquals("Manual Title", vm.uiState.value.title)
    }
}
```

- [ ] **Step 5: Run the ViewModel test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.feature.capture.SaveItemViewModelTest"
```

Expected: FAIL.

- [ ] **Step 6: Implement metadata fetching and the save UI state machine**

Fetch metadata with OkHttp and Jsoup:

```kotlin
class LinkMetadataFetcher @Inject constructor(
    private val client: OkHttpClient,
) {
    suspend fun fetch(url: String): LinkMetadata? = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).header("User-Agent", "Dripin/0.1").build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val html = response.body?.string().orEmpty()
            val document = Jsoup.parse(html)
            val title = document.selectFirst("meta[property=og:title]")?.attr("content")
                ?.ifBlank { null }
                ?: document.title().ifBlank { null }
            LinkMetadata(title = title)
        }
    }
}
```

The ViewModel should:

- prefill title/url/text/image/source/domain
- generate initial domain/platform/topic tags
- fetch metadata only for link items without a user-entered title
- switch save action label between `保存` and `更新已有内容`

- [ ] **Step 7: Implement the save screen and share receiver activity**

The `ShareReceiverActivity` should parse the incoming intent, then forward into the Compose save route:

```kotlin
class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val payload = ShareIntentParser.parse(intent, callingPackage, null)
        setContent { SaveItemScreen(initialPayload = payload, onDone = { finish() }) }
    }
}
```

The screen must render:

- title field
- link or text summary field
- source app/domain preview
- note field
- editable tag chips
- duplicate banner
- fixed bottom CTA

- [ ] **Step 8: Re-run the capture unit tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.feature.capture.*"
```

Expected: PASS.

- [ ] **Step 9: Commit the capture flow**

```powershell
git add .
git commit -m "feat: add share parsing and editable save flow"
```

### Task 5: Implement the home list, filters, and item detail experience

**Files:**
- Create: `app/src/main/java/com/dripin/app/feature/home/HomeViewModel.kt`
- Create: `app/src/main/java/com/dripin/app/feature/home/HomeScreen.kt`
- Create: `app/src/main/java/com/dripin/app/feature/home/SavedItemCard.kt`
- Create: `app/src/main/java/com/dripin/app/feature/detail/DetailViewModel.kt`
- Create: `app/src/main/java/com/dripin/app/feature/detail/DetailScreen.kt`
- Create: `app/src/main/java/com/dripin/app/core/designsystem/component/FilterChipRow.kt`
- Create: `app/src/main/java/com/dripin/app/core/designsystem/component/SectionCard.kt`
- Test: `app/src/test/java/com/dripin/app/feature/home/HomeViewModelTest.kt`
- Test: `app/src/test/java/com/dripin/app/feature/detail/DetailViewModelTest.kt`

- [ ] **Step 1: Write a failing home filter test**

```kotlin
class HomeViewModelTest {
    @Test
    fun filter_state_limits_visible_items() = runTest {
        val repo = FakeSavedItemRepository(
            listOf(
                fakeLinkItem(isRead = false, pushCount = 0),
                fakeTextItem(isRead = true, pushCount = 2),
            )
        )
        val vm = HomeViewModel(repo)

        vm.onReadFilterChanged(ReadFilter.UNREAD)

        assertEquals(1, vm.uiState.value.items.size)
        assertEquals(ContentType.LINK, vm.uiState.value.items.first().contentType)
    }
}
```

- [ ] **Step 2: Run the home ViewModel test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.feature.home.HomeViewModelTest"
```

Expected: FAIL.

- [ ] **Step 3: Implement the home ViewModel and card UI**

```kotlin
data class HomeUiState(
    val filterState: HomeFilterState = HomeFilterState(),
    val items: List<SavedItemSummary> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SavedItemRepository,
) : ViewModel() {
    private val filters = MutableStateFlow(HomeFilterState())
    val uiState = filters.flatMapLatest { filter ->
        repository.observeSummaries(filter).map { HomeUiState(filter, it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
```

Each card must show:

- title
- content type label
- source platform or domain
- save time
- note preview if present

- [ ] **Step 4: Write a failing detail action test**

```kotlin
class DetailViewModelTest {
    @Test
    fun mark_read_updates_repository() = runTest {
        val repo = FakeSavedItemRepository(listOf(fakeLinkItem(id = 1L, isRead = false)))
        val vm = DetailViewModel(itemId = 1L, repository = repo)

        vm.markRead()

        assertTrue(repo.requireItem(1L).isRead)
    }
}
```

- [ ] **Step 5: Run the detail ViewModel test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.feature.detail.DetailViewModelTest"
```

Expected: FAIL.

- [ ] **Step 6: Implement the detail screen**

The detail route must display:

```kotlin
Text(item.title ?: "(无标题)")
item.rawUrl?.let { Text(it) }
item.textContent?.let { Text(it) }
item.imageUri?.let { AsyncImage(model = it, contentDescription = item.title) }
Text("来源: ${item.sourcePlatform ?: item.sourceDomain ?: "未知"}")
Text("推送次数: ${item.pushCount}")
```

and actions:

- mark read/unread
- edit note/title
- edit tags
- open external link

- [ ] **Step 7: Re-run the home/detail tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.feature.home.HomeViewModelTest"
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.feature.detail.DetailViewModelTest"
```

Expected: PASS.

- [ ] **Step 8: Commit browsing screens**

```powershell
git add .
git commit -m "feat: add home list filters and detail screen"
```

### Task 6: Implement settings persistence and daily schedule updates

**Files:**
- Create: `app/src/main/java/com/dripin/app/core/model/RecommendationSortMode.kt`
- Create: `app/src/main/java/com/dripin/app/data/preferences/UserPreferences.kt`
- Create: `app/src/main/java/com/dripin/app/data/preferences/UserPreferencesRepository.kt`
- Create: `app/src/main/java/com/dripin/app/data/repository/SettingsRepository.kt`
- Create: `app/src/main/java/com/dripin/app/feature/settings/SettingsViewModel.kt`
- Modify: `app/src/main/java/com/dripin/app/feature/settings/SettingsScreen.kt`
- Create: `app/src/main/java/com/dripin/app/worker/DailyRecommendationScheduler.kt`
- Test: `app/src/test/java/com/dripin/app/data/preferences/UserPreferencesRepositoryTest.kt`

- [ ] **Step 1: Write a failing settings persistence test**

```kotlin
class UserPreferencesRepositoryTest {
    @Test
    fun saves_notification_time_and_repeat_rule() = runTest {
        val repo = buildTestPreferencesRepository()
        repo.updateNotificationTime(LocalTime.of(21, 0))
        repo.setRepeatUnreadPushedItems(false)

        val prefs = repo.preferences.first()
        assertEquals(LocalTime.of(21, 0), prefs.dailyPushTime)
        assertFalse(prefs.repeatPushedUnreadItems)
    }
}
```

- [ ] **Step 2: Run the settings repository test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.data.preferences.UserPreferencesRepositoryTest"
```

Expected: FAIL.

- [ ] **Step 3: Implement DataStore-backed preferences**

```kotlin
data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val dailyPushTime: LocalTime = LocalTime.of(21, 0),
    val dailyPushCount: Int = 3,
    val repeatPushedUnreadItems: Boolean = true,
    val recommendationSortMode: RecommendationSortMode = RecommendationSortMode.OLDEST_SAVED_FIRST,
)
```

Use `PreferencesDataStore` keys for:

- `notifications_enabled`
- `daily_push_hour`
- `daily_push_minute`
- `daily_push_count`
- `repeat_pushed_unread_items`
- `recommendation_sort_mode`

- [ ] **Step 4: Add a failing schedule orchestration test**

```kotlin
class DailyRecommendationSchedulerTest {
    @Test
    fun schedules_unique_daily_work_after_settings_change() {
        val requests = mutableListOf<OneTimeWorkRequest>()
        val scheduler = FakeDailyRecommendationScheduler(requests)

        scheduler.scheduleNextRun(LocalTime.of(21, 0), ZoneId.of("Asia/Shanghai"))

        assertEquals(1, requests.size)
    }
}
```

- [ ] **Step 5: Run the scheduler test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.worker.DailyRecommendationSchedulerTest"
```

Expected: FAIL.

- [ ] **Step 6: Implement the settings ViewModel, screen, and scheduler**

The settings screen must include:

```kotlin
Switch(checked = state.notificationsEnabled, onCheckedChange = vm::setNotificationsEnabled)
val pickerState = rememberTimePickerState(
    initialHour = state.dailyPushTime.hour,
    initialMinute = state.dailyPushTime.minute,
)
TimeInput(state = pickerState)
Slider(value = state.dailyPushCount.toFloat(), valueRange = 1f..10f, onValueChange = { vm.setDailyPushCount(it.toInt()) })
Switch(checked = state.repeatPushedUnreadItems, onCheckedChange = vm::setRepeatUnreadPushedItems)
```

The scheduler should:

- cancel prior unique work
- compute the next target `ZonedDateTime`
- enqueue a unique `OneTimeWorkRequest`

- [ ] **Step 7: Re-run the settings tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.data.preferences.UserPreferencesRepositoryTest"
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.worker.DailyRecommendationSchedulerTest"
```

Expected: PASS.

- [ ] **Step 8: Commit settings and scheduling**

```powershell
git add .
git commit -m "feat: add settings persistence and daily schedule updates"
```

### Task 7: Implement recommendation batching, notifications, and the Today page

**Files:**
- Create: `app/src/main/java/com/dripin/app/data/local/entity/DailyRecommendationEntity.kt`
- Create: `app/src/main/java/com/dripin/app/data/local/entity/DailyRecommendationItemEntity.kt`
- Create: `app/src/main/java/com/dripin/app/data/local/dao/DailyRecommendationDao.kt`
- Create: `app/src/main/java/com/dripin/app/data/repository/RecommendationRepository.kt`
- Create: `app/src/main/java/com/dripin/app/worker/DailyRecommendationWorker.kt`
- Create: `app/src/main/java/com/dripin/app/worker/RecommendationNotifier.kt`
- Create: `app/src/main/java/com/dripin/app/feature/recommendation/TodayViewModel.kt`
- Modify: `app/src/main/java/com/dripin/app/feature/recommendation/TodayScreen.kt`
- Create: `app/src/main/java/com/dripin/app/feature/recommendation/RecommendationCard.kt`
- Test: `app/src/test/java/com/dripin/app/data/repository/RecommendationRepositoryTest.kt`
- Test: `app/src/test/java/com/dripin/app/feature/recommendation/TodayViewModelTest.kt`

- [ ] **Step 1: Write a failing recommendation selection test**

```kotlin
class RecommendationRepositoryTest {
    @Test
    fun selects_unread_items_and_respects_repeat_rule() = runTest {
        val repo = buildRecommendationRepositoryWithItems(
            fakeLinkItem(id = 1L, isRead = false, pushCount = 0),
            fakeTextItem(id = 2L, isRead = false, pushCount = 3),
            fakeImageItem(id = 3L, isRead = true, pushCount = 0),
        )

        val batch = repo.generateTodayBatch(
            preferences = UserPreferences(dailyPushCount = 2, repeatPushedUnreadItems = false),
            today = LocalDate.parse("2026-04-13"),
        )

        assertEquals(listOf(1L), batch.itemIds)
    }
}
```

- [ ] **Step 2: Run the recommendation repository test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.data.repository.RecommendationRepositoryTest"
```

Expected: FAIL.

- [ ] **Step 3: Implement recommendation persistence and selection**

The repository contract should look like:

```kotlin
data class TodayBatch(val id: Long, val itemIds: List<Long>)

suspend fun generateTodayBatch(preferences: UserPreferences, today: LocalDate): TodayBatch?
suspend fun getTodayBatch(today: LocalDate): TodayBatch?
```

Selection rules:

- unread only
- non-empty payload only
- if repeat is off, require `pushCount == 0`
- sort by selected mode
- take first `dailyPushCount`
- persist batch and update item push metadata

- [ ] **Step 4: Write a failing worker / notifier test**

```kotlin
class DailyRecommendationWorkerTest {
    @Test
    fun worker_skips_notification_when_no_batch_generated() = runTest {
        val worker = buildWorker(generateTodayBatchResult = null)
        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertFalse(worker.notifier.wasNotified)
    }
}
```

- [ ] **Step 5: Run the worker test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.worker.DailyRecommendationWorkerTest"
```

Expected: FAIL.

- [ ] **Step 6: Implement the worker and notification deep link**

The notifier should create channel `daily_recommendation` and build a `PendingIntent` into the Today route:

```kotlin
val intent = Intent(context, MainActivity::class.java).apply {
    action = Intent.ACTION_VIEW
    data = "dripin://today".toUri()
}
```

The worker should:

- load preferences
- skip when notifications are disabled
- skip if today already has a batch
- generate batch
- notify only when at least one item is selected

- [ ] **Step 7: Write a failing Today screen state test**

```kotlin
class TodayViewModelTest {
    @Test
    fun exposes_today_cards_in_rank_order() = runTest {
        val vm = TodayViewModel(fakeRecommendationRepository(todayItemIds = listOf(2L, 1L)))
        assertEquals(listOf(2L, 1L), vm.uiState.value.cards.map { it.id })
    }
}
```

- [ ] **Step 8: Run the Today ViewModel test and verify failure**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.feature.recommendation.TodayViewModelTest"
```

Expected: FAIL.

- [ ] **Step 9: Implement the Today page and card interactions**

Each recommendation card must support:

```kotlin
Button(onClick = onMarkRead) { Text("标记已读") }
item.rawUrl?.let { Button(onClick = { openExternalLink(it) }) { Text("打开原链接") } }
item.imageUri?.let { AsyncImage(model = it, contentDescription = item.title) }
```

The page should visually differ from Home by card emphasis and motion, while staying inside the same theme tokens and typography system.

- [ ] **Step 10: Re-run recommendation tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.data.repository.RecommendationRepositoryTest"
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.worker.DailyRecommendationWorkerTest"
.\gradlew.bat :app:testDebugUnitTest --tests "com.dripin.app.feature.recommendation.TodayViewModelTest"
```

Expected: PASS.

- [ ] **Step 11: Commit recommendation features**

```powershell
git add .
git commit -m "feat: add daily recommendations and today experience"
```

### Task 8: Wire Android share targets, deep links, and end-to-end verification

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/dripin/app/navigation/DripinNavGraph.kt`
- Create: `app/src/main/java/com/dripin/app/HiltDripinTestRunner.kt`
- Create: `app/src/androidTest/java/com/dripin/app/feature/capture/ShareCaptureFlowTest.kt`
- Create: `app/src/androidTest/java/com/dripin/app/feature/recommendation/TodayNotificationDeepLinkTest.kt`

- [ ] **Step 1: Write the failing share-capture integration test**

```kotlin
@RunWith(AndroidJUnit4::class)
class ShareCaptureFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ShareReceiverActivity>()

    @Test
    fun shared_link_prefills_save_form() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://github.com/openai/openai")
        }

        composeRule.activityRule.scenario.onActivity { it.intent = intent }

        composeRule.onNodeWithText("https://github.com/openai/openai").assertExists()
        composeRule.onNodeWithText("保存").assertExists()
    }
}
```

- [ ] **Step 2: Run the share integration test and verify failure**

Run:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.dripin.app.feature.capture.ShareCaptureFlowTest
```

Expected: FAIL.

- [ ] **Step 3: Add the manifest share target and Today deep link**

The manifest should include:

```xml
<activity
    android:name=".feature.capture.ShareReceiverActivity"
    android:exported="true"
    android:theme="@style/Theme.Dripin">
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
        <data android:mimeType="image/*" />
    </intent-filter>
</activity>
```

and the nav graph should handle:

```kotlin
composable(
    route = "today",
    deepLinks = listOf(navDeepLink { uriPattern = "dripin://today" })
) { TodayScreen() }
```

- [ ] **Step 4: Write the failing notification deep-link test**

```kotlin
@RunWith(AndroidJUnit4::class)
class TodayNotificationDeepLinkTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun deep_link_opens_today_route() {
        val intent = Intent(Intent.ACTION_VIEW, "dripin://today".toUri())
        composeRule.activity.startActivity(intent)
        composeRule.onNodeWithText("Today").assertExists()
    }
}
```

- [ ] **Step 5: Run the deep-link test and verify failure**

Run:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.dripin.app.feature.recommendation.TodayNotificationDeepLinkTest
```

Expected: FAIL.

- [ ] **Step 6: Implement the remaining Android wiring and test runner**

Add `HiltDripinTestRunner`:

```kotlin
class HiltDripinTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
```

Also update `app/build.gradle.kts` so instrumentation switches from `androidx.test.runner.AndroidJUnitRunner` to `com.dripin.app.HiltDripinTestRunner`, and add the Hilt Android test processor:

```kotlin
android {
    defaultConfig {
        testInstrumentationRunner = "com.dripin.app.HiltDripinTestRunner"
    }
}

dependencies {
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.59.2")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.59.2")
}
```

Also verify:

- notification channel creation happens at app startup
- share receiver finishes after save or cancel
- Today deep link is idempotent

- [ ] **Step 7: Run the full verification suite**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:assembleDebug
```

Expected: all commands PASS.

- [ ] **Step 8: Manual QA on a device or emulator**

Validate these flows manually:

```text
1. Share a GitHub URL from another app -> Save screen opens with link and source prefilled.
2. Leave title empty on a link -> metadata backfills title when available.
3. Save the same URL twice -> second flow switches to "更新已有内容".
4. Toggle filters on Home -> list updates without navigation glitches.
5. Change daily push time/count in Settings -> next WorkManager request is rescheduled.
6. Trigger worker manually -> notification opens Today page with cards.
7. Mark a Today card as read -> Home and Detail reflect the change.
```

- [ ] **Step 9: Commit the integrated MVP**

```powershell
git add .
git commit -m "feat: ship local-first Dripin MVP"
```
