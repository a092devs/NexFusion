<div align="center">
  <img src="https://raw.githubusercontent.com/a092devs/NexFusion/main/assets/icon.png" width="128" height="128" alt="NexFusion Logo" />
  <h1>NexFusion Maven Repository</h1>
  <p><strong>Official Maven package distribution for NexFusion patcher API, build plugins, and runtime dependencies.</strong></p>
  <p>
    <img src="https://img.shields.io/badge/Format-Maven%20Repository-8957e5?style=flat&labelColor=21262d" alt="Format: Maven Repository" />
    <img src="https://img.shields.io/badge/LSPosed-API%20101%2F102-8957e5?style=flat&labelColor=21262d" alt="LSPosed API 101/102" />
    <img src="https://img.shields.io/badge/Icon-Hardware%20Silicon%20Matrix-1f6feb?style=flat&labelColor=21262d" alt="Hardware Silicon Matrix" />
    <a href="https://t.me/a092devs"><img src="https://img.shields.io/badge/Telegram-@a092devs-2f81f7?style=flat&labelColor=21262d&logo=telegram&logoColor=white" alt="Telegram Support" /></a>
    <a href="https://github.com/a092devs/NexFusion/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-GPL--3.0-d29922?style=flat&labelColor=21262d" alt="GPL-3.0" /></a>
  </p>
  <p>
    <a href="#-overview">📖 Overview</a> •
    <a href="#-repository-setup">📦 Repository Setup</a> •
    <a href="#-published-artifacts">🧩 Published Artifacts</a> •
    <a href="#-usage-example">🛠️ Usage</a>
  </p>
</div>

---

## 📖 Overview

This branch serves as the official GitHub-hosted Maven repository for the **NexFusion** ecosystem. It provides the core patcher API, DexKit pattern matching integration, and Gradle build logic used to develop and assemble modular runtime patch bundles (`.nxb`).

> [!IMPORTANT]
> - **Framework Support:** Artifacts support both **LSPosed API 101 and 102**.
> - **Runtime Architecture:** Patches compiled using this API execute purely in-memory at process startup via LSPosed without modifying, re-signing, or repackaging target APKs.

---

## 📦 Repository Setup

To use artifacts from this repository, declare the Maven endpoint in your Gradle project:

### Kotlin DSL (`settings.gradle.kts`)

```kotlin
dependencyResolutionManagement {
    repositories {
        maven(url = "https://raw.githubusercontent.com/a092devs/NexFusion/maven")
        mavenCentral()
        maven(url = "https://api.xposed.info")
    }
}

pluginManagement {
    repositories {
        maven(url = "https://raw.githubusercontent.com/a092devs/NexFusion/maven")
        gradlePluginPortal()
        mavenCentral()
    }
}
```

### Groovy DSL (`settings.gradle`)

```groovy
dependencyResolutionManagement {
    repositories {
        maven { url 'https://raw.githubusercontent.com/a092devs/NexFusion/maven' }
        mavenCentral()
        maven { url 'https://api.xposed.info' }
    }
}

pluginManagement {
    repositories {
        maven { url 'https://raw.githubusercontent.com/a092devs/NexFusion/maven' }
        gradlePluginPortal()
        mavenCentral()
    }
}
```

---

## 🧩 Published Artifacts

| Group ID | Artifact ID | Description |
|---|---|---|
| `io.github.nexfusion` | `patcher` | Core runtime patcher and lifecycle API supporting LSPosed API 101/102 |
| `io.github.nexfusion` | `dexkit` | High-performance DexKit bytecode pattern matching and opcode resolution engine |
| `io.github.nexfusion.build` | `build-logic` | Shared Gradle convention plugins and packaging tasks |
| `nexfusion.bundle` | `nexfusion.bundle.gradle.plugin` | Gradle plugin for building and packaging signed `.nxb` bundle APKs |

---

## 🛠️ Usage Example

Add the patcher dependencies to your bundle module's `build.gradle.kts`:

```kotlin
plugins {
    id("nexfusion.bundle")
}

dependencies {
    compileOnly("io.github.nexfusion:patcher:2.1.0")
    compileOnly("io.github.nexfusion:dexkit:2.1.0")
}
```

---

## 🤝 Contributing & Support

- **Repository:** [NexFusion on GitHub](https://github.com/a092devs/NexFusion)
- **Patch Bundles:** [Nexfusion-Patches on GitHub](https://github.com/a092devs/Nexfusion-Patches)
- **Telegram Support:** [@a092devs](https://t.me/a092devs)
- **License:** Licensed under [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html).
