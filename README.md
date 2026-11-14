<div align="center">
  <img src="assets/icon.png" width="128" height="128" alt="NexFusion Logo" />
  <h1>NexFusion</h1>
  <p><strong>Advanced root + LSPosed runtime bytecode patch manager for Android featuring fluid Material design.</strong></p>
  <p>
    <a href="https://github.com/a092devs/NexFusion/releases"><img src="https://img.shields.io/github/v/release/a092devs/NexFusion?style=flat&color=1f6feb&labelColor=21262d&logo=github&label=Release" alt="Latest Release" /></a>
    <img src="https://img.shields.io/badge/Android-8.1%2B-238636?style=flat&labelColor=21262d&logo=android&logoColor=white" alt="Android 8.1+" />
    <img src="https://img.shields.io/badge/LSPosed-API%20101%2F102-8957e5?style=flat&labelColor=21262d" alt="LSPosed API 101/102" />
    <img src="https://img.shields.io/badge/UI-Miuix%20%7C%20Material%203-a371f7?style=flat&labelColor=21262d" alt="Design Systems" />
    <img src="https://img.shields.io/badge/Icon-Hardware%20Silicon%20Matrix-1f6feb?style=flat&labelColor=21262d" alt="Hardware Silicon Matrix" />
    <a href="https://crowdin.com/project/nexfusion"><img src="https://img.shields.io/badge/Crowdin-Translate-1f6feb?style=flat&labelColor=21262d&logo=crowdin&logoColor=white" alt="Crowdin Translations" /></a>
    <a href="https://t.me/a092devs"><img src="https://img.shields.io/badge/Telegram-@a092devs-2f81f7?style=flat&labelColor=21262d&logo=telegram&logoColor=white" alt="Telegram Support" /></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL--3.0-d29922?style=flat&labelColor=21262d" alt="GPL-3.0" /></a>
  </p>
  <p>
    <a href="#-getting-started">🚀 Getting Started</a> •
    <a href="#-key-features">⚡ Features</a> •
    <a href="#-curated-patch-repositories">🧩 Patch Bundles</a> •
    <a href="#-supported-applications-matrix">📱 Supported Apps</a> •
    <a href="#-appearance--theming">🎨 Theming</a> •
    <a href="#-translations">🌐 Translations</a> •
    <a href="#-troubleshooting">🔍 Troubleshooting</a>
  </p>
</div>

---

## 📖 Overview

**NexFusion** is an advanced Android application that applies modular runtime bytecode patches to supported applications via **LSPosed / libxposed (API 101/102)** and **DexKit**.

Unlike traditional ahead-of-time (AOT) patchers that decompile, modify, re-sign, and reinstall target APKs, **NexFusion operates purely in-memory at process startup**:

- 🔒 **Zero APK Tampering:** Target applications remain official, unaltered on disk, and preserve their authentic Play Store signatures and Play Protect integrity.
- ⚡ **Dynamic DexKit Interception:** Obfuscated classes, methods, and fields are resolved on the fly via bytecode opcode pattern analysis and string fingerprinting.
- 🛡️ **Fail-Safe Isolation:** Hooks execute within protected `runCatching` runtime boundaries, preventing target app crashes if non-critical symbols change across updates.
- 📦 **Modular `.nxb` Bundles:** Patches are packaged into signed `.nxb` (NexFusion Bundle) APK containers loaded directly into process memory — no APK installation required.

> [!IMPORTANT]
> - **Root & Framework Requirements:** Android 8.1+ (API 27) with root access and LSPosed or any modern framework implementing `libxposed` API 101/102.
> - **Security Boundary:** Patch bundles execute bytecode within target application processes. Always source bundles from trusted maintainers.
> - **Integrity Guarantee:** NexFusion deliberately excludes banking, payment, and security-critical applications from patching.

---

## 🚀 Getting Started

1. **📥 Install NexFusion:** Download the signed release APK from [Releases](https://github.com/a092devs/NexFusion/releases).
2. **🛡️ Configure LSPosed Scope:** Enable the **NexFusion** module in LSPosed and check the scope for your target apps (e.g. YouTube, Reddit, Instagram, Facebook, Telegram, X, Maps).
3. **📦 Install Patch Bundles:** Open NexFusion → **Patches** tab. The official companion repository (`a092devs/Nexfusion-Patches`) is pre-configured; tap **Install** on `NexFusion Patches`.
4. **⚙️ Toggle Desired Patches:** Open the **Home** tab, tap any supported application card, and enable your preferred runtime hooks.
5. **🔄 Apply & Restart:** Tap **Force Stop** or use the 1-tap **App Info** shortcut to restart the target application and let LSPosed attach the updated bytecode hooks.

---

## ⚡ Key Features

<details>
<summary><b>1. 🌐 Multi-Source Patch Aggregation & Provenance</b></summary>
<br>

- Add, update, and manage multiple independent patch repositories simultaneously.
- **Source Provenance Classification:** Clearly badges sources as **Official**, **NexFusion**, **Community**, or **Custom**.
- **Conflict Detection:** Proactively flags applications whose patches are declared by more than one bundle before enabling.
- **Offline Caching:** Instant application startup using local metadata caching with pull-to-refresh synchronization.
</details>

<details>
<summary><b>2. 🎨 Dual Design Systems: Official Miuix & Material 3</b></summary>
<br>

- **Official Miuix Integration:** Built directly with the official [compose-miuix-ui](https://github.com/compose-miuix-ui/miuix) library (`miuix-ui`, `miuix-blur`, `miuix-preference`, `miuix-icons`), delivering the authentic MIUI / HyperOS aesthetic.
- **Material 3 Mode:** Native AndroidX Material 3 design with dynamic Monet color theming on Android 12+.
- **Cohesive Surface Switching:** Changing the design system instantly transforms every screen, dialog, card, and navigation bar in unison.
- **Optional Floating Navigation Bar:** An opt-in Miuix arrangement featuring `FloatingNavigationBar` with real-time blur shaders on Android 13+ and safe fallbacks on older versions.
</details>

<details>
<summary><b>3. 🎯 Truthful Dynamic Compatibility Engine</b></summary>
<br>

- Version recommendations and compatibility badges are derived directly from the targets declared by active patch bundles at runtime:
  ```kotlin
  val allTargets: List<AppTarget> = if (matchingBundles.isNotEmpty()) {
      matchingBundles.flatMap { it.targets[packageName].orEmpty() }
  } else {
      known?.targets.orEmpty()
  }
  ```
- Uses semantic version evaluation with trailing zero normalization (`2026.42.0` vs `2026.42`).
</details>

<details>
<summary><b>4. 🔄 Honest Process Lifecycle & Force-Stop Control</b></summary>
<br>

- Distinguishes genuine root `am force-stop` termination from background process killing (`killBackgroundProcesses`), providing truthful feedback.
- Integrated 1-tap **App Info navigation** directly from the patch screen for manual force-stop on unrooted configurations.
</details>

---

## 🎨 Appearance & Theming

<details>
<summary><b>View Display & Customization Settings</b></summary>
<br>

Configure via **Settings → Appearance**:

| Setting | Options | Description |
|---|---|---|
| **Colour Mode** | ☀️ Light / 🌙 Dark / ⚙️ Follow System | System-wide theme brightness, persisted across reboots |
| **Design System** | 📱 Material 3 / 💎 Miuix | Seamlessly switches the visual design language across all screens |
| **Floating Navigation Bar** | 🟢 On / ⚪ Off | *(Miuix only)* Detaches bottom bar with real-time blur shader (Android 13+) |

</details>

---

## 🧩 Curated Patch Repositories

The official companion patch repository is maintained at:
- 🔗 **[a092devs/Nexfusion-Patches](https://github.com/a092devs/Nexfusion-Patches)**

### Included Bundles:
- **`Nexfusion-Patches`:** Unified community runtime patch distribution covering all **22 supported application targets**:
  - 🎥 **Video & Audio:** YouTube, YouTube Music, Crunchyroll, Kick, Twitch, VLC, Stremio
  - 💬 **Social & Communication:** Telegram, Telegram (Web), Instagram, Facebook, X (Twitter), Reddit, Discord, LinkedIn
  - 🛠️ **System & Productivity:** Google Photos, Gboard, Gmail, Google App (Discover), Google Maps, Proton VPN

---

## 📱 Supported Applications Matrix

<details>
<summary><b>🎥 Video, Audio & Streaming (7 Targets)</b> — <i>YouTube, YouTube Music, Kick, Twitch, Crunchyroll, VLC, Stremio</i></summary>
<br>

| Application | Package Identifier | Key Patch Capabilities |
|---|---|---|
| **YouTube** | `com.google.android.youtube` | Ad-free playback, SponsorBlock, background audio, DeArrow replacement, custom speed/quality dialogs, swipe volume/brightness, miniplayer, ambient mode, Litho node bypass, channel search fix. |
| **YouTube Music** | `com.google.android.apps.youtube.music` | Background playback, audio ad removal, exclusive audio stream playback, hide upgrade buttons, suppress promotional banners, SoundProof AI blocker, Playlist tags, offline fullscreen player, mark downloaded songs, third-party lyrics enhancement. |
| **Kick** | `com.kick.mobile` | Live stream & VOD ad blocking, background playback, custom chat emotes (7TV, BTTV, FFZ), telemetry suppression, AMOLED black theme. |
| **Twitch** | `tv.twitch.android.app` | Stream and display ad blocking, 7TV / BTTV / FFZ chat emotes, background playback, telemetry suppression, AMOLED black theme. |
| **Crunchyroll** | `com.crunchyroll.crunchyroid` | Native Picture-in-Picture mode with contextual Skip Intro and Skip Credits controls; remove titles from Continue Watching feed. Verified targets: 3.126.0, 3.117.0. |
| **VLC** | `org.videolan.vlc` | AMOLED pure black theme; suppress What's New changelog dialog on updates; unlock experimental feature flags; force bitstream audio passthrough (digital output); force AAudio low-latency output; raise network cache to 3000ms; disable audio time-stretch for artifact-free 1x speed playback; force ReplayGain track normalization with peak protection; verbose libVLC logging. |
| **Stremio** | `com.stremio.one` | Auto & manual intro/credits skipping using embedded chapters and stream metadata; auto-pick best stream (resolves 4K/cached stream on tap); hide episode spoilers (synopses & thumbnails); avoid SDH subtitles; audio & subtitle track language memory; smoother playback buffer expansion (120s); seamless binge watching; external player progress & subtitle hand-off (VLC, Vimu). |

</details>

<details>
<summary><b>💬 Social Networks & Messaging (8 Targets)</b> — <i>Telegram, Telegram (Web), Instagram, Facebook, X (Twitter), Reddit, Discord, LinkedIn</i></summary>
<br>

| Application | Package Identifier | Key Patch Capabilities |
|---|---|---|
| **Telegram** | `org.telegram.messenger` | Remove channel sponsored messages, bot ads, and video promotions; hide Story bar and avatar story rings; keep deleted messages locally; exact unrounded counts; 512 KB fast multi-connection downloads; block update dialogs; disable telemetry; auto-reveal spoilers; pure black AMOLED dark theme. |
| **Telegram (Web)** | `org.telegram.messenger.web` | Identical full-suite runtime patches as official Telegram for web-distributed standalone builds. |
| **Instagram** | `com.instagram.android` | Media downloader for feed posts, Reels, Stories, Highlights, and profile pictures; remove sponsored feed posts, Reels, and Story ads; anonymous Story and Live viewing; strip tracking parameters; ghost mode. |
| **Facebook** | `com.facebook.katana` | Media downloader for videos, photos, Reels, and Stories with DASH stream audio muxing; sponsored post and Reel ad removal; bypass screenshot and screen recording restrictions; telemetry blocking. |
| **X (Twitter)** | `com.twitter.android` | Promoted post and ad removal; video and GIF media downloader; bypass Compose sensitive media blur interstitials without tap-to-reveal; block forced updates; telemetry blackholing. |
| **Reddit** | `com.reddit.frontpage` | Feed and comment ad removal; guest browsing mode; sanitize shared URLs; suppress screenshot sharing bottom sheet; unmute Redgifs streams; custom feed sorting. |
| **Discord** | `com.discord` | Suppress crash telemetry, analytics, touch logging, and advertising identifiers; native system font typography support across chat and UI surfaces. |
| **LinkedIn** | `com.linkedin.android` | 1-tap media downloader for photos, carousel images, and progressive MP4 videos; sanitize share links; direct URL navigation bypassing `safety/go` redirects; hide suggested posts, promoted jobs, and premium upsells; feed filters; Material You dynamic theme. |

</details>

<details>
<summary><b>🛠️ System, Productivity & Utilities (6 Targets)</b> — <i>Google Photos, Google Maps, Gboard, Gmail, Google App (Discover), Proton VPN</i></summary>
<br>

| Application | Package Identifier | Key Patch Capabilities |
|---|---|---|
| **Google Photos** | `com.google.android.apps.photos` | Device spoofing for unlimited original-quality Google Photos cloud storage backups; DCIM folder backup control. |
| **Google Maps** | `com.google.android.apps.maps` | Sponsored pin and search ad suppression, high refresh rate unlock (90/120 Hz), telemetry blocking, hide AI clutter, AMOLED black theme. |
| **Gboard** | `com.google.android.inputmethod.latin` | Rounded keycaps; Google Sans Flex typography & GM3 Expressive styling; proactive post-correction undo controls; Quick & Magic Emoji (Emojify); expanded AI writing tools; spellchecker chips UI; scan text OCR; spacebar cursor trackpad. |
| **Gmail** | `com.google.android.gm` | Suppress sponsored promotional and advertising cards in personal inboxes. |
| **Google App (Discover)** | `com.google.android.googlequicksearchbox` | Filter sponsored cards, shopping ads, and promotional recommendations in the Discover feed. |
| **Proton VPN** | `ch.protonvpn.android` | Suppress upgrade banners and nag dialogs; unlock NetShield UI toggles; disable telemetry and background observability workers. |

</details>

---

## 🛠️ Building from Source

<details>
<summary><b>View Build Instructions & Developer Guide</b></summary>
<br>

### Prerequisites
- JDK 21 (Temurin or OpenJDK)
- Android SDK with API 37 platform tools (configured via `ANDROID_HOME` or `local.properties`)

```bash
# Clone the repository
git clone https://github.com/a092devs/NexFusion.git
cd NexFusion

# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK (app/build/outputs/apk/debug/)
./gradlew assembleDebug
```

</details>

---

## 🌐 Translations

NexFusion is localized by community contributors via [Crowdin](https://crowdin.com/project/nexfusion).

<div align="center">
  <a href="https://crowdin.com/project/nexfusion">
    <img src="https://img.shields.io/badge/Crowdin-Translate%20NexFusion-1f6feb?style=flat&labelColor=21262d&logo=crowdin&logoColor=white" alt="Crowdin Translations" />
  </a>
</div>

- **Help Translate:** Help translate NexFusion into your language or review existing localizations on our [Crowdin Project](https://crowdin.com/project/nexfusion).
- **Automated Synchronization:** Completed translations are automatically synchronized into the repository and included in upcoming release builds.

---

## 🔍 Troubleshooting

<details>
<summary><b>View Troubleshooting & Diagnostics Matrix</b></summary>
<br>

| Issue | Likely Cause | Solution |
|---|---|---|
| **Module shows Inactive** | Module not enabled in LSPosed | Open LSPosed Manager, enable NexFusion, and reboot device. |
| **Patches not applying** | Target app not in scope | Check LSPosed scope; NexFusion Home screen displays a yellow scope warning if missing. |
| **Compatibility Warning** | Installed app differs from target | Inspect the target app card in Home to check verified versions; hooks may still resolve dynamically. |
| **Blur missing on Floating Bar** | Android version limitation | Runtime shader blur requires Android 13+ (API 33+); older versions render an opaque surface. |
| **Need Support / Diagnostics** | Reporting an issue | Go to **Settings → Copy diagnostics** and attach the report when submitting a GitHub issue, or reach out on Telegram [@a092devs](https://t.me/a092devs). |

</details>

---

## 🤝 Contributing & Support

Contributions, bug reports, and feature requests are welcome!
- **Feedback & Feature Requests:** If you encounter issues or have feature requests, feel free to contact [@a092devs](https://t.me/a092devs) on Telegram.
- **Bug Reports & Pull Requests:** Ensure all unit tests pass prior to opening pull requests: `./gradlew testDebugUnitTest`
- All contributions must adhere to the runtime-only architecture (in-memory execution via LSPosed API 101/102, zero APK tampering).
- Ensure target application stability by wrapping non-essential hooks in `runCatching` safeguards.

---

## 📜 Credits & Acknowledgments

<details>
<summary><b>View Upstream & Open-Source Acknowledgments</b></summary>
<br>

NexFusion is developed and maintained by **a092devs**. We gratefully acknowledge the open-source projects, reverse engineers, and library authors whose research and tools empower our ecosystem:

| Project | Contribution |
|---|---|
| **[LSPosed](https://github.com/LSPosed/LSPosed)** | Core ART runtime hooking framework and official `libxposed` (API 101/102) |
| **[DexKit](https://luckypray.org/DexKit/en/)** | High-performance Dex bytecode pattern matching and opcode resolution |
| **[compose-miuix-ui](https://github.com/compose-miuix-ui/miuix)** | Official Compose Multiplatform MIUI / HyperOS design components by YuKongA |
| **[Morphe](https://morphe.software)** | Bytecode runtime hook patterns, fingerprint specifications, and shared utilities |
| **[ReVanced](https://revanced.app)** | Reverse engineering research, feature specifications, and community contributions |
| **[SysAdminDoc](https://github.com/SysAdminDoc)** | Stealth, ad-blocking, and media extraction hook specifications |
| **[Piko](https://github.com/crimera/piko)** | Runtime hook logic and downloader patterns |
| **[VenusIsJaded](https://github.com/VenusIsJaded/Venus-Patches)** | Privacy target tables and font styling |
| **[heyymichii](https://github.com/heyymichii/michii-patches)** | Share-link cleaning, direct navigation, and tracking removal |
| **[ak800i](https://github.com/ak800i/mixplorer-patches-for-morphe)** | Scoped-storage file sharing fix for Android 11+ |
| **[jkennethcarino](https://github.com/jkennethcarino/adobo)** | Screenshot-banner suppression, link sanitization, and UI hooks |
| **[gnadgnaoh](https://github.com/gnadgnaoh/SexAlloy)** | Inbox promotion and Discover feed ad filtering |

</details>

---

## ⚖️ License

NexFusion is open-source software developed by **a092devs** and licensed under the **GNU General Public License v3.0** — see [LICENSE](LICENSE). Third-party libraries and ported feature specifications retain their respective licenses and attribution.
