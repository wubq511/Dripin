# UI MVP Backend Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Connect the migrated prototype UI to the existing MVP repositories, persistence, share-entry flow, settings, and daily recommendation logic without replacing the prototype visual system.

**Architecture:** Keep the prototype `ui/*` design system and screen structure as the visual source of truth. Add a runtime bridge layer that maps existing MVP domain/viewmodel state into prototype-facing screen state, then wire `MainActivity` and `ShareReceiverActivity` to the runtime host so the migrated UI drives the old repositories instead of static demo data.

**Tech Stack:** Android Compose, existing Room repositories, existing ViewModels, DataStore settings, WorkManager scheduling, prototype `ui/*` design system, JUnit4, Compose UI tests.

---

### Task 1: Lock mapping behavior with tests

**Files:**
- Create: `app/src/test/java/com/example/dripin4/ui/app/DripRuntimeMappersTest.kt`
- Modify: `app/src/main/java/com/example/dripin4/ui/app/DripRuntimeMappers.kt`

- [ ] **Step 1: Write failing mapper tests**

```kotlin
@Test
fun saved_link_maps_into_inbox_card_content() {
    val item = savedItem(
        id = 42L,
        contentType = ContentType.LINK,
        title = "OpenAI release",
        note = "Need to revisit",
        sourcePlatform = "GitHub",
        topicCategory = "设计",
        createdAt = Instant.parse("2026-04-21T12:00:00Z"),
    )

    val mapped = item.toInboxItemUi(now = Instant.parse("2026-04-21T12:08:00Z"), zoneId = ZoneOffset.UTC)

    assertEquals("42", mapped.id)
    assertEquals("OpenAI release", mapped.title)
    assertEquals("Need to revisit", mapped.note)
    assertEquals("GitHub", mapped.source)
    assertEquals("8 分钟前", mapped.time)
    assertEquals("设计", mapped.tag)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.dripin4.ui.app.DripRuntimeMappersTest"`
Expected: FAIL because runtime mapper helpers do not exist yet.

- [ ] **Step 3: Implement minimal runtime mapper helpers**

```kotlin
internal fun SavedItemEntity.toInboxItemUi(now: Instant, zoneId: ZoneId): InboxItemUi { ... }
internal fun TodayCardModel.toTodayItemUi(now: LocalDate): TodayItemUi { ... }
internal fun SettingsUiState.toPrototypeSettingsUi(): PrototypeSettingsUi { ... }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.dripin4.ui.app.DripRuntimeMappersTest"`
Expected: PASS

### Task 2: Build runtime host around existing MVP logic

**Files:**
- Create: `app/src/main/java/com/example/dripin4/ui/app/DripRuntimeApp.kt`
- Modify: `app/src/main/java/com/example/dripin4/ui/app/DripAppState.kt`
- Modify: `app/src/main/java/com/example/dripin4/ui/app/DripAppScaffold.kt`
- Modify: `app/src/main/java/com/dripin/app/MainActivity.kt`
- Modify: `app/src/main/java/com/dripin/app/feature/capture/ShareReceiverActivity.kt`

- [ ] **Step 1: Write failing integration-state tests**

```kotlin
@Test
fun default_runtime_capture_session_starts_as_manual_link_entry() { ... }

@Test
fun share_capture_completion_returns_to_inbox_or_finishes_host() { ... }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.dripin4.ui.app.*"`
Expected: FAIL because runtime host/session behavior is not implemented.

- [ ] **Step 3: Implement runtime host and state synchronization**

```kotlin
@Composable
fun DripRuntimeApp(
    repository: SavedItemStore,
    settingsRepository: SettingsRepository,
    recommendationRepository: RecommendationStore,
    launchIntent: Intent? = null,
    initialCapturePayload: IncomingSharePayload? = null,
    onCaptureFinished: (() -> Unit)? = null,
) { ... }
```

- [ ] **Step 4: Wire activities to the runtime host**

```kotlin
setContent {
    DripRuntimeApp(
        repository = repository,
        settingsRepository = settingsRepository,
        recommendationRepository = recommendationRepository,
        launchIntent = launchIntentState.value,
    )
}
```

- [ ] **Step 5: Run focused tests**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.dripin4.ui.app.*"`
Expected: PASS

### Task 3: Restore capture, detail, today, and settings functionality in prototype screens

**Files:**
- Modify: `app/src/main/java/com/example/dripin4/ui/features/capture/CaptureScreen.kt`
- Modify: `app/src/main/java/com/example/dripin4/ui/features/detail/DetailScreen.kt`
- Modify: `app/src/main/java/com/example/dripin4/ui/features/today/TodayScreen.kt`
- Modify: `app/src/main/java/com/example/dripin4/ui/features/settings/SettingsScreen.kt`

- [ ] **Step 1: Write failing behavior tests for prototype-facing runtime state**

```kotlin
@Test
fun capture_image_mode_exposes_picker_and_selected_images() { ... }

@Test
fun detail_primary_action_opens_link_only_when_url_exists() { ... }

@Test
fun settings_time_row_uses_runtime_push_time() { ... }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.dripin4.ui.*"`
Expected: FAIL because prototype runtime behavior is still static.

- [ ] **Step 3: Add the minimal runtime UI extensions needed for MVP features**

```kotlin
if (capture.contentType == ContentType.IMAGE) { ... }
if (detail.editorVisible) { ... }
UtilityRow(title = ..., subtitle = appState.settingsReminderSubtitle, onClick = appState::openTimePicker)
```

- [ ] **Step 4: Run related tests**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.dripin4.ui.*"`
Expected: PASS

### Task 4: Full verification of compile, unit tests, and device flows

**Files:**
- Verify only

- [ ] **Step 1: Run compile verification**

Run: `./gradlew :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run unit tests**

Run: `./gradlew :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Run connected tests for migrated prototype flows**

Run: `./gradlew '-Pandroid.testInstrumentationRunnerArguments.class=com.example.dripin4.MainActivityUiTest,com.example.dripin4.ui.app.DripNavigationUiTest,com.example.dripin4.ui.features.CaptureUiTest,com.example.dripin4.ui.features.InboxUiTest' :app:connectedDebugAndroidTest`
Expected: no new migration-only failures; investigate any failures against source project parity.

- [ ] **Step 4: Manually verify critical MVP flows on device**

Run:
- `adb shell am start -n com.dripin.app/.MainActivity`
- `adb shell am start -n com.dripin.app/.feature.capture.ShareReceiverActivity --es android.intent.extra.TEXT "https://example.com/article"`

Expected:
- Inbox displays persisted data
- Today displays generated recommendations
- Capture saves into Room
- Detail opens original links and saves edits
- Settings updates persisted preferences and scheduling
