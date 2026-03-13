#!/bin/bash
# Quick performance fix for NativeChatActivity.kt

FILE="app/src/main/java/org/ekatra/alfred/NativeChatActivity.kt"

echo "Applying performance optimizations to $FILE..."

# Create a Python script to fix the file
python3 << 'PYTHON_END'
import re

file_path = "app/src/main/java/org/ekatra/alfred/NativeChatActivity.kt"

with open(file_path, 'r') as f:
    content = f.read()

# Replace the slow .map operations with direct index updates
# Pattern 1: Update message text during streaming
old_pattern1 = r'''messages = messages\.map \{
\s+if \(it\.id == aiMsgId\) \{
\s+it\.copy\(text = response\.toString\(\)\)
\s+\} else it
\s+\}'''

new_code1 = '''val idx = messages.indexOfLast { it.id == aiMsgId }
                                            if (idx >= 0) {
                                                messages = messages.toMutableList().apply {
                                                    this[idx] = this[idx].copy(text = response.toString())
                                                }
                                            }'''

content = re.sub(old_pattern1, new_code1, content)

# Pattern 2: Mark message as complete
old_pattern2 = r'''messages = messages\.map \{
\s+if \(it\.id == aiMsgId\) \{
\s+it\.copy\(isStreaming = false\)
\s+\} else it
\s+\}'''

new_code2 = '''val idx = messages.indexOfLast { it.id == aiMsgId }
                                        if (idx >= 0) {
                                            messages = messages.toMutableList().apply {
                                                this[idx] = this[idx].copy(isStreaming = false)
                                            }
                                        }'''

content = re.sub(old_pattern2, new_code2, content)

# Write back
with open(file_path, 'w') as f:
    f.write(content)

print(f"Optimizations applied!")

PYTHON_END

echo "Done! Building APK..."
