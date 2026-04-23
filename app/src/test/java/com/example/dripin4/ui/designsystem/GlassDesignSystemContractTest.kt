package com.example.dripin4.ui.designsystem

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassDesignSystemContractTest {
    private val projectRoot: Path = resolveProjectRoot()
    private val scaffoldScreens = listOf(
        "app/src/main/java/com/example/dripin4/ui/features/inbox/InboxScreen.kt",
        "app/src/main/java/com/example/dripin4/ui/features/capture/CaptureScreen.kt",
        "app/src/main/java/com/example/dripin4/ui/features/detail/DetailScreen.kt",
        "app/src/main/java/com/example/dripin4/ui/features/settings/SettingsScreen.kt",
    )
    private val todayScreenPath = "app/src/main/java/com/example/dripin4/ui/features/today/TodayScreen.kt"
    private val appScaffoldPath = "app/src/main/java/com/example/dripin4/ui/app/DripAppScaffold.kt"
    private val scaffoldComponentsPath = "app/src/main/java/com/example/dripin4/ui/designsystem/components/GlassScaffold.kt"
    private val surfaceComponentsPath = "app/src/main/java/com/example/dripin4/ui/designsystem/components/GlassSurface.kt"
    private val controlsComponentsPath = "app/src/main/java/com/example/dripin4/ui/designsystem/components/GlassControls.kt"
    private val barsComponentsPath = "app/src/main/java/com/example/dripin4/ui/designsystem/components/DripBars.kt"

    @Test
    fun nonTodayFeatureScreens_useGlassScaffoldTemplate() {
        scaffoldScreens.forEach { relativePath ->
            val source = readSource(relativePath)
            assertTrue(
                "Expected ${Paths.get(relativePath).name} to render through GlassScaffold",
                source.contains("GlassScaffold("),
            )
        }
    }

    @Test
    fun todayScreen_keepsLegacyAtmosphereImplementation() {
        val todaySource = readSource(todayScreenPath)
        assertTrue(
            "Expected TodayScreen to keep TodayAtmosphereLayer",
            todaySource.contains("TodayAtmosphereLayer("),
        )
        assertTrue(
            "Expected TodayScreen to keep TodayHeroCard",
            todaySource.contains("TodayHeroCard("),
        )
        assertFalse(
            "Expected TodayScreen to avoid GlassScaffold so it stays the visual source of truth",
            todaySource.contains("GlassScaffold("),
        )
    }

    @Test
    fun todayScreen_isWiredThroughBackdropFromAppScaffold() {
        val appScaffoldSource = readSource(appScaffoldPath)
        assertTrue(
            "Expected DripAppScaffold to pass backdrop into TodayScreen",
            appScaffoldSource.contains("backdrop = todayBackdrop"),
        )
    }

    @Test
    fun topBarActions_openSearchAndNotificationHistoryOverlays() {
        val appScaffoldSource = readSource(appScaffoldPath)
        val overlaySource = readSource("app/src/main/java/com/example/dripin4/ui/app/TopBarOverlays.kt")

        assertTrue(
            "Expected top search button to open the search overlay instead of using an empty callback",
            appScaffoldSource.contains("onSearch = { activeTopBarPanel = TopBarPanel.Search }"),
        )
        assertTrue(
            "Expected top bell button to open the notification history overlay instead of using an empty callback",
            appScaffoldSource.contains("onBell = { activeTopBarPanel = TopBarPanel.NotificationHistory }"),
        )
        assertTrue(
            "Expected DripAppScaffold to render the search overlay",
            appScaffoldSource.contains("TopBarSearchOverlay("),
        )
        assertTrue(
            "Expected DripAppScaffold to render the notification history overlay",
            appScaffoldSource.contains("NotificationHistoryOverlay("),
        )
        assertTrue(
            "Expected search overlay to collapse into a single floating glass search box",
            overlaySource.contains("GlassPanel(") &&
                overlaySource.contains(".testTag(\"search_box\")"),
        )
        assertTrue(
            "Expected search overlay to auto focus and request keyboard visibility when opened",
            overlaySource.contains("FocusRequester()") &&
                overlaySource.contains("keyboardController?.show()"),
        )
        assertTrue(
            "Expected overlay implementation to keep the existing liquid glass surface language",
            overlaySource.contains("GlassPanel(") &&
                overlaySource.contains("AnimatedVisibility(") &&
                overlaySource.contains("overlaySurfaceMotion("),
        )
        assertTrue(
            "Expected top bar overlays to pull the shared glass backdrop so the overlay background blurs instead of dimming",
            overlaySource.contains("LocalGlassBackdrop.current") &&
                overlaySource.contains("drawBackdrop("),
        )
        assertTrue(
            "Expected search query to be wired into actual result derivation instead of staying local to the text field",
            appScaffoldSource.contains("searchSourceItems.searchInboxItems(searchQuery)") &&
                appScaffoldSource.contains("results = searchResults"),
        )
        assertTrue(
            "Expected search results to open detail and dismiss the overlay when selected",
            appScaffoldSource.contains("onOpenResult = { itemId ->") &&
                appScaffoldSource.contains("onOpenDetail(itemId)"),
        )
        assertFalse(
            "Expected top bar overlays to stop using dark alpha scrims behind search and notification surfaces",
            overlaySource.contains("Color.Black.copy(alpha = 0.06f)") ||
                overlaySource.contains("Color.Black.copy(alpha = 0.10f)"),
        )
        assertFalse(
            "Expected notification history overlay to remove the explanatory subtitle under the title",
            overlaySource.contains("查看每日推荐是否真正发送到系统通知栏"),
        )
        assertFalse(
            "Expected notification history cards to remove the secondary detail text",
            overlaySource.contains("text = entry.detail"),
        )
        assertFalse(
            "Expected empty notification history state to remove the secondary explanatory copy",
            overlaySource.contains("下一次生成每日推荐后，这里会显示发送结果。"),
        )
    }

    @Test
    fun appScaffold_usesTodayChromeAcrossAllScreens() {
        val appScaffoldSource = readSource(appScaffoldPath)
        assertTrue(
            "Expected DripAppScaffold background to stay in today mode for every screen",
            appScaffoldSource.contains("todayMode = true"),
        )
        assertTrue(
            "Expected DripAppScaffold top bar to always use today glass chrome",
            appScaffoldSource.contains("todayGlassMode = true"),
        )
        assertTrue(
            "Expected DripAppScaffold chrome to always receive today backdrop",
            appScaffoldSource.contains("todayBackdrop = todayBackdrop"),
        )
        assertFalse(
            "Expected DripAppScaffold to stop branching visual chrome by destination",
            appScaffoldSource.contains("if (isTodayActive)"),
        )
    }

    @Test
    fun nonTodayFeatureScreens_doNotContainHardcodedColorLiterals() {
        val hardcodedColorPattern = Regex("""Color\s*\(\s*0x[0-9A-Fa-f]{6,8}\s*\)|#[0-9A-Fa-f]{6,8}""")

        scaffoldScreens.forEach { relativePath ->
            val source = readSource(relativePath)
            assertFalse(
                "Expected ${Paths.get(relativePath).name} to use tokenized colors only",
                hardcodedColorPattern.containsMatchIn(source),
            )
        }
    }

    @Test
    fun nonTodayFeatureScreens_useTodayMintHeroAccentOnly() {
        scaffoldScreens.forEach { relativePath ->
            val source = readSource(relativePath)
            listOf("GlassHeroAccent.Sky", "GlassHeroAccent.Peach", "GlassHeroAccent.Lilac", "GlassHeroAccent.Slate").forEach { accent ->
                assertFalse(
                    "Expected ${Paths.get(relativePath).name} to stop using $accent and align to Today mint styling",
                    source.contains(accent),
                )
            }
        }
    }

    @Test
    fun designSystem_heroComponents_dropNonTodayAccentFamilies() {
        val source = readSource(scaffoldComponentsPath)
        listOf(
            "AccentSky",
            "AccentSkySoft",
            "AccentPeach",
            "AccentLilac",
            "AuroraBlue",
            "AuroraPurple",
            "AuroraPeach",
            "HeroDispersionPink",
            "HeroDispersionBlue",
            "HeroDispersionPurple",
            "Sky,",
            "Peach,",
            "Lilac,",
            "Slate,",
        ).forEach { marker ->
            assertFalse(
                "Expected design system to keep only Today's green accent family, found $marker",
                source.contains(marker),
            )
        }
    }

    @Test
    fun inboxCaptureAndSettings_dropHeroCards() {
        listOf(
            "app/src/main/java/com/example/dripin4/ui/features/inbox/InboxScreen.kt",
            "app/src/main/java/com/example/dripin4/ui/features/capture/CaptureScreen.kt",
            "app/src/main/java/com/example/dripin4/ui/features/settings/SettingsScreen.kt",
        ).forEach { relativePath ->
            val source = readSource(relativePath)
            assertFalse(
                "Expected ${Paths.get(relativePath).name} to drop the hero card and keep only the Today-style page title header",
                source.contains("GlassHeroHeader("),
            )
        }
    }

    @Test
    fun inboxCardChrome_usesContextPillsAndStructuredFootnotes() {
        val source = readSource("app/src/main/java/com/example/dripin4/ui/features/inbox/InboxScreen.kt")
        assertTrue(
            "Expected Inbox cards to render their top context through GlassInfoPill",
            source.contains("GlassInfoPill("),
        )
        assertTrue(
            "Expected Inbox cards to promote titles to titleLarge for clearer hierarchy",
            source.contains("MaterialTheme.typography.titleLarge"),
        )
        assertTrue(
            "Expected Inbox cards to expose a structured status group beneath the meta line",
            source.contains("inbox_status_group_"),
        )
        assertTrue(
            "Expected Inbox cards to collapse the summary panel when there is no text",
            source.contains("summaryText?.let"),
        )
    }

    @Test
    fun sharedNeutralCards_useTodayRenderingStack() {
        val source = readSource(surfaceComponentsPath)
        assertTrue(
            "Expected shared neutral glass cards to use Today's vibrancy stack",
            source.contains("vibrancy()"),
        )
        assertTrue(
            "Expected shared neutral glass cards to use Today's lens refraction",
            source.contains("lens("),
        )
    }

    @Test
    fun settingsSwitch_usesBlackCheckedTrack() {
        val source = readSource(controlsComponentsPath)
        assertTrue(
            "Expected Settings switch to use black for the checked track fill",
            source.contains(".background(DripColors.Ink, trackShape)"),
        )
    }

    @Test
    fun settingsScreen_usesGlassSwitchComponent() {
        val settingsSource = readSource("app/src/main/java/com/example/dripin4/ui/features/settings/SettingsScreen.kt")
        val controlsSource = readSource(controlsComponentsPath)
        assertTrue(
            "Expected Settings screen to render toggles through GlassSwitch",
            settingsSource.contains("GlassSwitch("),
        )
        assertFalse(
            "Expected Settings screen to stop using the raw Material3 Switch",
            Regex("""(?m)^\s*Switch\(""").containsMatchIn(settingsSource),
        )
        assertTrue(
            "Expected the design system to provide a GlassSwitch component",
            controlsSource.contains("fun GlassSwitch("),
        )
    }

    @Test
    fun settingsSortPreference_usesExplicitOptionChips() {
        val settingsSource = readSource("app/src/main/java/com/example/dripin4/ui/features/settings/SettingsScreen.kt")

        assertTrue(
            "Expected Settings screen to render the sort preference as a dedicated option group",
            settingsSource.contains("SortPreferenceRow("),
        )
        assertTrue(
            "Expected Settings sort preference to use chip choices instead of a switch",
            settingsSource.contains("GlassChipRow"),
        )
        assertTrue(
            "Expected Settings sort preference to expose the newest-first option copy",
            settingsSource.contains("\"最新优先\""),
        )
        assertTrue(
            "Expected Settings sort preference to expose the oldest-first option copy",
            settingsSource.contains("\"最早优先\""),
        )
        assertFalse(
            "Expected Settings screen to remove the redundant current-sort summary row",
            settingsSource.contains("当前排序"),
        )
    }

    @Test
    fun glassSwitch_isCompactAndReadableWhenUnchecked() {
        val controlsSource = readSource(controlsComponentsPath)
        assertTrue(
            "Expected GlassSwitch to use a more compact track size",
            controlsSource.contains(".size(width = 58.dp, height = 34.dp)"),
        )
        assertTrue(
            "Expected GlassSwitch thumb to shrink with the track",
            controlsSource.contains(".size(24.dp)"),
        )
        assertTrue(
            "Expected unchecked GlassSwitch to raise glass visibility",
            controlsSource.contains("fillAlpha = 0.56f"),
        )
    }

    @Test
    fun nonTodayScaffold_addsSameBackdropAtmosphereLayerAsToday() {
        val scaffoldSource = readSource(scaffoldComponentsPath)
        val surfaceSource = readSource(surfaceComponentsPath)
        assertTrue(
            "Expected GlassScaffold to inject the shared backdrop atmosphere so top bar and dock match Today",
            scaffoldSource.contains("GlassBackdropAtmosphereLayer("),
        )
        assertTrue(
            "Expected the shared backdrop atmosphere layer to live in the design system",
            surfaceSource.contains("fun GlassBackdropAtmosphereLayer("),
        )
    }

    @Test
    fun filterChips_shareSelectedTintImplementationWithBottomDock() {
        val controlsSource = readSource(controlsComponentsPath)
        val barsSource = readSource(barsComponentsPath)
        assertTrue(
            "Expected GlassControls to define a shared dock-selected tint helper for chips",
            controlsSource.contains("fun Modifier.dockSelectedTint("),
        )
        assertTrue(
            "Expected GlassChip to use the dock-selected tint helper",
            controlsSource.contains(".dockSelectedTint(") &&
                controlsSource.contains("selected = selected"),
        )
        assertTrue(
            "Expected DripBottomNavEntry to use the same dock-selected tint helper",
            barsSource.contains(".dockSelectedTint(selected = selected"),
        )
    }

    @Test
    fun horizontalChipRows_provideEdgeBreathingRoomForGlassBloom() {
        val controlsSource = readSource(controlsComponentsPath)
        val inboxSource = readSource("app/src/main/java/com/example/dripin4/ui/features/inbox/InboxScreen.kt")
        val captureSource = readSource("app/src/main/java/com/example/dripin4/ui/features/capture/CaptureScreen.kt")
        val detailSource = readSource("app/src/main/java/com/example/dripin4/ui/features/detail/DetailScreen.kt")

        assertTrue(
            "Expected design system to provide a shared chip row that prevents scroll-edge bloom clipping",
            controlsSource.contains("fun GlassChipRow("),
        )
        assertTrue(
            "Expected GlassChipRow to keep horizontal scrolling in the shared design-system component",
            controlsSource.contains(".horizontalScroll(rememberScrollState())"),
        )
        assertTrue(
            "Expected GlassChipRow to give chip bloom room at both scroll edges",
            controlsSource.contains(".padding(horizontal = DripSpacing.XSmall)"),
        )
        assertTrue(
            "Expected selected chips to suppress external bloom so aligned filter rows do not create vertical shadow seams",
            controlsSource.contains("bloomSpread = 0.dp"),
        )
        assertTrue(
            "Expected the shared tint helper to keep configurable bloom for dock and primary controls",
            controlsSource.contains("bloomSpread: Dp = 10.dp"),
        )
        assertTrue(
            "Expected Inbox filters to use the shared chip row instead of a raw horizontally scrolling Row",
            inboxSource.contains("GlassChipRow"),
        )
        assertTrue(
            "Expected Capture chip groups to use the shared chip row instead of a raw horizontally scrolling Row",
            captureSource.contains("GlassChipRow"),
        )
        assertTrue(
            "Expected Detail chip groups to use the shared chip row instead of a raw horizontally scrolling Row",
            detailSource.contains("GlassChipRow"),
        )
    }

    @Test
    fun primaryButtons_shareSelectedTintImplementationWithDock() {
        val controlsSource = readSource(controlsComponentsPath)
        assertTrue(
            "Expected GlassButton primary actions to use the shared dock-selected tint helper",
            controlsSource.contains(".dockSelectedTint(selected = isPrimaryStyle"),
        )
        assertFalse(
            "Expected GlassButton to stop using the old FrostLime primary fill",
            controlsSource.contains("DripColors.FrostLime.copy(alpha = 0.74f)"),
        )
    }

    private fun readSource(relativePath: String): String {
        val path = projectRoot.resolve(relativePath)
        assertTrue("Missing source file: $relativePath", path.exists())
        return path.toFile().readText()
    }

    private fun resolveProjectRoot(): Path {
        var current = Paths.get("").toAbsolutePath()
        while (true) {
            val settingsFile = current.resolve("settings.gradle.kts")
            val appSrcDir = current.resolve("app").resolve("src")
            if (settingsFile.exists() && appSrcDir.exists() && appSrcDir.isDirectory()) {
                return current
            }
            val parent = current.parent ?: error("Unable to resolve project root from ${Paths.get("").toAbsolutePath()}")
            current = parent
        }
    }
}
