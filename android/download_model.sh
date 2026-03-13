#!/bin/bash
# Download and set up the Qwen model for Ekatra Alfred

set -e

MODEL_URL="https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf"
MODEL_NAME="qwen-0.5b-q4.gguf"
MODELS_DIR="models"

echo "🎓 Ekatra Alfred - Model Setup"
echo ""

# Create models directory
mkdir -p "$MODELS_DIR"

# Check if model already exists
if [ -f "$MODELS_DIR/$MODEL_NAME" ]; then
    echo "✅ Model already exists: $MODELS_DIR/$MODEL_NAME"
    echo ""
    echo "To push to device:"
    echo "  adb push $MODELS_DIR/$MODEL_NAME /data/local/tmp/"
    echo "  adb shell run-as org.ekatra.alfred mkdir -p files/models"
    echo "  adb shell run-as org.ekatra.alfred cp /data/local/tmp/$MODEL_NAME files/models/"
    exit 0
fi

echo "📥 Downloading Qwen 2.5 0.5B (Q4_K_M quantization)..."
echo "   Size: ~400 MB"
echo "   URL: $MODEL_URL"
echo ""

# Download with curl (shows progress)
curl -L -o "$MODELS_DIR/$MODEL_NAME" "$MODEL_URL" --progress-bar

echo ""
echo "✅ Download complete!"
echo ""
echo "📦 Model saved to: $MODELS_DIR/$MODEL_NAME"
echo ""
echo "📲 To push to Android device:"
echo ""
echo "  # Push to temp location"
echo "  adb push $MODELS_DIR/$MODEL_NAME /data/local/tmp/"
echo ""
echo "  # Copy to app's private storage"
echo "  adb shell run-as org.ekatra.alfred mkdir -p files/models"
echo "  adb shell run-as org.ekatra.alfred cp /data/local/tmp/$MODEL_NAME files/models/"
echo ""
echo "  # Verify"
echo "  adb shell run-as org.ekatra.alfred ls -la files/models/"
