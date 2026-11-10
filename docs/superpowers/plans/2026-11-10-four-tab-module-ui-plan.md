# BellTest Four-Tab Module UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship visible 首页、精选、消息、我的 module pages while keeping real video playback in the AIDL `:player` process and adapting every page to system navigation insets.

**Architecture:** `app` owns only the four-tab shell. `module_feed` owns the remote-video UI, `module_featured` reads shared `lib_model` data, and message/user remain isolated feature modules. `lib_base` supplies reusable inset handling; the app shell owns bottom navigation bar insets.

**Tech Stack:** Android XML, ViewBinding, FragmentManager, WindowInsetsCompat, ViewPager2, AIDL, Media3 ExoPlayer 1.11.1.

**Spec:** `docs/superpowers/specs/2026-11-10-four-tab-module-ui-design.md`

## Global Constraints

- Tabs and labels are exactly `首页 / 精选 / 消息 / 我的`.
- No `findViewById`; every page uses ViewBinding.
- Feed video bytes, parsing, MediaCodec decoding and playback remain in `com.example.belltest:player`.
- Hidden top-level fragments use maximum lifecycle `CREATED`.
- All four pages and the bottom navigation consume the relevant system-bar/display-cutout insets.
- minSdk 24, compile/target SDK 37, Java 11.

## Review Focus

- Cold start must create exactly one FeedFragment and one player binding.
- Switching away from Feed must stop it and release the remote player without killing the app.
- Rapid tab changes must not add duplicate top-level fragments.
- Status bar/cutout inset must be applied once, not accumulate after view recreation.
- A failed video URL must leave the four-tab shell usable and expose retry UI.

---

### Task 1: Featured Module and Shared Catalog UI

**Files:**
- Create: `module_featured/build.gradle.kts`
- Create: `module_featured/src/main/java/com/example/belltest/featured/FeaturedFragment.kt`
- Create: `module_featured/src/main/java/com/example/belltest/featured/FeaturedVideoAdapter.kt`
- Create: `module_featured/src/main/res/layout/featured_fragment.xml`
- Create: `module_featured/src/main/res/layout/featured_item_video.xml`
- Modify: `settings.gradle.kts`

**Interfaces:**
- Consumes: `DemoVideoCatalog.items: List<VideoItem>` from `lib_model`.
- Produces: public `FeaturedFragment : Fragment` for the app shell.

- [ ] Add a module configuration test by running `./gradlew projects` and record the initial absence of `:module_featured`.
- [ ] Register `module_featured` with ViewBinding and dependencies on `lib_base`, `lib_resources`, and `lib_model`.
- [ ] Implement the Fragment and RecyclerView adapter; show a module-call banner and one card per catalog item.
- [ ] Run `./gradlew :module_featured:assembleDebug`; expect `BUILD SUCCESSFUL`.

### Task 2: Four-Tab Application Shell

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/java/com/example/belltest/MainActivity.kt`
- Modify: `app/src/main/res/menu/app_bottom_navigation.xml`
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/androidTest/java/com/example/belltest/MainNavigationTest.kt`

**Interfaces:**
- Consumes: `FeedFragment`, `FeaturedFragment`, `MessageFragment`, `UserFragment`.
- Produces: one top-level Fragment instance per tab and `CREATED` lifecycle for hidden pages.

- [ ] Change the navigation instrumentation test to assert four exact menu IDs and one FeedFragment after cold start.
- [ ] Run the test to observe failure while the old five-tab menu remains.
- [ ] Replace social/publish mappings with featured and keep show/hide + max lifecycle behavior.
- [ ] Run `./gradlew :app:assembleDebugAndroidTest`; expect compilation success.

### Task 3: Visible Module Call Chains and Insets

**Files:**
- Modify: `module_feed/src/main/res/layout/feed_fragment.xml`
- Modify: `module_message/src/main/res/layout/message_fragment.xml`
- Modify: `module_user/src/main/res/layout/user_fragment.xml`
- Modify: corresponding Fragment classes only if new bindings/inset listeners are needed.

**Interfaces:**
- Consumes: `View.applySystemBarsPadding(includeTop, includeBottom)`.
- Produces: visible module path text on all four pages and safe system-bar spacing.

- [ ] Add stable view IDs for each module-call banner so UI tests can assert them.
- [ ] Add `首页调用链`, `精选调用链`, `消息调用链`, and `我的调用链` text to the four pages.
- [ ] Verify top insets are owned by each page and bottom inset only by the app navigation.
- [ ] Build all four feature modules.

### Task 4: Real Video Visibility and Device Verification

**Files:**
- Modify if needed: `module_feed/.../FeedFragment.kt`, `FeedPagerAdapter.kt`
- Modify if needed: `service_player/.../RemoteVideoPlayerService.kt`
- Test: `app/src/androidTest/.../RemotePlayerProcessTest.kt`

**Interfaces:**
- Consumes: existing `RemotePlayerClient.prepare/attachSurface` and `PlaybackSnapshot` callbacks.
- Produces: visible frame plus remote PID/state diagnostics.

- [ ] Run unit tests and assemble: `./gradlew testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`.
- [ ] Run Pixel 8 tests: `./gradlew :app:connectedDebugAndroidTest`; expect all tests pass.
- [ ] Install/launch APK and verify separate main/player PIDs.
- [ ] Inspect remote PID logs for MediaCodec/READY or surfaced source error; fix only evidence-backed failures.
- [ ] Switch through all four tabs and confirm player release/rebind and inset layout visually.
