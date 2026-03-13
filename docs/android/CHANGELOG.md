# Changelog

All notable changes to Maya AI (Ekatra Alfred) will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### v1.1.0 - "Personal Touch" (Planned)

#### Model Upgrade
- Switch to Facebook MobileLLM-R1-360M (smaller, faster)
- Source: https://huggingface.co/DevQuasar/facebook.MobileLLM-R1-360M-GGUF
- ~30% smaller model file, lower RAM usage

#### User Profile & Personalization
- User registration (offline-first with optional cloud sync)
- Profile photo upload (camera or gallery)
- Name retention for personalized greetings
- User name appears in AI responses ("Hello, Priya!")

#### Voice Output
- Android Text-to-Speech integration
- Read AI responses aloud
- Voice speed and language settings
- Auto-read mode option

#### Ekatra Integration
- Optional registration with Ekatra backend
- Usage analytics (local-first, sync when online)
- Device identification for user tracking

### v1.2.0 - "Speak to Learn" (Planned)
- Voice input (Speech-to-Text)
- Hindi/Hinglish support
- Multi-language interface
- Subject-specific learning modes

### v1.3.0 - "Classroom Ready" (Planned)
- Teacher dashboard
- Curriculum content packs
- Enhanced classroom sharing

---

## [1.0.0] - 2026-02-04

## [1.0.1] - 2026-02-04
### Changed
- Version bump for Play Store upload (internal testing track)

## [1.0.2] - 2026-02-04
### Changed
- Version bump for Play Store upload (internal testing track)

### Added
- **Core AI Engine**: On-device Qwen 0.5B model using llama.cpp
- **Native Chat Interface**: Jetpack Compose UI with streaming responses
- **Model Download**: In-app model download from Hugging Face
- **Socratic Learning Mode**: AI tutor that guides through questions
- **Conversation History**: SQLite-based persistence with Room
- **Multi-Device Server**: Share AI with nearby devices (local network)
- **Offline-First Architecture**: Works completely without internet after setup
- **Context Management**: Intelligent conversation summarization
- **Dark Mode**: System theme support

### Technical
- Native JNI integration with llama.cpp
- arm64-v8a and x86_64 architecture support
- ProGuard configuration for release builds
- Foreground service for server mode

### Security
- No data transmitted externally after model download
- All AI processing happens on-device
- No analytics or tracking

---

## [1.0.3] - 2026-02-04
### Changed
- Version bump for Play Store upload (internal testing track)

---

## [1.0.4] - 2026-02-05
### Changed
- Version bump for Play Store upload (internal testing track)

---

## Version Numbering

We use [Semantic Versioning](https://semver.org/):

- **MAJOR** (1.x.x): Breaking changes, major new features
- **MINOR** (x.1.x): New features, backward compatible
- **PATCH** (x.x.1): Bug fixes, minor improvements

### Version Code Formula
```
versionCode = MAJOR * 10000 + MINOR * 100 + PATCH
```
Examples:
- 1.0.0 → 10000
- 1.1.0 → 10100
- 1.1.5 → 10105
- 2.0.0 → 20000

---

## Release Types

| Type | Description | Example |
|------|-------------|---------|
| Alpha | Internal testing | 1.0.0-alpha.1 |
| Beta | Limited external testing | 1.0.0-beta.1 |
| RC | Release Candidate | 1.0.0-rc.1 |
| Release | Production release | 1.0.0 |
