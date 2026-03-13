# Release Signing Configuration

## ⚠️ IMPORTANT: Keystore Security

**NEVER** commit your keystore file or passwords to version control!

## Generate Release Keystore

```bash
keytool -genkey -v \
  -keystore maya-ai-release.keystore \
  -alias maya-ai \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass YOUR_STORE_PASSWORD \
  -keypass YOUR_KEY_PASSWORD \
  -dname "CN=Ekatra, OU=Mobile, O=Ekatra, L=City, ST=State, C=IN"
```

## Store Keystore Securely

1. **DO NOT** add to git (already in .gitignore)
2. Store in password manager
3. Backup in multiple secure locations
4. Document passwords separately

## Configure Signing in build.gradle.kts

Add to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/maya-ai-release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = "maya-ai"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... existing config
        }
    }
}
```

## Environment Variables (for CI/CD)

```bash
export KEYSTORE_PASSWORD="your_store_password"
export KEY_PASSWORD="your_key_password"
```

## Build Signed Release

```bash
# Build signed APK
./gradlew assembleRelease

# Build signed AAB (for Play Store)
./gradlew bundleRelease
```

## Outputs

- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

## Upload to Play Store

1. Go to Google Play Console
2. Select your app
3. Go to Release > Production
4. Upload the `.aab` file
5. Fill in release notes
6. Submit for review
