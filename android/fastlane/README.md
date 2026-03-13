# Fastlane for Maya AI

Fastlane automates app store deployment. This folder contains metadata for Google Play Store.

## Structure

```
fastlane/
├── metadata/
│   └── android/
│       └── en-US/
│           ├── title.txt           # App name (30 chars max)
│           ├── short_description.txt # 80 chars max
│           ├── full_description.txt  # 4000 chars max
│           └── changelogs/
│               └── 10000.txt       # Changelog for versionCode 10000
└── Fastfile                        # Automation scripts (optional)
```

## Changelog Naming

Changelog files are named after `versionCode`:
- `10000.txt` → version 1.0.0 (versionCode 10000)
- `10100.txt` → version 1.1.0 (versionCode 10100)
- `10101.txt` → version 1.1.1 (versionCode 10101)

## Adding a New Release

1. Update version in `app/build.gradle.kts`
2. Create new changelog: `changelogs/{versionCode}.txt`
3. Build release: `./gradlew bundleRelease`
4. Upload to Play Console or use fastlane

## Optional: Install Fastlane

```bash
# Install fastlane
gem install fastlane

# Initialize in project
cd ekatra-android
fastlane init
```

## Resources

- [Fastlane Documentation](https://docs.fastlane.tools/)
- [Supply (Android)](https://docs.fastlane.tools/actions/supply/)
