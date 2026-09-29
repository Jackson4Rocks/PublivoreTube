<div align="center">

<img src="https://raw.githubusercontent.com/Jackson4Rocks/PublivoreTube/main/docs/assets/publivoretube-icon.svg" alt="PublivoreTube official icon" width="150"/>

# PublivoreTube

### YouTube, reimagined for your TV.

A polished, open-source Android TV client focused on **remote-first navigation**, **Material 3-inspired UI**, and a clean separation between **content metadata** and **playback**.

[![Android Build](https://github.com/Jackson4Rocks/PublivoreTube/actions/workflows/android.yml/badge.svg)](https://github.com/Jackson4Rocks/PublivoreTube/actions/workflows/android.yml)
[![Android TV](https://img.shields.io/badge/Android%20TV-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/tv)
[![Kotlin](https://img.shields.io/badge/Kotlin-Compose-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/MIT-License-4F378B?style=for-the-badge)](LICENSE)

</div>

---

## ✦ What is PublivoreTube?

PublivoreTube is my Android TV client project built around one idea:

> **The TV experience should feel like a TV experience.**

Big visual targets, predictable D-pad movement, obvious focus states, comfortable spacing, and an expressive interface designed for the couch.

The project keeps **metadata**, **UI**, and the future **playback layer** separate so each part can evolve cleanly.

<table>
<tr>
<td width="50%">

### ◈ Built for the couch

- D-pad / remote-first navigation
- Android TV launcher integration
- Large cards and readable typography
- Home, Search, Subscriptions, History, Settings and About
- Android 8.0+ target

</td>
<td width="50%">

### ◆ Built to experiment

- Jetpack Compose
- Compose for TV / Material 3 for TV
- YouTube Data API v3
- Coil thumbnail loading
- Media3 / ExoPlayer foundation
- Demo feed without an API key

</td>
</tr>
</table>

---

## ✦ Expressive visual language

The UI direction uses a restrained Material 3-inspired shape system: **pills, rounded rectangles, circles, diamonds, soft asymmetric forms, and tonal surfaces**. The goal is expressive hierarchy without turning the TV interface into visual noise.

<div align="center">

| Pill | Rounded container | Circle | Diamond | Soft asymmetric form |
|:---:|:---:|:---:|:---:|:---:|
| Navigation | Content cards | Status | Focus accent | Hero decoration |

</div>

---

## ✦ Current state

| Area | Status |
|---|:---:|
| Android TV / Leanback launcher | ✅ |
| Android 8.0+ | ✅ |
| D-pad navigation | ✅ |
| Material 3 for TV | ✅ |
| YouTube Data API metadata | ✅ |
| YouTube search | ✅ |
| Real thumbnails | ✅ |
| Demo feed | ✅ |
| Media3 / ExoPlayer foundation | ✅ |
| Native in-app video playback | 🚧 |
| Persistent history | 🚧 |
| Persistent settings | 🚧 |

---

## ✦ Architecture

```text
                 ┌───────────────────────────┐
                 │       Compose / TV UI   │
                 └─────────────┬─────────────┘
                               │
                               ▼
                 ┌───────────────────────────┐
                 │      VideoRepository     │
                 └─────────────┬─────────────┘
                         ┌─────┴─────┐
                         ▼           ▼
                ┌──────────────┐  ┌─────────────┐
                │ YouTube API  │  │ Demo Feed   │
                │   v3         │  │  fallback   │
                └──────┬───────┘  └─────────────┘
                       │
                       ▼
                ┌──────────────┐
                │    Video     │
                └──────┬───────┘
                       │
                       ▼
                ┌──────────────┐
                │    Media3    │
                │   playback   │
                └──────────────┘
```

---

## ✦ YouTube Data API

PublivoreTube **does not commit API credentials** to the repository.

Create a local `local.properties`:

```properties
YOUTUBE_API_KEY=YOUR_API_KEY_HERE
```

A `YOUTUBE_API_KEY` environment variable is also supported.

Without a key, the app falls back to built-in demo content.

### Playback note

The YouTube Data API provides **metadata and search results**, not a general-purpose direct media stream URL.

For the current implementation, selecting a video opens its normal YouTube URL as a compatibility fallback. Native in-app playback remains a separate development track.

---

## ✦ Build

### Requirements

- JDK 17+
- Android SDK
- Android SDK Platform 36
- Android Build Tools 36.0.0
- A Gradle version compatible with the project

### Debug

```bash
gradle :app:assembleDebug
```

### Release

```bash
gradle :app:assembleRelease
```

### Install over ADB

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Release output:

```text
app/build/outputs/apk/release/app-release.apk
```

---

## ✦ Release signing

The release build uses a local signing keystore.

Keep these **out of GitHub**:

```text
local.properties
*.jks
*.keystore
```

The actual keystore and passwords stay on your machine / CI secrets.

---

## ✦ Roadmap

### 0.1.x — Foundation
- [x] Android TV shell
- [x] Remote-first UI
- [x] Material 3 TV components
- [x] YouTube Data API metadata/search
- [x] Real thumbnails
- [x] About / maintainer page
- [x] Custom official app icon
- [x] CI APK artifacts

### 0.2 — Experience
- [ ] Native in-app player
- [ ] Video details screen
- [ ] Persistent history
- [ ] Persistent settings
- [ ] Channel pages
- [ ] Search filters

### 0.3 — Power features
- [ ] Playback controls
- [ ] Sponsor / segment handling
- [ ] Playback filtering
- [ ] Account-aware features

---

## ✦ Maintainer

<div align="center">

### Leon Sony

**Project creator & maintainer**

[![GitHub](https://img.shields.io/badge/GitHub-Jackson4Rocks-1D1B20?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Jackson4Rocks)

**Build • Break • Improve • Repeat.**

</div>

---

<div align="center">

### 💜 PublivoreTube

**Open source. TV-first. Still evolving.**

</div>
