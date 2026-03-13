# Google Play Store Submission Checklist

## Pre-Submission Requirements

### ✅ App Information (Required)

- [ ] **App Name**: Maya AI - Offline Learning Companion
- [ ] **Short Description** (80 chars max): Offline AI tutor for students. Learn without internet. Privacy-first.
- [ ] **Full Description** (4000 chars max): See below
- [ ] **App Category**: Education
- [ ] **Content Rating**: Complete questionnaire (expected: Everyone)
- [ ] **Contact Email**: support@ekatra.org
- [ ] **Privacy Policy URL**: Host PRIVACY_POLICY.md and provide URL

### ✅ Store Listing Assets

#### Screenshots (Required)
- [ ] Phone screenshots (min 2, max 8)
  - Recommended: 1080x1920 or 1440x2560
  - Show: Chat interface, model download, settings
- [ ] 7-inch tablet screenshots (optional)
- [ ] 10-inch tablet screenshots (optional)

#### Graphics
- [ ] **App Icon**: 512x512 PNG (32-bit, no alpha)
- [ ] **Feature Graphic**: 1024x500 PNG/JPG (shown on Play Store)
- [ ] **Promo Video**: YouTube URL (optional but recommended)

### ✅ Release Configuration

#### Signing
- [ ] Generate release keystore:
  ```bash
  keytool -genkey -v -keystore maya-ai-release.keystore \
    -alias maya-ai -keyalg RSA -keysize 2048 -validity 10000
  ```
- [ ] Store keystore securely (NEVER commit to git)
- [ ] Configure signing in build.gradle.kts
- [ ] Back up keystore in multiple secure locations

#### Build
- [ ] Update version code/name in build.gradle.kts
- [ ] Build release APK/AAB:
  ```bash
  ./gradlew bundleRelease
  ```
- [ ] Test release build on real device
- [ ] Verify ProGuard hasn't broken anything

### ✅ Policy Compliance

#### Google Play Policies
- [x] **Restricted Content**: No violations
- [x] **Intellectual Property**: Using MIT/Apache licensed code only
- [x] **Privacy**: Privacy policy compliant, no data collection
- [x] **Monetization**: No ads, no in-app purchases (v1)
- [x] **Ads**: None
- [x] **Families Policy**: Compliant (educational, no data collection)
- [x] **User Generated Content**: Conversations stored locally only

#### Data Safety Form
- [x] Data collection: None transmitted
- [x] Data sharing: None
- [x] Security practices: Encrypted in transit (model download)
- [x] Data deletion: Automatic on uninstall

### ✅ Technical Requirements

- [x] Target SDK 34 (Android 14)
- [x] Min SDK 24 (Android 7.0) - Covers 97%+ devices
- [x] 64-bit support (arm64-v8a, x86_64)
- [x] ProGuard enabled for release
- [ ] App Bundle (AAB) format for upload
- [ ] Deobfuscation mapping file uploaded

### ✅ Testing

- [ ] Test on multiple devices
  - [ ] Low-end device (2GB RAM)
  - [ ] Mid-range device (4GB RAM)
  - [ ] High-end device (8GB+ RAM)
- [ ] Test offline functionality
- [ ] Test model download on slow connection
- [ ] Verify all UI elements display correctly
- [ ] Check for memory leaks
- [ ] Battery usage testing

---

## Store Listing Content

### Short Description (80 chars)
```
Offline AI tutor for students. Learn without internet. Privacy-first.
```

### Full Description
```
🎓 MAYA AI - Your Offline Learning Companion

Learn anywhere, anytime - no internet required after initial setup!

Maya AI is a revolutionary educational app that brings AI-powered tutoring directly to your device. Perfect for students in areas with limited connectivity.

✨ KEY FEATURES

📚 SOCRATIC LEARNING
• AI tutor that guides you through questions
• Helps you discover answers, not just gives them
• Builds critical thinking skills

🔒 COMPLETE PRIVACY
• All AI processing happens on your device
• No data sent to servers
• No tracking or analytics
• Your conversations stay private

📶 FULLY OFFLINE
• Download the AI model once
• Works without internet forever
• Perfect for rural areas and travel

🤝 SHARE WITH FRIENDS
• Multi-device mode lets you share AI with nearby students
• One phone can help an entire classroom
• Connect over local WiFi

⚡ LIGHTWEIGHT & FAST
• Optimized for Android phones
• Works on devices with 4GB+ RAM
• Quick responses using efficient AI

🌍 DESIGNED FOR ACCESSIBILITY
• Simple, clean interface
• Easy to use for all ages
• Low storage requirements

PERFECT FOR:
• Students in rural areas
• Schools with limited internet
• Self-directed learners
• Anyone who values privacy

SUBJECTS COVERED:
• Mathematics
• Science (Physics, Chemistry, Biology)
• History & Geography
• Language & Literature
• General Knowledge
• And more!

HOW IT WORKS:
1. Download the app
2. Download the AI model (one-time, ~500MB)
3. Start learning - no internet needed!

TECHNICAL DETAILS:
• Uses Qwen 0.5B language model
• Powered by llama.cpp
• Open-source foundations

Made with ❤️ for students everywhere by Ekatra

No ads. No subscriptions. No data collection. Just learning.
```

---

## Release Tracks

| Track | Purpose | Users |
|-------|---------|-------|
| Internal Testing | Team testing | 100 max |
| Closed Testing | Beta testers | Invite only |
| Open Testing | Public beta | Anyone can join |
| Production | Full release | Everyone |

### Recommended Rollout
1. **Week 1**: Internal testing (team)
2. **Week 2**: Closed testing (50-100 beta testers)
3. **Week 3**: Open testing (public beta)
4. **Week 4+**: Production (staged rollout 10% → 50% → 100%)

---

## Post-Launch

### Monitoring
- [ ] Set up Google Play Console notifications
- [ ] Monitor reviews and ratings
- [ ] Track crash reports (Android Vitals)
- [ ] Respond to user feedback

### Updates
- [ ] Plan update schedule
- [ ] Maintain changelog
- [ ] Increment version code for each update

---

## Important Files

| File | Purpose |
|------|---------|
| `CHANGELOG.md` | Version history |
| `PRIVACY_POLICY.md` | Privacy policy (host online) |
| `LICENSE` | Open source license |
| `app/build.gradle.kts` | Version configuration |
| `maya-ai-release.keystore` | Signing key (KEEP SAFE!) |

---

## Resources

- [Google Play Console](https://play.google.com/console)
- [Developer Policy Center](https://play.google.com/about/developer-content-policy/)
- [Launch Checklist](https://developer.android.com/distribute/best-practices/launch/launch-checklist)
- [App Quality Guidelines](https://developer.android.com/docs/quality-guidelines)
