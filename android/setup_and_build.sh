#!/bin/bash
# setup_and_build.sh - Complete setup and build script for Ekatra Alfred
# Run this to build the APK

set -e
cd "$(dirname "$0")"

echo "============================================================"
echo "🎓 Ekatra Alfred - Android Build Setup"
echo "============================================================"
echo ""

# Check for Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install JDK 17+:"
    echo "   brew install openjdk@17"
    exit 1
fi
echo "✅ Java found: $(java -version 2>&1 | head -1)"

# Check/Set ANDROID_HOME
if [ -z "$ANDROID_HOME" ]; then
    # Try common locations
    if [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
    elif [ -d "/usr/local/share/android-sdk" ]; then
        export ANDROID_HOME="/usr/local/share/android-sdk"
    elif [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    else
        echo ""
        echo "❌ Android SDK not found!"
        echo ""
        echo "Please install Android Studio from:"
        echo "   https://developer.android.com/studio"
        echo ""
        echo "Or install via command line:"
        echo "   brew install --cask android-commandlinetools"
        echo ""
        echo "Then set ANDROID_HOME:"
        echo "   export ANDROID_HOME=\$HOME/Library/Android/sdk"
        echo ""
        exit 1
    fi
fi
echo "✅ Android SDK: $ANDROID_HOME"

# Create local.properties
echo "sdk.dir=$ANDROID_HOME" > local.properties
echo "✅ Created local.properties"

# Check for NDK
NDK_DIR="$ANDROID_HOME/ndk"
if [ ! -d "$NDK_DIR" ] || [ -z "$(ls -A $NDK_DIR 2>/dev/null)" ]; then
    echo ""
    echo "⚠️  NDK not found. Installing..."
    "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --install "ndk;25.2.9519653" 2>/dev/null || {
        echo "❌ Could not install NDK automatically."
        echo "   Please install NDK via Android Studio:"
        echo "   Tools → SDK Manager → SDK Tools → NDK (Side by side)"
        exit 1
    }
fi
echo "✅ NDK found"

# Check for CMake
CMAKE_DIR="$ANDROID_HOME/cmake"
if [ ! -d "$CMAKE_DIR" ] || [ -z "$(ls -A $CMAKE_DIR 2>/dev/null)" ]; then
    echo ""
    echo "⚠️  CMake not found. Installing..."
    "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --install "cmake;3.22.1" 2>/dev/null || {
        echo "❌ Could not install CMake automatically."
        echo "   Please install CMake via Android Studio:"
        echo "   Tools → SDK Manager → SDK Tools → CMake"
        exit 1
    }
fi
echo "✅ CMake found"

# Check llama.cpp
if [ ! -d "lib/llama.cpp" ]; then
    echo ""
    echo "📥 Cloning llama.cpp..."
    git clone --depth 1 https://github.com/ggerganov/llama.cpp.git lib/llama.cpp
fi
echo "✅ llama.cpp present"

# Make gradlew executable
chmod +x gradlew

echo ""
echo "============================================================"
echo "🔨 Building APK..."
echo "============================================================"
echo ""

# Clean and build
./gradlew clean assembleDebug

echo ""
echo "============================================================"
echo "✅ BUILD COMPLETE!"
echo "============================================================"
echo ""
echo "📦 APK location:"
echo "   app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "📲 To install on device:"
echo "   adb install app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "📱 To download the model:"
echo "   ./download_model.sh"
echo ""
