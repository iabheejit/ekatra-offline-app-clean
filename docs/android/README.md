# Ekatra Alfred - Android App

Offline AI Learning Companion with WebView UI + Native LLM Backend + Multi-Device Server

## 🎯 Architecture

```
┌─────────────────────────────────────────────────┐
│                  WebView UI                      │
│            (index.html - Chat Interface)         │
├─────────────────────────────────────────────────┤
│              JavaScript Bridge                   │
│         (AlfredBridge - sendMessage, etc)        │
├─────────────────────────────────────────────────┤
│              Kotlin Layer                        │
│  ┌─────────────┐      ┌─────────────────────┐   │
│  │ LlamaEngine │      │ AlfredServer        │   │
│  │ (Inference) │      │ (HTTP for clients)  │   │
│  └─────────────┘      └─────────────────────┘   │
├─────────────────────────────────────────────────┤
│              Native JNI (C++)                    │
│              llama_jni.cpp                       │
├─────────────────────────────────────────────────┤
│              llama.cpp                           │
│         (Model loading & inference)              │
└─────────────────────────────────────────────────┘
```

## 📁 Project Structure

```
ekatra-android/
├── app/
│   ├── src/main/
│   │   ├── java/org/ekatra/alfred/
│   │   │   ├── EkatraApp.kt        # Application class
│   │   │   ├── MainActivity.kt      # WebView + JS bridge
│   │   │   ├── LlamaEngine.kt       # LLM inference wrapper
│   │   │   ├── AlfredServer.kt      # HTTP server for multi-device
│   │   │   └── AlfredServerService.kt # Foreground service
│   │   ├── assets/
│   │   │   └── index.html           # Chat UI (HTML/CSS/JS)
│   │   ├── res/                     # Android resources
│   │   └── cpp/
│   │       ├── CMakeLists.txt       # Native build config
│   │       └── llama_jni.cpp        # JNI bindings (STUB)
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 🚀 Quick Start

### Prerequisites

1. **Android Studio** (latest stable)
2. **NDK** (r25b or later) - Install via SDK Manager
3. **CMake 3.22.1** - Install via SDK Manager

### Build Steps

1. **Open in Android Studio**
   ```bash
   # From this directory
   open -a "Android Studio" .
   ```

2. **Sync Gradle**
   - Android Studio will prompt to sync
   - Click "Sync Now"

3. **Build APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or run: `./gradlew assembleDebug`

4. **Install on Device**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Add the Model

The app expects the model at:
```
/data/data/org.ekatra.alfred/files/models/qwen-0.5b-q4.gguf
```

To copy via ADB:
```bash
# First, download the model
# https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF

# Push to device (requires debug build or rooted device)
adb shell mkdir -p /data/local/tmp/
adb push qwen-0.5b-q4.gguf /data/local/tmp/
adb shell run-as org.ekatra.alfred cp /data/local/tmp/qwen-0.5b-q4.gguf files/models/
```

## 🔧 Integrating Real llama.cpp

The current native code is a **STUB**. To use real inference:

### Option 1: Use Pre-built AAR (Recommended)

```bash
# Download pre-built llama.cpp Android library
# Add to app/libs/llama.aar
# Update build.gradle.kts:
# implementation(files("libs/llama.aar"))
```

### Option 2: Build from Source

1. Clone llama.cpp:
   ```bash
   cd ekatra-android
   git clone https://github.com/ggerganov/llama.cpp.git lib/llama.cpp
   ```

2. Update `CMakeLists.txt`:
   ```cmake
   set(LLAMA_CPP_DIR "${CMAKE_SOURCE_DIR}/../../../../lib/llama.cpp")
   add_subdirectory(${LLAMA_CPP_DIR} llama.cpp)
   target_link_libraries(llama_jni llama)
   ```

3. Update `llama_jni.cpp` to use real llama.cpp calls (see comments in file)

## 📱 Features

### Chat Interface
- Clean mobile-optimized UI
- Typing indicators
- Suggestion chips for quick start
- Smooth animations

### Server Mode
- Enable in Settings
- Other devices on same WiFi can connect
- Ollama-compatible API endpoints:
  - `GET /status` - Server status
  - `POST /api/chat` - Send message, get response

### Client Mode
- Connect to another Ekatra device's server
- Share one device's LLM with multiple clients

## 🔒 Security

- Model stored in app-private storage
- No external storage permissions
- Input validation and length limits
- System prompt hardcoded (not user-modifiable)
- Server only binds to local network

## 📊 Performance Targets

| Metric | Target | Notes |
|--------|--------|-------|
| First token | < 3 sec | On $50 phones |
| Token rate | 5-10 t/s | Varies by device |
| APK size | ~20 MB | Without model |
| Model size | ~400 MB | Qwen 2.5 0.5B Q4 |
| RAM usage | < 1 GB | During inference |

## 🐛 Troubleshooting

### "Model not found"
- Ensure model file exists at the correct path
- Check file permissions

### Native library not loading
- Verify NDK is installed
- Check `abiFilters` in build.gradle.kts matches device

### Server not accessible
- Check WiFi connection
- Verify IP address
- Ensure firewall allows port 8080

## 📝 API Reference

### JavaScript Bridge (window.Alfred)

```javascript
// Check if model is ready
Alfred.isModelReady() → boolean

// Get model status
Alfred.getModelStatus() → string

// Send message (blocking)
Alfred.sendMessage(text) → string

// Send message (streaming)
Alfred.sendMessageAsync(text, callbackId)
// Calls window.nativeCallback(callbackId, 'token'|'done'|'error', data)

// Server control
Alfred.startServer() → string (URL)
Alfred.stopServer()
Alfred.isServerRunning() → boolean
Alfred.getServerUrl() → string
```

### HTTP API (Server Mode)

```bash
# Check status
curl http://192.168.x.x:8080/status

# Send message
curl -X POST http://192.168.x.x:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello!"}'
```

---

## 📋 Documentation

| Document | Description |
|----------|-------------|
| [ROADMAP.md](ROADMAP.md) | Product roadmap & planned features |
| [CHANGELOG.md](CHANGELOG.md) | Version history |
| [GOOGLE_PLAY_CHECKLIST.md](GOOGLE_PLAY_CHECKLIST.md) | Play Store submission guide |
| [PRIVACY_POLICY.md](PRIVACY_POLICY.md) | Privacy policy |
| [GRASSROOTS_PLAN.md](GRASSROOTS_PLAN.md) | Original grassroots design |
| [PERFORMANCE_GUIDE.md](PERFORMANCE_GUIDE.md) | Optimization tips |
| [CONTEXT_MANAGEMENT.md](CONTEXT_MANAGEMENT.md) | LLM context handling |

---

## 🤝 Contributing

Maya AI is built by Ekatra for students everywhere.

- **Report bugs**: Open an issue
- **Feature requests**: Discuss in issues
- **Pull requests**: Welcome!

---

## 📄 License

MIT License - Built for education

**Made with ❤️ by Ekatra**
