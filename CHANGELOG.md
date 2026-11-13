# Changelog

All notable changes to NexFusion are documented here. Releases are dispatched automatically on
every push to `main` for the version in `app/build.gradle.kts` (`versionName` with `versionCode`
substituted) when that version has no release yet. The section matching the release tag becomes
the release notes, falling back to generated commit notes when no section exists.

## 4.3.4

- **Cache Cap & Background Patch Persistence:**
  * Capped maximum cache size to 5GB in `CacheManager` (`MAX_CACHE_SIZE_BYTES`), preventing aggressive OS storage quota sweeps from evicting cached artifacts.
  * Increased stale temporary file and expired update package retention thresholds from 10/15 minutes to 24 hours.
  * Added Device Protected Storage (DPS) fallback for bundle registry and DexKit fingerprint cache, ensuring hooked applications like Gboard running in Direct Boot or background wakeups reliably retain their patches.
  * Guarded `loadBundle()` against deleting bundle APKs if remote IPC temporarily times out.
  * Added an atomic process-level execution guard in `MainHook` to prevent duplicate patch session initialization.
  * Verified cold-start-only initialization toast popup behavior, eliminating repeated popups during background operations.

## 4.3.3

- **Build, CI & Automation Maintenance:**
  * Increased Gradle memory heap to 4GB and unified APK compilation tasks for significantly faster CI builds.
  * Added duplicate push detection for `beta` branch to avoid redundant CI runs without cancellation errors.
  * Hardened nightly `autobuild` release workflow with in-place asset updates, preventing GitHub release race condition collisions.
  * Added authentication guard to Crowdin localization workflows to skip gracefully when API secrets are not configured.

## 4.3.2

- **Idle Hook Persistence & App Storage Hardening:**
  * Migrated target application bundle APK and remapped `.dex` storage to `filesDir/nexfusion` (owned permanently by the app UID), preventing Android OS idle cache trimming (`pm trim-caches`), low-memory killer sweeps, and deep doze maintenance from evicting active patch bundles.
  * Added persistent local `SharedPreferences` bundle registry caching in hooked applications. If the Xposed/LSPosed remote binder drops, sleeps, or times out during deep doze, hooked applications seamlessly fall back to local cached bundle configurations and load without unhooking.
  * Protected `PatchExecutor` user preference resolution with local SharedPreferences caching, ensuring custom patch settings persist across idle waking cycles.
  * Softened `CacheManager` pruning by extending stale temp timeout to 10 minutes and strictly restricting storage quota eviction to transient `cacheDir` artifacts, shielding ART bytecode and DexKit cache directories from eviction.

## 4.3.1

- **Patch Bundle Lifecycle & Synchronization Fixes:**
  * Fixed an issue where updating patch bundles in-app glitched and reverted due to system package synchronization incorrectly downgrading newer bundle versions.
  * Fixed bundle deletion/uninstallation so uninstalled bundles are cleanly removed and not inadvertently revived by background system package synchronization.
  * Added uninstalled bundle tombstoning in local preferences to prevent unwanted resurrection of uninstalled system packages.
  * Added automated orphan bundle artifact pruning in manager storage to eliminate stale bundle accumulation and maintain a zero persistent storage overhead.
  * Offloaded bundle uninstallation tasks from the main thread to prevent UI frame stalls during deletion and disk operations.

## 4.3.0

- **libxposed API 101 & 102 Compatibility:**
  * Added compatibility supporting both **libxposed API 101** and **API 102** (`minApiVersion=101, targetApiVersion=102`).
  * Runtime hooking bridge isolated into `io.github.nexfusion.compat.*`.
  * Configured `XposedDexRemapper` with smali bytecode remapping for backward compatibility.

- **In-App Build Variant Selection (Release vs Debug):**
  * Added **Build variant** switcher under **Settings → Updates** allowing users to switch between **Release** (production) and **Debug** (development) APKs directly in-app without visiting GitHub.
  * Enhanced `AppUpdateChecker` and `HomeScreen` with variant-aware asset resolution and update cards.

- **Sophisticated Color System & Design Tokens:**
  * Completely redesigned color system with 6 harmonized, perceptually-tuned accent seeds: Default Indigo, Violet, Emerald, Amber, Rose, and Ocean.
  * Balanced tonal palettes across light and dark modes for both Material 3 and Miuix design systems.
  * Introduced `DesignTokens.kt` for centralized spacing, rounded corner geometry, typography scales, opacity levels, and fluid motion animations.
  * Unified `PrefKeys` and streamlined preference state handling.

- **Bundle Version Tracking & Manager Synchronization:**
  * Fixed installed source version desynchronization when importing `.nxb` bundles manually or updating via APK.
  * Corrected manager UI to auto-sync updated system package APKs and eliminated spurious downgrade prompts when newer beta/manual builds are installed.
  * Corrected GitHub release comparator sorting for beta pre-releases versus stable releases.
  * Decoupled system package syncing to eliminate UI blocking and recursion.

- **Stealth & Anti-Detection Hardening:**
  * AndroidManifest.xml: added stealth attributes (`android:backupInForeground="false"`, `android:allowBackup="false"`, `android:fullBackupContent="false"`, `android:hasFragileUserData="false"`, `android:directBootAware="false"`, `android:extractNativeLibs="false"`) and removed explicit package queries in favor of runtime hiding.
  * `StealthCloak.kt`: comprehensive package hiding via `ApplicationPackageManager` hooks (`getPackageInfo`, `getApplicationInfo`, `getInstalledPackages`, `getInstalledApplications`, `queryIntentActivities`, `getPackagesForUid`).

- **Smart Zero-Storage Cache Management & Quota Protection:**
  * Implemented an intelligent, battery-friendly `CacheManager` designed for minimal storage footprint and zero persistent storage accumulation.
  * Added proactive sanitization that automatically detects and purges rogue nested directories, orphan temporary containers, and expired update APKs.
  * Enhanced Settings with real-time cache occupancy reporting and comprehensive 1-tap cache purging across both temporary files and bytecode indexes.
  * Integrated zero-overhead periodic cache pruning into background update checks.

- **CI & Build Optimization:**
  * Skipped redundant unit tests on `main` branch pushes to conserve GitHub Actions resources while preserving complete unit test validation on `beta` and pull requests.

## 4.2.8

- VLC Support & Target Discovery:
  * Added package queries and discovery metadata for VLC (`org.videolan.vlc`).
  * Removed MX Player (`com.mxtech.videoplayer.ad`) from discovery and bundle targets.
  * Updated supported applications matrix across video, audio, social, and system utilities.
- Community Translations via Crowdin:
  * Integrated automated Crowdin translation configuration (`crowdin.yml`) for multi-language community localization.
  * Added automated GitHub Actions workflows for source upload (`crowdin_push.yml`) and community translation sync (`crowdin_pull.yml`).

## 4.2.7

- Stremio & MX Player Support & Target Discovery:
  * Added package queries and discovery metadata for Stremio (`com.stremio.one`) and MX Player (`com.mxtech.videoplayer.ad`).
  * Updated supported applications matrix to 22 targets across video, audio, social, and system utilities.

## 4.2.5

- Application Exclusion & Debloated App Optimization:
  * Introduced universal App Exclusion controls: users can exclude any supported application from patching directly from the Home screen or app detail settings.
  * Excluded applications will never trigger scope approval prompts or notifications, and hooks are bypassed at process startup with zero overhead.
  * Automatically inspects application enabled state on the device; disabled or debloated applications are treated as uninstalled and will not trigger false "missing from module scope" warnings or scope prompts.
  * Added a dedicated "Excluded" filter chip with live counts and an organized "Excluded Applications" section on the Home screen.
- Scope Approval Notification Refinement:
  * Streamlined the LSPosed scope approval snackbar from a multi-line raw package identifier list to a clean, formatted confirmation ("Added YouTube to module scope" or "Added 14 apps to module scope"), eliminating notification bar clutter.
- Fluid Depth Transitions & HyperOS Performance:
  * Re-engineered screen navigation with native depth elevation and scale transitions (`scaleIn` / `scaleOut` + `fadeIn` / `fadeOut`), delivering smooth, tear-free navigation across gesture swipes (left or right edge), 3-button navigation, and top back icons.
  * Directional tab sliding synchronized with navigation bar selection for peer tabs.
  * Disabled floating navigation and translucent blur by default to eliminate navigation frame drops on Xiaomi HyperOS.
  * Restricted floating navigation exclusively to Miuix mode; Material 3 mode now strictly renders standard Material 3 Scaffold navigation without floating bar overhead.
- Anti-Detection & Stealth Hiding:
  * Introduced `StealthCloak`: automatically intercepts `ApplicationPackageManager` in all hooked target processes to mask `io.github.nexfusion` from package queries (`getPackageInfo`, `getApplicationInfo`, `getInstalledPackages`, `getInstalledApplications`, `queryIntentActivities`, and `getPackagesForUid`), completely shielding the module from in-app Google Play Services and security scanners.
  * Decoupled `de.robv.android.xposed.category.MODULE_SETTINGS` into a dedicated `<activity-alias>` (`ModuleSettingsAlias`) so it can be disabled when hiding the launcher icon, preventing package scanners searching for Xposed categories from detecting NexFusion.

- Bundle Card & UI Refinement:
  * Redesigned bundle cards in the Bundles tab to be compact, professional, and visually refined.
  * Eliminated redundant app enumeration by prioritizing top flagship applications on a single line.
  * Added smooth expandable chip flow with an interactive count badge (`+X more` / `Show less`) and animated transitions.
  * Enhanced BundleManager to automatically sync manifest descriptions from on-disk bundle APKs.
- Update Detection & Stability:
  * Resolved beta update loops and suppressed false downgrade warnings for preview and nightly releases.
  * Hardened patch source synchronization and release parsing across update channels.

## 4.2.3

- Beta Updates & Lifecycle Fixes:
  * Fixed beta update detection using commit hashes and timestamps to eliminate false update cards and settings badges.
  * Normalized bundle semantic version parsing on beta release channels.
  * Hardened patch session cache key with bundle content hash to guarantee instant cache invalidation upon bundle updates.

## 4.2.2

- Process Management & Target Lifecycle:
  * Enhanced target app process lifecycle management: when root privileges are absent or unavailable, automatically navigates to the target application's system App Info settings with a guided prompt to perform a complete force-stop restart so updated patches and hooks apply cleanly.
  * Preserved silent background root termination when root permissions are granted.
- CI/CD & Distribution Enhancements:
  * Streamlined standalone APK distribution: automated standalone release APK publishing on push events without packaging in zip archives.
  * Added direct release download notices in CI workflow step summaries.
- Visual Architecture & Navigational Solid Fallbacks:
  * Hardened non-translucent navigation bar container rendering, ensuring 100% solid opacity across themes and preventing any scroll bleed-through.

## 4.2.1

- Ecosystem Compatibility & Performance:
  * Synchronized compatibility bindings and patch bundle definitions for release 4.2.1.
  * Enhanced update channel routing and release bundle caching.

## 4.2.0

- Update Channel Selection (Stable & Beta):
  * Added dedicated "Update channel" preference setting under the Updates section in Settings.
  * Allows users to seamlessly switch between Stable (official production releases) and Beta (pre-release preview builds and patch bundles).
  * Implemented for both Miuix and Material 3 design systems with interactive radio selection dialogs.
  * Integrated channel-aware release parsing across GitHub API and Atom fallback feeds, enabling automated detection and in-app updating of beta pre-release builds and standalone APK assets.
- Background Workers & Source Update Hardening:
  * Wired update channel preferences directly into periodic background update checks and source synchronization routines.
  * Preserved full inter-version bundle cross-compatibility across stable and beta release channels.

## 4.1.0

- Liquid Floating Navigation Bar:
  * Implemented floating pill navigation container with AGSL runtime shaders:
    - Real-time backdrop blur and rounded-rectangle lens refraction with chromatic aberration and depth effect.
    - Specular light bloom highlight driven by device physical tilt via sensor gravity tracking.
    - Interactive touch bloom tracking gesture interactions.
    - Fluid damped spring drag animation across tabs with velocity squish/stretch and rubber-band boundary physics.
  * Supported across both official Miuix and standard Material 3 design systems.
  * Added responsive centering, safe window insets padding, and real-time update badge counts.
  * Added Settings controls for floating navigation bar and translucent glass effect.
- Solid Fallback & Non-Translucent Mode:
  * Enforced 100% solid, fully opaque surface (`alpha = 1.0f`) on the floating bar container and active indicator pill when translucent mode is disabled, eliminating any bleed-through of underlying scrolled content or buttons.
  * Optimized rendering performance by completely bypassing background shader capture when translucent glass effect is disabled.

## 4.0.0

- Inaugural NexFusion Rebranding & Package Migration:
  * Renamed the project to NexFusion across the manager application, patcher engine, build logic, and automation workflows.
  * Migrated all internal packages and components directly to `io.github.nexfusion.*` with application package ID `io.github.nexfusion`.
  * Updated bundle plugin to `nexfusion.bundle` and Maven coordinates to `io.github.nexfusion`.
  * Refined visual identity with fluid Material design and modern theme token architecture.
- Release Artifact Distribution:
  * Prioritized production release builds (`NexFusion-<tag>.apk`) over standalone debug builds in update resolution.
  * Cleaned up issue templates, aligned YouTube debugging instructions to Morphe settings, and corrected example references.
- Unified Runtime & Compatibility Architecture:
  * Full integration with the universal cross-compatibility engine, allowing seamless inter-version bundle installations, updates, and rollbacks.
  * Preserved full user preference state, patch provenance tracking, and dynamic bytecode remapping.
- Zero-Trace Architecture & Cleanup:
  * Completely eliminated all legacy project identifiers, outdated branch tracking, and temporary build metadata across the codebase.

## 3.6.3


- Universal Cross-Compatibility System:
  * Removed rigid bundle version and series installation locks. Any version of the manager can install, update, or downgrade patch bundles across versions directly without artificial blockage.
  * Transformed version mismatch enforcement into a non-blocking advisory notification system: bundles targeting newer manager versions display helpful recommendation warnings rather than rejecting installation.
- Direct Bundle Updates & Downgrades:
  * Full direct lifecycle control across bundle releases with dedicated update, downgrade, and reinstall actions.
- Uninstalled Applications Visibility Preference:
  * Added "Hide uninstalled applications" setting under General settings and a 1-tap visibility toggle on the Home screen header.
  * When enabled, supported but uninstalled catalog applications are hidden, focusing the interface strictly on applications installed on the device.
- Bundle & Patch Suite Synchronization:
  * Synchronized catalog and patch descriptions with NexFusion 3.6.3, including Instagram HDR brightness boost controls, Facebook suggested group suppression, and streaming core extension refinements.

## 3.6.2

- Application Catalog & Maintenance:
  * Decommissioned Spotify from application discovery catalog and manifest queries.
- Gboard Preference Persistence:
  * Synchronized catalog and patch preferences with NexFusion 3.6.2, resolving emoji fast access row user toggle persistence.
- Bundle Synchronization:
  * Synchronized compatibility engine with NexFusion 3.6.2.

## 3.6.1

- Application Catalog & Discovery Expansion:
  * Added full support for Google Maps (`com.google.android.apps.maps`) to the application discovery catalog and manifest queries.
  * Added Kick (`com.kick.mobile`) and Twitch (`tv.twitch.android.app`) to the catalog and manifest queries.
  * Decommissioned MiXplorer from default queries and catalog.
- Package Resolution & Scope Accuracy:
  * Optimized package presence detection using multi-flag package queries for accurate cross-version scope and installation resolution.
- Dynamic Compatibility:
  * Synchronized catalog compatibility resolution for 21 supported applications with NexFusion 3.6.1.

## 3.6.0

- Universal Dynamic Patch Architecture:
  * Synchronized compatibility engine and application discovery catalog with the universal dynamic patch suite, removing rigid target version gating across all 18 supported applications.
  * Target applications hook dynamically at process startup across app updates without triggering false incompatibility or untested version warnings.
  * Evaluates universal dynamic bundles to display Dynamic compatibility status consistently across all supported applications, including uninstalled catalog apps.
- Supported Applications Catalog Visibility:
  * Revamped Home screen application discovery with dual-section categorization: "Installed Applications" and "Supported Applications".
  * Users can discover, browse, and configure runtime patch preferences for all supported applications (YouTube, YT Music, Google Photos, Crunchyroll, Discord, Facebook, Instagram, Gboard, Gmail, Google Discover, LinkedIn, MiXplorer, Proton VPN, Reddit, Spotify, X, Telegram, Telegram Web) directly from the manager regardless of whether the app is currently installed on the device.
- Bundle Compatibility Series 3.6:
  * Upgraded manager runtime compatibility series to `3.6` and synchronized with NexFusion 3.6.0.

## 3.5.10

- Application Catalog Expansion: Added Crunchyroll (`com.crunchyroll.crunchyroid`) with verified targets `3.126.0` and `3.117.0` supporting native Picture-in-Picture mode and Continue Watching feed removal.
- Target Discovery Updates:
  * Added Facebook `585.0.0.47.54`.
  * Added Gboard `18.5.6.993608717-release-arm64-v8a`.
  * Converted LinkedIn to universal target support across all installed versions.
  * Added X (Twitter) `12.38.1-prod.01`.
- Compatibility & Bundle Integration: Synchronized application catalog and recommendation engine with NexFusion 3.5.10 patch bundle.

## 3.5.9

- Target Discovery Updates: Added Discord `351.11`, Instagram `453.0.0.57.79`, and Reddit `2026.43.1` to the application catalog.
- Compatibility & Bundle Integration: Synchronized the target catalog and recommendation engine with NexFusion 3.5.9.

## 3.5.8

- Dual Artifact Distribution: Release workflow now automatically compiles and attaches both optimized release APKs (`NexFusion-<tag>.apk`) and standalone debug APKs (`NexFusion-<tag>-debug.apk`) for immediate diagnostic logging and testing.
- Patcher Diagnostic Visibility & Trace Reporting:
  * Enhanced `PatchExecutor` hook failure handlers to log complete exception stack traces directly into LSPosed logs (`XposedBridge.log`) and Android Logcat (`PatcherLog.error`).
  * Instrumented bundle initialization in `PatchSession` with detailed exception capture for missing or broken patch dependencies.
- Compatibility & Bundle Integration: Synchronized target catalog and recommendation engine with NexFusion 3.5.8 patch bundle.

## 3.5.7

- Compatibility & Bundle Integration: Synchronized application catalog and recommendation engine with NexFusion 3.5.7 patch bundle including complete LinkedIn SDUI media downloading support.

## 3.5.6

- Target Discovery & Catalog Synchronization:
  * Added official target support for X (Twitter) `12.38.0-prod.01`.
  * Synchronized YouTube target recommendations so `21.43.523` is properly surfaced as the recommended release.
- Compatibility & Bundle Integration: Aligned application catalog and recommendation engine with NexFusion 3.5.6 patch bundle.

## 3.5.5

- Target Discovery & Catalog Synchronization:
  * Added official target support for Telegram and Telegram (Web) `13.1.1` (Play Store build).
  * Synchronized target catalog for LinkedIn `4.1.1266` compatibility.
- Compatibility & Bundle Integration: Fully aligned application catalog and recommendation engine with NexFusion 3.5.5 patch bundle features across all 17 supported applications.

## 3.5.4

- Modern Brand Evolution: Introduced the new "Hardware Silicon Matrix" adaptive app icon featuring a chamfered silicon security die, thick circuit bus trace geometry, and hardware contact pads with complete Android 13+ monochrome themed-icon support.
- Target Discovery Updates: Added official target version discovery for Instagram `453.0.0.55.79`, X (Twitter) `12.37.2-prod.01`, and YouTube Music `9.41.52` across the application catalog.
- Compatibility & Bundle Integration: Fully synchronized target catalog and recommendation engine with NexFusion 3.5.4 patch bundle.

## 3.5.3

- Target Discovery Updates: Added official target version discovery for YouTube `21.43.523` and Reddit `2026.43.0` across the application catalog.
- Compatibility & Patch Integration: Aligned manager catalog and patch recommendations with NexFusion 3.5.2 bundle series.

## 3.5.2

- Fluid Navigation Geometry & Bounds Alignment: Re-architected indicator layout to sample exact measured tab coordinates directly from the layout pass, guaranteeing pixel-perfect alignment under the Settings tab across all screen sizes, display scaling levels, and orientations.
- Multi-Screen Responsive Drag Kinetics: Refactored touch drag gesture tracking with continuous boundary mapping and eliminated artificial container shifts during intra-tab transitions.

## 3.5.1

- Fluid Liquid Glass Floating Navigation: Implemented continuous touch drag tracking, dynamic horizontal stretch kinetics, and physics-based spring snapping across navigation destinations.
- Direct-Page Full Height Layout: Removed artificial bottom container clearance to let scrollable content glide smoothly underneath the translucent floating bar with seamless edge-to-edge depth.
- Translucent Frosted Glass Effect: Enhanced floating navigation bar with dynamic backdrop texture blur and subtle highlight borders in MIUIX theme, accompanied by an appearance toggle in Settings.
- System Gesture Navigation Insets: Coordinated navigation bar window insets and bottom content padding so interactive elements remain accessible above the gesture navigation pill.
- Unified Theme & Icon Harmony: Paired modern outlined/filled icons for Home, modular Patches, and Settings with a balanced dark theme palette across Material 3 and MIUIX.

## 3.5.0

- App Icon & Visual Identity: Replaced legacy branding with modern Prism Fusion geometric emblem and Material You monochrome adaptive icons.
- Backwards-Compatible Bundle Series Validation: Relaxed BundleManifest series check so manager 3.5 seamlessly loads and installs bundles from earlier series without version mismatch errors.
## 3.4.5

- Patch Download Progress: Added real-time download progress tracking (streaming byte counts, percentage, and progress bar) during patch bundle installations and updates in both PatchesScreen and the HomeScreen updates banner.
- Responsive Card Header: Modernized patch source card header layout with adaptive FlowRow wrapping and constrained provenance badges (softWrap = false, maxLines = 1), preventing badges from wrapping into vertical single-character columns on narrow screens or enlarged system font scales.
- Performance & State Throttling: Throttled download progress state updates to 100ms intervals to eliminate unnecessary recompositions during fast downloads.

## 3.4.4

- Navigation Pill & Layout Geometry Fix: Restored exact bounds on MiuixFloatingNavItem and selection indicators, preventing the navigation pill from expanding across the entire display.
- Window Inset Alignment: Removed root-level navigation bar insets so MIUIX's built-in floating bar padding aligns cleanly with system gestures.
- Rate-Limit Resilient Update Checker: Added transparent fallback to GitHub Atom feeds and expanded assets when pi.github.com encounters unauthenticated IP rate limits (HTTP 403), ensuring in-app update checks succeed reliably on all mobile networks.
## 3.4.3

- Responsive UI Layout & Font Scaling: Applied content-proportional height constraints (`heightIn(min = ...)`) across buttons, segmented tabs, search inputs, and navigation items, preventing label clipping and text overflow at high system font scales.
- Navigation Bar Inset Handling: Injected proper navigation bar window insets and IME padding on the primary container, ensuring bottom buttons and input fields are never obscured by system navigation bars or the keyboard.
- PatchExecutor DEX Discovery: Added `FindClassListFunc` and `dexClassList` / `getDexClasses` helpers for multi-class DEX searches in runtime patches.

## 3.4.2

- UI Rollback: Restored the stable manager presentation without the experimental Liquid Glass
  redesign.
- Compatibility: Preserved patching, updater, settings, navigation, and lifecycle behavior while
  the visual redesign is revisited separately.

## 3.3.2

- Honest Process Lifecycle & Force-Stop Control: Distinguishes genuine root `am force-stop` execution from background process termination (`killBackgroundProcesses`), providing truthful status feedback without false success claims.
- 1-Tap App Info Navigation: Added direct "App info" button and menu action on each app's patch screen to quickly navigate to Android system settings for manual force-stops on unrooted devices.
- Curated Catalog & Upstream Attribution: Updated catalog entries with the Facebook media downloader (photos, videos, Reels, audio, stories) and consolidated Reddit patches, adding upstream attribution for `SysAdminDoc/Hushfacebook` and `gnadgnaoh/NexFusion-XES`.
- Repository Maintenance: Removed dead links to internal audit files and aligned release metadata.

## 3.3.0

- Coordinated Versioning Series 3.3: Updated compatibility series to `3.3` for `NexFusion` 3.3.0 patch bundle release parity.
- Supported App Catalog: Added `Gmail` (`com.google.android.gm`) and `Google (Discover)` (`com.google.android.googlequicksearchbox`) to default supported applications list mapped to `NexFusion Patches`.

## 3.2.5

- In-App Root Force Stop: "Force stop" button on patch screens now terminates target applications directly via root (`am force-stop`) without redirecting the user to system App Info settings.
- Version Display Alignment & Color Harmony: fixed layout misalignment on long version strings (e.g. Gboard) by applying single-line ellipsis constraints, hiding redundant "Rec: v..." hints when the installed target is already officially compatible, and harmonizing typography colors to `onSurfaceVariant`.
- Matched release release parity with NexFusion patch bundle 3.2.5.

## 3.2.2

- Patch-only release within series `3.2`, patches repo at `3.2.2`.
- UI fix: update-check refresh now cancels any in-progress job and debounces rapid taps, so the UI stays fluid.

## 3.2.0

- Coordinated Versioning Architecture: introduced coordinated release series `3.2`, establishing the manager version as the major compatibility series while enabling independent patch-only updates (`3.2.0`, `3.2.1`...) without requiring a new manager build.
- Bundle Compatibility Validation: `BundleManifest` now validates bundle compatibility metadata (`compatibilitySeries`, `minManagerVersion`, `maxManagerVersion`) using semantic version comparison, cleanly rejecting incompatible bundles with explicit user feedback while preserving full backwards compatibility for legacy and third-party bundles.
- Seamless Bundle Migration: added automated migration when updating from the legacy `a092devs` bundle to `NexFusion Patches` (`io.github.nexfusion.patches`), automatically replacing previous installations without resource package ID conflicts while preserving user toggle states and source tracking.
- Smart Release Discovery: `SourceClient` now discovers and prioritizes releases compatible with the running manager's compatibility series, preventing accidental installation of breaking future series releases during patch-only update checks.
- Supported App Catalog: updated default bundle associations across all community-ported applications (Instagram, Spotify, Reddit, Discord, X, Gboard, LinkedIn, Proton VPN, MiXplorer) to `NexFusion Patches`.

## 3.1.29


- Spotify Bundle Mapping: mapped Spotify to `a092devs Patches` in the supported app catalog.

## 3.1.28

- Installed Bundles: a bundle's card now lists only the apps present on this device, so apps you have uninstalled (for example Spotify after uninstalling it) stop appearing in the app chips; a bundle whose apps are all missing says so in place of the chips.

## 3.1.27

- Changelog Wrapping: soft-wrapped changelog lines are now merged back into their markdown block the way GitHub renders them, so a bullet or paragraph whose text spans several source lines shows as one wrapped block instead of continuation lines jumping to the left margin.

## 3.1.26

- Changelog Rendering: release notes and bundle changelogs in the dialogs now render their inline markdown instead of showing the raw markers — `code` runs as a monospace chip, **bold**, *italic* and ~~strikethrough~~ merge with the body text, and markdown links plus bare URLs become tappable — so backticks around package, bundle and file names no longer leak into the dialog.

## 3.1.25

- X Bundle Mapping: the supported-app catalog now points X (`com.twitter.android`) at the `a092devs Patches` bundle and its `12.35.0-prod.01` target instead of the removed `Piko Patches` bundle (`12.19.1-release.0`); the README's bundled-app list documents Instagram and X under `a092devs`.

## 3.1.24

- Compact Floating Bar Setting: the Miuix-only floating navigation toggle is now labelled "Floating bar" with a single-line description ("Floats over the content with Miuix blur; off by default.") instead of the oversized title and two-line summary.

## 3.1.23

- Settings Cleanup: the Diagnostics entry and its copyable system/bundle dump were removed from Settings, and Clear DexKit cache moved under General. The framework name and version are no longer surfaced anywhere in the UI.
- Shorter Tagline: the description at the bottom of Settings now reads "NexFusion applies patch bundles from patch sources. Only add sources you trust."

## 3.1.22

- Status Card App Version: the home screen's "Module is active" card now shows the running NexFusion version instead of the Xposed framework name and version (like "Vector 1.9.4"), so screenshots and bug reports name the module build that is actually in use; the framework is still listed in Diagnostics.

## 3.1.21

- Installed-Only App List: the home catalog now lists only applications that are actually installed on the device, so bundle-supported apps that are not installed — for example YouTube Music while the nexfusion bundle is imported — no longer appear under "Supported Apps (Not Installed)". The Not Installed filter chip and the unused catalog helpers behind it were removed as well; the app catalog itself is unchanged for per-app screens and diagnostics.

## 3.1.20

- Patcher Resilience: a patch or bundle that throws while applying no longer aborts the rest of the session, and the fingerprint cache is now persisted even then; previously an escaping exception skipped the cache write, so every following start of the affected app re-ran every fingerprint match and showed the initialization popup again.
- Readable Patch Failures: a failed patch is now logged with its bundle and patch name plus its exception on release builds, and the "Error while applying the following patches" toast is only shown when the set of failing patches changes instead of on every start of the affected app (which previously tripped the system toast quota and got the message discarded entirely).
- Missing-Anchors Reporting: a fingerprint that finds no anchor now raises a named FingerprintNotFoundException instead of surfacing as an anonymous NullPointerException from DexKit's cache layer.
- Check for Updates Feedback: tapping Check for updates now shows an inline "Checking for updates…" state right away and always reports the outcome in a snackbar — including "a new NexFusion version is available" and "patch updates are available" — where the Settings screen previously stayed silent whenever something was found.
- Release Diagnostics: the per-bundle cache decision ("using cache: true/false"), the applied summary and failures are logged on release builds too, so a device can be audited with logcat alone.

## 3.1.19

- Patcher Cache: fingerprint caches are now reused by debug builds as well, so the companion module stops re-running every fingerprint match each time a patched app starts; the cache key already covers the app, the NexFusion build and the bundle versions, and Clear patcher cache stays as the manual escape hatch.
- Miuix Update Feedback: the Miuix presentation now surfaces one-off messages, like the result of Check for updates, source refreshes and install failures, in a swipe-dismissible Miuix snackbar anchored above the navigation bar; previously only the Material presentation had a message surface, which made Check for updates look like it did nothing.

## 3.1.18

- Search Bar Alignment: Redesigned `ThemedSearchField` with a centered capsule layout ensuring the search glyph and placeholder/input text share the exact same vertical center line without offset in both MIUIX and Material 3.
- Floating Navigation Bar: Eliminated the sliding animation across navigation tabs in favor of instant, smooth in-place selection pill transitions.
- Module Active Status Card: Redesigned the home screen status card with a lush emerald green container, fine accent border, circular checkmark badge, and active status chip when the LSPosed module is loaded.
- Compact Card Action Buttons: Replaced oversized patch source action buttons on `SourceCard` with sleek, proportional compact pill buttons (`ThemedCompactFilledButton` and `ThemedCompactTonalButton`).
- Changelog Readability: Implemented `ChangelogText` with clean markdown heading typography, accent bullet lists, and scrollable container for release notes dialogs.

## 3.1.17

- Companion Patch Catalog Alignment:
  * Migrated Instagram (`com.instagram.android`) default patch source to `a092devs Patches` with verified compatibility targets `451.0.0.53.80`, `450.0.0.46.79`, and `447.0.0.55.81` ported from `SysAdminDoc/HushGram`.
  * Expanded Facebook (`com.facebook.katana`) target recommendations to include `582.0.0.50.54` and `581.0.0.45.54` ported from `SysAdminDoc/Hushfacebook`.
- Repository & Pipeline Maintenance:
  * Synced `.gitignore` rules across modules and ensured automated release pipelines trigger cleanly on pushed tags without commit squashing.

## 3.1.16

- In-App Updater: Directly download and launch package installation for NexFusion releases inside the app without external browser redirection, using `FileProvider` and background progress reporting.
- Supported Uninstalled Apps Catalog: Ensure all applications supported across patch bundles (such as YouTube Music, Proton VPN, Spotify, etc.) are discovered and displayed in the app catalog with verified recommendations even before installation.
- Miuix & HyperOS Theme Consistency: Replace hardcoded page backgrounds with `MiuixTheme.colorScheme.background`, replace Bootstrap badge colors with adaptive translucent squircle chips in dark/light themes, wrap patch toggle preferences in rounded `MiuixCard` groups, and distinguish primary vs secondary buttons.

## 3.1.15

- Silence routine debug toasts and restrict initialization toast notifications to the main application process (`isMainProcess()`) to eliminate spam across multi-process application startups.
- Refine Miuix floating navigation bar touch targets to 92.dp x 64.dp with 80.dp x 52.dp concentric selection pill and 12.dp bottom inset for comfortable navigation.
- Remove redundant card border outlines (`cardBorder = null`) in Miuix design system theme configuration.
- Upgrade in-app update checker SemVer comparison using `VersionComparator` to correctly evaluate release tags containing zero patch versions (e.g. `3.x.0`).
- Update companion patch catalog descriptions and attributions for Proton VPN, PixelBoard Gboard features, and new YouTube player controls.

## 3.1.14

- Fix Miuix (HyperOS design) text and icon visibility: Material text and icons inside the Miuix
  presentation followed the Miuix palette instead of falling back to black, so card titles,
  filter chips, search field icons and button labels were hard to read.
- Raise the low-contrast Miuix pairings used by filter chips, the search placeholder and text
  field labels to readable roles; section titles are slightly brighter.
- Show labels in the optional floating navigation bar, with the selected item emphasised.
- Paint the Miuix page background from the app's own light/dark mode so forcing a theme no
  longer clashes with the system window background.
- Add selectable colour schemes (dynamic colour on supported devices, or a fixed accent such as
  violet, emerald or amber) applied to both design systems.
- Add animated screen transitions and a sliding selection indicator in the floating navigation
  bar; align paddings and typography across the Material 3 and Miuix presentations.
- Rebuild the Miuix tab selector and the floating-bar selection pill with concentric, evenly
  inset shapes so they show no stray border or clipped corner while sliding.
- Publish releases automatically: a push to `main` dispatches the release workflow for the
  version in `app/build.gradle.kts` whenever that version has no release yet.

## 3.1.12 and earlier

See the GitHub Releases page for earlier release notes.
