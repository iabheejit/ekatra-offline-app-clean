# Ekatra Alfred Android App - UI Design Brief

## App Overview
**Purpose:** On-device AI learning companion for students aged 10-18  
**Tech:** Native Android app with WebView-based chat interface + llama.cpp inference  
**Current APK Size:** 21 MB (without model)  
**Model Size:** ~400 MB (downloads on first launch)

---

## 🔄 USER FLOW

### **Flow 1: First Launch (New User)**
```
App Launch
    ↓
[SPLASH/LOADING SCREEN]
- Shows: Ekatra logo + "Loading Alfred..."
- Background: Light gradient (#F0F4F8 to #E8EEF5)
- Duration: While checking for model file
    ↓
[MODEL DOWNLOAD SCREEN]
- Shows: "Model not found. Ready to download (~400 MB)?"
- Elements:
  * Animated AI illustration/icon
  * File size warning
  * "Download Model" button (blue, prominent)
  * Progress bar (hidden initially)
  * Status text (updates during download)
- User Action: Taps "Download Model"
    ↓
[DOWNLOADING STATE]
- Shows progress bar (horizontal, blue gradient)
- Updates text: "Downloading model... 45% (180/400 MB)"
- Retry button appears on failure
- Duration: ~5-10 minutes depending on connection
    ↓
[LOADING MODEL STATE]
- Shows: "Loading model..."
- Spinner animation
- Duration: ~10-30 seconds
    ↓
[CHAT INTERFACE] (Main App)
```

### **Flow 2: Returning User**
```
App Launch
    ↓
[LOADING SCREEN]
- Shows: "Loading model..."
- Duration: ~10-30 seconds
    ↓
[CHAT INTERFACE] (Main App)
```

---

## 📱 SCREEN BREAKDOWN

### **SCREEN 1: Chat Interface (Main Screen)**

#### **A. Header Bar**
**Current Implementation:**
- **Left Side:**
  - Avatar circle (40x40px) with 🎓 emoji
  - Text:
    - "Alfred" (18px, bold, white)
    - Status: "Ready" or "Generating..." (12px, white with 90% opacity)
    - Status dot (8px circle): Yellow (loading), Green (ready), Red (error)
  
- **Right Side:**
  - Settings icon button (⚙️, 36x36px circle)

**Current Styling:**
- Background: Blue gradient (135deg, #2563EB to #1E40AF)
- Height: ~72px
- Padding: 16px 20px
- Shadow: 0 2px 8px rgba(0,0,0,0.15)

**Design Request:**
- Make header more modern/playful for student audience
- Consider replacing emoji avatar with actual Alfred character illustration
- Add visual feedback for "thinking" state (e.g., animated dots)

---

#### **B. Chat Area**
**Current Implementation:**
- Scrollable message container
- Background: Light blue-gray (#E8EEF5)
- Padding: 16px

**Welcome Message (Empty State):**
```
👋 Welcome to Ekatra!
I'm Alfred, your AI learning companion.
Ask me anything - I'm here to help you learn!
```

**Message Bubbles:**

**User Messages:**
- Alignment: Right
- Background: Blue (#2563EB)
- Text: White
- Border radius: 18px (6px on bottom-right)
- Max width: 85% of screen
- Padding: 12px 16px
- Animation: Fade in + slide up

**AI (Alfred) Messages:**
- Alignment: Left
- Background: White
- Text: Dark (#1E293B)
- Has "Alfred" label above (11px, blue, bold)
- Border radius: 18px (6px on bottom-left)
- Max width: 85% of screen
- Padding: 12px 16px
- Shadow: 0 1px 3px rgba(0,0,0,0.1)
- Animation: Fade in + slide up

**Typing Indicator:**
- 3 bouncing dots (8px circles)
- Color: Gray (#64748B)
- Appears while AI is generating

**Design Request:**
- Make message bubbles more engaging for students
- Consider adding message timestamps
- Add avatars to messages (user photo + Alfred character)
- Improve typing indicator animation
- Add support for rich content (code blocks, math formulas, images)
- Consider adding "copy" and "regenerate" buttons to AI messages

---

#### **C. Quick Suggestions Bar**
**Current Implementation:**
- Horizontal scrollable row
- Background: Same as chat area (#E8EEF5)
- Border top: 1px solid rgba(0,0,0,0.05)
- Padding: 12px 16px

**Suggestion Chips:**
- Current suggestions:
  1. 🌱 Photosynthesis
  2. 🔢 Fractions
  3. 🌧️ Weather
  4. 🌍 Solar System
- Style: White background, blue text, rounded (20px), 1px border
- Hover: Blue background, white text
- Size: 14px text, 8px 16px padding

**Design Request:**
- Make chips more visually appealing
- Consider dynamic suggestions based on conversation
- Add more relevant student topics (10-18 age range)
- Improve visual hierarchy

---

#### **D. Input Area**
**Current Implementation:**
- Fixed at bottom
- Background: White
- Shadow: 0 -2px 8px rgba(0,0,0,0.05)
- Padding: 12px 16px

**Components:**
- **Text Input:**
  - Background: Light gray (#F0F4F8)
  - Border radius: 24px
  - Padding: 8px 16px
  - Placeholder: "Ask me anything..."
  - Auto-resize (max 120px height)
  - Multi-line support

- **Send Button:**
  - Size: 48x48px circle
  - Background: Blue (#2563EB)
  - Icon: Paper plane (SVG)
  - States:
    - Normal: Blue
    - Hover: Darker blue + scale 1.05
    - Active: Scale 0.95
    - Disabled: Gray (#CBD5E1)

**Design Request:**
- Make input area more inviting
- Add microphone button for voice input (future)
- Add attachment button for images/documents (future)
- Improve send button animation
- Add character counter if needed

---

### **SCREEN 2: Settings Panel**

**Current Implementation:**
- Slides in from right (full width overlay on mobile)
- Max width: 400px on tablets
- Background: White
- Shadow: -4px 0 16px rgba(0,0,0,0.1)

**Header:**
- Background: Blue (#2563EB)
- Text: "Settings" (18px, white)
- Close button (X icon)

**Content Sections:**

1. **Server Mode Toggle**
   - Label: "Multi-Device Mode"
   - Description: "Let other devices connect to this device"
   - Toggle switch (48x28px, blue when active)
   - Server info box (shows IP address when active)

2. **Model Settings** (Currently not visible but can add)
   - Temperature slider
   - Max tokens setting
   - System prompt customization

3. **About Section**
   - App version
   - Model info
   - Credits

**Design Request:**
- Modernize settings panel
- Better visual hierarchy
- Add icons to each setting
- Improve toggle switch design
- Add more settings options:
  - Theme selection (Light/Dark)
  - Font size adjustment
  - Chat history management
  - Privacy settings

---

### **SCREEN 3: Model Download/Loading Screen**

**Current Implementation:**
- Full screen overlay
- Background: Light (#F0F4F8)
- Centered content:
  - Circular progress spinner (48px)
  - Loading text (16px, blue)
  - Download progress bar (horizontal, hidden until needed)
  - Download button (blue, rounded)

**States:**

1. **Initial Check:**
   - Shows: "Checking for model..."
   - Spinner visible

2. **Model Missing:**
   - Shows: "Model not found. Ready to download (~400 MB)?"
   - Download button visible
   - Progress bar hidden

3. **Downloading:**
   - Shows: "Downloading model... 45% (180/400 MB)"
   - Progress bar visible (blue gradient fill)
   - Button hidden

4. **Download Failed:**
   - Shows: "Download failed: [error message]\n\nTap to retry"
   - Retry button visible
   - Progress bar hidden

5. **Loading Model:**
   - Shows: "Loading model..."
   - Spinner visible

**Design Request:**
- Create engaging loading animations
- Add illustration/character animation during download
- Show estimated time remaining
- Add pause/resume functionality
- Better error state designs with helpful messages
- Add WiFi-only download option
- Show network speed indicator

---

## 🎨 CURRENT COLOR PALETTE

```css
Primary Blue: #2563EB
Primary Dark: #1E40AF
Accent Blue: #3B82F6
Success Green: #22C55E
Warning Orange: #F59E0B
Background: #F0F4F8
Chat Background: #E8EEF5
User Bubble: #2563EB
AI Bubble: #FFFFFF
Text Dark: #1E293B
Text Light: #64748B
```

**Design Request:**
- Refine color palette for better accessibility
- Add more colors for different message types (system, error, info)
- Consider dark mode palette

---

## 🎭 CHARACTER DESIGN NEEDS

### **Alfred Character**
**Current:** Just an emoji (🎓)

**Needed:**
1. **Avatar/Profile Image** (40x40px for header)
2. **Full Character Illustration** (for loading/empty states)
3. **Animated States:**
   - Idle
   - Thinking/Processing
   - Success/Happy
   - Error/Confused
4. **Multiple Expressions** for different contexts

**Personality Traits:**
- Friendly, approachable
- Educational but not boring
- Encouraging and supportive
- Age-appropriate for 10-18 year olds

---

## 📐 TECHNICAL CONSTRAINTS

### **Display Specs:**
- Target: Android phones (4.5" to 6.7")
- Tablets supported (responsive up to 10")
- Portrait and landscape orientations
- Min Android version: 7.0 (API 24)

### **WebView Container:**
- Uses Android WebView
- Renders HTML/CSS/JavaScript
- JavaScript bridge to native (Alfred.sendMessage, etc.)
- All UI is web-based (not native Android views)

### **File Locations:**
- Main UI: `/app/src/main/assets/index.html`
- Styles: Inline CSS in index.html
- JavaScript: Inline in index.html
- Native loading screen: `/app/src/main/res/layout/activity_main.xml`

### **Assets to Provide:**
Must be web-compatible formats:
- Images: SVG (preferred), PNG, WebP
- Animations: CSS animations, Lottie JSON, or animated SVG
- Icons: SVG or icon fonts
- No video files (app size constraints)

---

## 🎯 DESIGN PRIORITIES (Ranked)

### **HIGH Priority:**
1. **Loading/Download Screen** - First user impression
2. **Chat Message Bubbles** - Core interaction
3. **Alfred Character Design** - Brand identity
4. **Header Bar** - Always visible
5. **Input Area** - Primary action

### **MEDIUM Priority:**
6. **Welcome Screen/Empty State**
7. **Quick Suggestions**
8. **Settings Panel**
9. **Typing Indicator**
10. **Status Indicators**

### **LOW Priority:**
11. **Error States**
12. **Animations/Transitions**
13. **Dark Mode**
14. **Tablet Layouts**

---

## 🎨 DELIVERABLES REQUESTED

### **Phase 1: Core UI**
1. **Alfred Character Sheet:**
   - Main avatar (40x40px, 80x80px, 120x120px)
   - Full body illustration (various sizes)
   - 3-5 emotional states
   - Thinking animation (Lottie or CSS)

2. **Message Bubble Redesign:**
   - User message style
   - AI message style
   - Timestamp design
   - Action buttons (copy, regenerate)

3. **Loading Screen:**
   - Download prompt design
   - Progress bar style
   - Animated illustration
   - Error state designs

4. **Header Bar:**
   - New layout proposal
   - Status indicator designs
   - Icon buttons

5. **Input Area:**
   - Enhanced text input
   - Send button redesign
   - Additional action buttons

### **Phase 2: Enhancement**
6. **Settings Panel Redesign**
7. **Quick Suggestions Redesign**
8. **Welcome/Empty State**
9. **Dark Mode Palette**
10. **Animations Library**

### **Asset Format Requirements:**
- **SVG files** for all icons and illustrations (scalable)
- **PNG files** at 1x, 2x, 3x for raster images
- **Lottie JSON** for complex animations (optional)
- **Color codes** in HEX and RGB
- **Font specifications** (web-safe fonts or Google Fonts)
- **CSS code** for styles and animations

---

## 💬 CURRENT USER INTERACTIONS

### **JavaScript Bridge (Native ↔ Web)**

**From Web to Native:**
```javascript
// Send message and get streaming response
Alfred.sendMessageAsync(message, callbackId)

// Start/stop server mode
Alfred.startServer()
Alfred.stopServer()
Alfred.getServerStatus()
```

**From Native to Web:**
```javascript
// Notify when model is loaded
window.onNativeEvent('modelLoaded', 'true')

// Streaming token callback
window.onNativeEvent('token', tokenText)
```

### **Chat Flow:**
1. User types message → clicks send button
2. Message appears in chat (blue bubble, right aligned)
3. Typing indicator appears (AI thinking)
4. AI response streams in word-by-word
5. Response completes → typing indicator disappears
6. Suggestion chips update (optional)

---

## 🔧 TECHNICAL NOTES FOR DESIGNER

### **Current Implementation Files:**
- **Web UI:** `app/src/main/assets/index.html` (863 lines, all-in-one file)
- **Native Loading:** `app/src/main/res/layout/activity_main.xml`
- **Colors:** `app/src/main/res/values/colors.xml`

### **CSS Framework:**
- Pure CSS (no framework)
- CSS Grid & Flexbox for layout
- CSS animations for transitions
- Responsive design with media queries

### **Font Stack:**
```css
font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
```

### **Accessibility Considerations:**
- Minimum touch target: 48x48dp
- Color contrast ratio: 4.5:1 minimum
- Font size: 14px minimum for body text
- Support for system font scaling

### **Performance:**
- Keep images < 100KB each
- Use CSS animations over JavaScript
- Minimize repaints/reflows
- Lazy load images if possible

---

## 📋 EXAMPLE USE CASES

### **Scenario 1: Math Homework Help**
```
User: "Help me understand fractions"
Alfred: "Great question! Let me explain fractions in a simple way.
        Think of fractions like slicing a pizza..."
[Includes simple diagram/illustration]
```

### **Scenario 2: Science Explanation**
```
User: "What causes rain?"
Alfred: "Rain is part of the water cycle! Here's how it works:
        1. Water evaporates from oceans/lakes
        2. Forms clouds in the sky
        3. Cools down and falls as rain"
[Could include animated water cycle diagram]
```

### **Scenario 3: General Conversation**
```
User: "I'm nervous about my test tomorrow"
Alfred: "It's normal to feel nervous! Here are some tips:
        - Get a good night's sleep
        - Review your notes once more
        - Take deep breaths before the test
        You've got this! 💪"
```

---

## 🎯 BRAND IDENTITY

### **Ekatra Brand:**
- **Mission:** Make education accessible through AI
- **Target:** Students 10-18 years old (middle school to high school)
- **Tone:** Friendly, educational, encouraging, trustworthy
- **Values:** Accessibility, Privacy, Learning, Empowerment

### **Alfred Personality:**
- Helpful teaching assistant
- Patient and encouraging
- Age-appropriate language
- Non-judgmental
- Celebrates learning progress

---

## 📝 DESIGN REVIEW CHECKLIST

When delivering designs, please ensure:

- ✅ Works on screens 4.5" to 6.7" (320px to 414px width)
- ✅ Touch targets minimum 48x48dp
- ✅ Color contrast meets WCAG AA standards
- ✅ Includes both light and dark variants (if applicable)
- ✅ All interactive elements have hover/active states
- ✅ Loading states designed for all actions
- ✅ Error states include helpful recovery messages
- ✅ Animations have reduced-motion alternatives
- ✅ Text is readable at default system sizes
- ✅ Designs consider users with color blindness
- ✅ Asset files optimized for mobile (file size)
- ✅ Style guide/documentation included

---

## 📞 QUESTIONS TO ANSWER

Please address these in your design:

1. **Character Design:** What personality should Alfred have? (e.g., robot, owl, wizard, friendly blob?)
2. **Animation Style:** Subtle or playful? Educational tone or fun?
3. **Age Appropriateness:** How to balance appeal for 10yo vs 18yo?
4. **Information Density:** How much detail to show in messages?
5. **Loading Experience:** How to make 400MB download feel acceptable?
6. **Trust Signals:** How to show AI is working/thinking/safe?
7. **Error Recovery:** How to help users when things go wrong?
8. **Onboarding:** Should there be a tutorial or tips on first launch?

---

## 📦 CURRENT BUILD STATUS

**Latest APK:**
- File: `app-debug.apk`
- Size: 21 MB
- Build date: February 4, 2026
- Status: ✅ Fully functional with model download

**Ready for Testing:**
- Install APK on Android device
- App downloads model on first launch
- Chat interface fully working
- llama.cpp inference running on-device

---

## 🚀 FUTURE FEATURES (Consider in Design)

**Phase 2 (Not yet implemented):**
- Voice input/output
- Image attachments for visual questions
- Math formula rendering (LaTeX)
- Code syntax highlighting
- Study progress tracking
- Conversation history/search
- Multi-language support
- Offline indicator/mode

**Design Consideration:** Leave room in UI for these features

---

## END OF BRIEF

**Summary:** We need a modern, student-friendly UI redesign for an offline AI learning app. Current implementation is functional but basic. Focus on making Alfred feel like a helpful tutor that students actually want to interact with.

**Timeline:** Flexible, but prioritize Phase 1 deliverables first.

**Contact:** [Provide your contact info for design questions]
