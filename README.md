# Ekatra Offline Android App

A Python-based Android application using Kivy framework.

## Important Note

**Kivy currently has compatibility issues with Python 3.13.** 

For Android app development with Kivy, you need **Python 3.11** or earlier.

## Quick Start

### Option 1: Using Python 3.11 (Recommended for Kivy)

If you have Python 3.11 installed:

```bash
# Create virtual environment with Python 3.11
python3.11 -m venv venv

# Activate virtual environment
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run the app on desktop
python main.py
```

### Option 2: Test Without Installing (View Code Only)

The app structure is ready. You can view and edit `main.py` to understand the app structure.

## Installing Python 3.11 on macOS

```bash
# Using Homebrew
brew install python@3.11

# Then recreate the venv with Python 3.11
rm -rf venv
python3.11 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

## Running the App

### Desktop Testing (Requires Kivy installed)

```bash
source venv/bin/activate
python main.py
```

## Building for Android

### Prerequisites

- Python 3.11 (or earlier, not 3.13)
- Java JDK 8 or 11
- Android SDK and NDK (Buildozer will download these)
- Buildozer

### System Dependencies (macOS)

```bash
brew install autoconf automake libtool pkg-config
brew install zlib sdl2 sdl2_image sdl2_ttf sdl2_mixer
```

### Build APK

```bash
source venv/bin/activate

# Build debug APK (first build takes 30-60 minutes)
buildozer -v android debug

# The APK will be in: bin/ekatraoffline-0.1-debug.apk
```

### Deploy to Device

```bash
# Connect your Android device via USB with USB debugging enabled
buildozer android debug deploy run
```

## Project Structure

```
ekatra-offline-app/
├── venv/                 # Virtual environment (Python 3.11)
├── main.py              # Main Kivy application
├── buildozer.spec       # Android build configuration
├── requirements.txt     # Python dependencies
├── setup.sh             # Setup script
└── README.md           # This file
```

## Customizing the App

Edit [main.py](main.py) to add your own functionality:

- Modify UI layout and widgets
- Add processing logic
- Integrate with local AI models or databases
- Add more screens and navigation

## Troubleshooting

### Python Version Issues

```bash
# Check Python version
python --version

# Should be Python 3.11.x or earlier for Kivy
```

### Buildozer Issues

```bash
# Clean build cache
buildozer android clean

# Update buildozer
pip install --upgrade buildozer
```

### Android Build Issues

- Ensure Java JDK 8 or 11 is installed
- Check that Android device has USB debugging enabled
- First build downloads ~2GB of dependencies
- Builds work best on Linux, macOS is supported but slower

## Alternative: React Native or Flutter

If you prefer modern mobile development frameworks:

- **React Native**: JavaScript/TypeScript based
- **Flutter**: Dart based, excellent performance
- Both have better Python 3.13 compatibility than Kivy

## Next Steps

1. Install Python 3.11
2. Recreate venv with Python 3.11
3. Install dependencies
4. Test on desktop
5. Build for Android when ready
