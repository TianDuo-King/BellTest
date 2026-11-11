# SDD ledger — plan: docs/superpowers/plans/2026-11-10-four-tab-module-ui-plan.md

Setup: Ruling: BellTest is not a Git repository, so no isolated worktree, BASE/HEAD ranges, or task commits can be created — execute in the user-authorized current workspace and preserve unrelated files — cost if wrong: changes cannot be separated or reverted through Git history.

Pre-flight: Task 1 produces `FeaturedFragment`, consumed by Task 2; exact type is `com.example.belltest.featured.FeaturedFragment : Fragment`.

Pre-flight: Task 2 produces the four-tab shell, consumed by Task 3 page call-chain UI; exact menu IDs are `nav_home`, `nav_featured`, `nav_message`, `nav_user`.

Pre-flight: Task 3 preserves the Feed lifecycle consumed by Task 4 device verification; hidden Feed must reach `onStop` through max lifecycle `CREATED`.

Task 1: complete (no git commits available, tests: `./gradlew :module_featured:testDebugUnitTest :module_featured:assembleDebug` → BUILD SUCCESSFUL; RED was unresolved `FeaturedContent`).

Task 2: complete (no git commits available, tests: `./gradlew :app:assembleDebugAndroidTest :app:assembleDebug` → BUILD SUCCESSFUL; RED was unresolved `R.id.nav_featured`).

Task 3: complete (no git commits available, tests: four feature `assembleDebug` tasks plus `:app:assembleDebugAndroidTest` → BUILD SUCCESSFUL; RED was missing feed/message/user module-chain IDs after correcting non-transitive R access).

Task 4: complete (no git commits available; `testDebugUnitTest`, app/debug APK assembly, and AndroidTest APK assembly passed; all four connected tests passed on a Pixel 8/API 37 after waking and unlocking the physical device; screenshots verified top and bottom system-bar clearance on all four tabs).

Verification: main process PID `22121` and remote player PID `22155`; remote Media3 logs reached `state=3` and `Rendered first frame media=big-buck-bunny pid=22155`, proving network loading, decoding, and rendering ran through the `:player` AIDL process.

Ruling: upgraded Espresso `3.5.1` to stable `3.7.0` because the API 37 failure was caused by the removed reflective `InputManager.getInstance()` path; AndroidX Test 3.7.0 replaces it with `getSystemService`. Later `NoActivityResumedException` runs were physical-device screen/gesture interference, confirmed by `SCREEN_ON` and launcher swipe logs, not app failures.

Playback smoothness follow-up: complete. Root cause was a half-finished adapter refresh path that sent every 500 ms playback snapshot through full-item `notifyItemChanged`, repeatedly recreating/rebinding the video Surface. `FeedRenderState` now drives direct ViewHolder-only playback UI updates, static content and Surface binding are isolated, and remote progress callbacks run at 1 Hz because the UI displays whole seconds. RED: `:module_feed:testDebugUnitTest` failed while the incomplete adapter lacked full binding/local update methods. GREEN: all unit tests, APK/test APK builds, and all connected tests pass. Final Pixel 8 evidence: main PID `22146`, player PID `22170`, `state=2` → one `Rendered first frame` → `state=3`; first-frame count stayed at exactly 1 during the observation window.
