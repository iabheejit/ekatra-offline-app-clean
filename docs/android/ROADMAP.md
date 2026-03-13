# Maya AI - Product Roadmap

> **App Name**: Maya AI (formerly Ekatra Alfred)  
> **Package**: `org.ekatra.alfred`  
> **Mission**: Offline AI education for every student, everywhere

---

## 📍 Current Status (v1.0.0)

### ✅ Completed Features

| Feature | Status | Notes |
|---------|--------|-------|
| On-device LLM inference | ✅ Done | llama.cpp + JNI |
| Native Compose UI | ✅ Done | Modern chat interface |
| Model download | ✅ Done | In-app from Hugging Face |
| Conversation history | ✅ Done | Room/SQLite persistence |
| Multi-device server | ✅ Done | NanoHTTPD, share via WiFi |
| Streaming responses | ✅ Done | Real-time token display |
| Context management | ✅ Done | Smart summarization |
| Dark mode | ✅ Done | System theme |
| Professional versioning | ✅ Done | Semantic versioning |
| Play Store prep | ✅ Done | Privacy policy, checklist |

### 📱 Current Model
- **Model**: Qwen 2.5 0.5B Instruct (Q4_K_M)
- **Size**: ~500 MB
- **Context**: 2048 tokens
- **Performance**: 10-15 sec first token on mid-range device

---

## 🗺️ Version Roadmap

### v1.1.0 - "Personal Touch" 🎯 NEXT
*Target: Q1 2026*

#### 🔄 Model Upgrade
- [ ] **Switch to MobileLLM-R1-360M**
  - Source: https://huggingface.co/DevQuasar/facebook.MobileLLM-R1-360M-GGUF
  - Smaller (~360 MB), optimized for mobile
  - Faster inference, lower RAM usage
  - Maintains quality for educational use

#### 👤 User Profile & Personalization
- [ ] **User registration (offline-first)**
  - Name, optional profile photo
  - Stored locally in SQLite
  - Syncs to Ekatra cloud when online (optional)
  
- [ ] **Profile photo upload**
  - Camera capture or gallery pick
  - Circular avatar in header
  - Compressed storage (< 100 KB)
  
- [ ] **Personalized greetings**
  - "Good morning, Priya!"
  - Use name in AI responses
  - Birthday wishes (if provided)

#### 🔊 Voice Output (Text-to-Speech)
- [ ] **Android TTS integration**
  - Read AI responses aloud
  - Play button on each message
  - Auto-read option in settings
  - Multiple language voices
  
- [ ] **Voice settings**
  - Speed control (0.5x - 2x)
  - Voice selection
  - Enable/disable per session

#### 📊 User Analytics (Optional Sync)
- [ ] **Registration with Ekatra backend**
  - Anonymous device ID
  - Optional: name, school, grade
  - Sync only when user chooses
  
- [ ] **Usage tracking (local)**
  - Questions asked per day
  - Subjects explored
  - Time spent learning
  - All stored locally first

---

### v1.2.0 - "Speak to Learn" 🎤
*Target: Q2 2026*

#### 🎙️ Voice Input (Speech-to-Text)
- [ ] **Android speech recognition**
  - Microphone button in chat
  - Works offline (device STT)
  - Hindi/Hinglish support
  
- [ ] **Continuous conversation mode**
  - Push-to-talk or auto-detect
  - Great for hands-free learning

#### 🌐 Multi-Language Support
- [ ] **Hindi interface**
  - Translated UI strings
  - Hindi TTS voice
  - Model understands Hindi
  
- [ ] **Regional languages**
  - Marathi, Tamil, Telugu, Bengali
  - Progressive rollout

#### 📚 Subject-Specific Modes
- [ ] **Math solver mode**
  - Step-by-step solutions
  - Calculation verification
  - Equation formatting
  
- [ ] **Science explainer mode**
  - Concept breakdown
  - Real-world examples
  - Diagram suggestions

---

### v1.3.0 - "Classroom Ready" 📡
*Target: Q3 2026*

#### 👩‍🏫 Teacher Dashboard
- [ ] **Teacher account type**
  - Manage student group
  - View class statistics
  - Assign topics
  
- [ ] **Class analytics**
  - Popular questions
  - Subject coverage
  - Engagement metrics

#### 📖 Curriculum Packs
- [ ] **Offline content bundles**
  - NCERT-aligned modules
  - Download once, use forever
  - State board variations
  
- [ ] **Guided lessons**
  - Structured learning paths
  - Progress tracking
  - Quizzes/assessments

#### 🤝 Enhanced Sharing
- [ ] **Classroom mode**
  - One teacher phone → 30 students
  - Question queue management
  - Broadcast mode

---

### v2.0.0 - "Smart Learning" 🧠
*Target: Q4 2026*

#### 📷 Visual Learning
- [ ] **Camera integration**
  - OCR for textbook pages
  - Solve problems from photos
  - Diagram recognition
  
- [ ] **Handwriting recognition**
  - Math equations
  - Chemistry formulas

#### 🔄 Smart Sync
- [ ] **Cloud backup**
  - Optional Ekatra account
  - Sync across devices
  - Family sharing

---

## ⚡ Performance & Accessibility (Priority)

### Performance
- [ ] **Faster cold start** - Target < 2 seconds

### Accessibility
- [ ] **Large text support** - Scalable fonts

---

## 🏗️ Technical Architecture

### Current Stack
```
┌─────────────────────────────────────────────────┐
│                  Jetpack Compose                 │
│              (Native Chat Interface)             │
├─────────────────────────────────────────────────┤
│              ViewModel + StateFlow               │
│           (Reactive UI State Management)         │
├─────────────────────────────────────────────────┤
│              Room Database (SQLite)              │
│        (Conversations, Profiles, Settings)       │
├─────────────────────────────────────────────────┤
│              LlamaEngine (Kotlin)                │
│         (Model Loading, Inference Control)       │
├─────────────────────────────────────────────────┤
│              JNI Bridge (C++)                    │
│              llama_jni.cpp                       │
├─────────────────────────────────────────────────┤
│              llama.cpp                           │
│         (Optimized CPU Inference)                │
└─────────────────────────────────────────────────┘
```

### Planned Additions
```
┌─────────────────────────────────────────────────┐
│              Android TTS / STT                   │
│         (Voice Input/Output - System APIs)       │
├─────────────────────────────────────────────────┤
│              User Profile Manager                │
│    (Registration, Photo, Preferences)            │
├─────────────────────────────────────────────────┤
│              Ekatra Sync Service                 │
│    (Optional Cloud Sync when Online)             │
└─────────────────────────────────────────────────┘
```

---

## 📊 Success Metrics

### User Experience
| Metric | v1.0 | v1.1 Target | v2.0 Target |
|--------|------|-------------|-------------|
| Cold start time | 5s | 3s | 2s |
| First token latency | 12s | 8s | 5s |
| RAM usage (active) | 1.2 GB | 1 GB | 800 MB |
| APK size | 15 MB | 12 MB | 10 MB |
| Model size | 500 MB | 360 MB | 360 MB |

### Engagement (Future)
| Metric | Target |
|--------|--------|
| Daily active users | 10,000+ |
| Questions per session | 5+ |
| Retention (Day 7) | 40%+ |
| Play Store rating | 4.5+ |

---

## 🔧 Model Configuration

### Current (v1.0.0)
```kotlin
// Qwen 2.5 0.5B Instruct
modelPath = "qwen-0.5b-q4.gguf"
contextSize = 2048
maxTokens = 256
temperature = 0.7
```

### Planned (v1.1.0)
```kotlin
// Facebook MobileLLM-R1-360M
modelPath = "mobilellm-r1-360m-q4.gguf"
contextSize = 2048
maxTokens = 256
temperature = 0.7

// Benefits:
// - 30% smaller model file
// - Optimized for mobile CPU
// - Lower memory footprint
// - Comparable quality for education
```

### Model Source
- **HuggingFace**: https://huggingface.co/DevQuasar/facebook.MobileLLM-R1-360M-GGUF
- **Original**: Facebook Research MobileLLM
- **Quantization**: Q4_K_M (best size/quality balance)

---

## 📁 Project Structure (Planned)

```
app/src/main/java/org/ekatra/alfred/
├── EkatraApp.kt                    # Application class
├── MainActivity.kt                 # Entry point
│
├── data/
│   ├── local/
│   │   ├── EkatraDatabase.kt       # Room database
│   │   ├── UserProfileDao.kt       # Profile CRUD
│   │   ├── ConversationDao.kt      # Chat history
│   │   └── SettingsDao.kt          # Preferences
│   │
│   ├── model/
│   │   ├── UserProfile.kt          # User data class
│   │   ├── Conversation.kt         # Chat session
│   │   ├── Message.kt              # Individual message
│   │   └── AppSettings.kt          # Settings model
│   │
│   ├── remote/                     # v1.1+ - Optional sync
│   │   ├── EkatraApi.kt            # API interface
│   │   └── SyncService.kt          # Background sync
│   │
│   └── repository/
│       ├── UserRepository.kt       # Profile management
│       ├── ChatRepository.kt       # Conversations
│       └── SyncRepository.kt       # Cloud sync
│
├── llm/
│   ├── LlamaEngine.kt              # Model inference
│   ├── ModelDownloader.kt          # Download manager
│   └── PromptBuilder.kt            # System prompts
│
├── voice/                          # v1.1+
│   ├── TextToSpeechManager.kt      # TTS wrapper
│   └── SpeechRecognitionManager.kt # STT wrapper (v1.2)
│
├── ui/
│   ├── screens/
│   │   ├── ChatScreen.kt           # Main chat
│   │   ├── ProfileScreen.kt        # User profile
│   │   ├── SettingsScreen.kt       # Settings
│   │   ├── DownloadScreen.kt       # Model download
│   │   └── OnboardingScreen.kt     # First-time setup
│   │
│   ├── components/
│   │   ├── MessageBubble.kt        # Chat message
│   │   ├── ProfileAvatar.kt        # User photo
│   │   ├── VoiceButton.kt          # TTS/STT controls
│   │   └── ThinkingIndicator.kt    # Loading state
│   │
│   └── theme/
│       ├── Color.kt
│       ├── Typography.kt
│       └── Theme.kt
│
└── util/
    ├── PhotoUtils.kt               # Image handling
    ├── ShareUtils.kt               # Share intents
    └── PermissionUtils.kt          # Runtime permissions
```

---

## 🔐 Privacy & Data

### Data Storage Philosophy
1. **Local-first**: Everything stored on device by default
2. **User control**: Explicit opt-in for any cloud sync
3. **Minimal data**: Only collect what's necessary
4. **Transparent**: Clear explanation of data usage

### Registration Flow (v1.1+)
```
1. User opens app → Works immediately (no login required)
2. Optional: "Create Profile" in settings
3. Enter: Name, optional photo, optional school/grade
4. Stored locally in SQLite
5. Optional: "Sync to Ekatra Cloud" for backup
6. Sync requires explicit permission
```

### Data Synced to Cloud (Optional)
| Data | Synced? | Purpose |
|------|---------|---------|
| User name | ✅ If opted | Personalization |
| Profile photo | ✅ If opted | Avatar |
| Device ID | ✅ Anonymous | Analytics |
| Conversation content | ❌ Never | Privacy |
| Usage statistics | ✅ Aggregated | Product improvement |

---

## 🚀 Next Steps (Immediate)

### Week 1-2: v1.1 Foundation
1. [ ] Integrate MobileLLM-R1-360M model
2. [ ] Create UserProfile Room entity
3. [ ] Add profile photo capture/upload
4. [ ] Implement Android TTS

### Week 3-4: v1.1 Polish
5. [ ] Personalized greetings in chat
6. [ ] Voice settings UI
7. [ ] Registration flow
8. [ ] Testing & optimization

### Release
9. [ ] Update Play Store listing
10. [ ] Staged rollout (10% → 50% → 100%)

---

## 📞 Contact & Contribution

- **GitHub**: https://github.com/ekatra
- **Email**: team@ekatra.org
- **Discord**: [Ekatra Community]

---

*Last Updated: February 4, 2026*
*Version: 1.0.0*
