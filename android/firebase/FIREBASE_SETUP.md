# Firebase Console Setup Guide — Maya AI

**Project:** maya-ai-ekatra  
**Project Number:** 727316288933  
**App ID:** 1:727316288933:android:ca9fc4215db05ba1e3844c  
**Package:** org.ekatra.alfred

---

## 1. Client JSON (already done ✅)

The `google-services.json` is already in `app/google-services.json`.  
If you need to re-download: Firebase Console → Project Settings → Your Apps → Download.

---

## 2. Remote Config (Server-side)

### Import defaults:
1. Go to **Firebase Console → Remote Config**
2. Click **⋮ menu → Import from file**
3. Upload: `firebase/remote_config_defaults.json`
4. Click **Publish Changes**

### Key parameters you can change anytime:

| Key | Type | What it does |
|-----|------|-------------|
| `min_app_version` | String | Set to `"1.2.0"` to trigger update banners for users on 1.1.0 |
| `system_prompt` | String | Change Maya's personality/instructions without an app update |
| `model_download_url` | String | Point to a different GGUF model URL |
| `max_context_messages` | Number | Increase/decrease conversation memory (default: 10) |
| `hotspot_feature_enabled` | Boolean | Enable/disable the multi-device server feature |
| `analytics_sample_rate` | Number | 0-100, percentage of users sending analytics |
| `maintenance_message` | String | Non-empty = shows a banner in the app |

---

## 3. Firestore Rules

1. Go to **Firebase Console → Firestore Database → Rules**
2. Replace the existing rules with the contents of `firebase/firestore.rules`
3. Click **Publish**

### Collections structure:
```
/users/{userId}
  ├── name: string
  ├── country: string
  ├── grade: string
  ├── preferredLanguage: string
  ├── phoneNumber: string
  ├── createdAt: number
  └── lastActiveAt: number
  └── /analytics/{sessionId}
        ├── sessionId: string
        ├── subject: string
        ├── messageCount: number
        ├── duration: number
        └── timestamp: number

/fcm_tokens/{tokenId}  (optional, for targeted notifications)
  ├── uid: string
  ├── token: string
  └── updatedAt: number
```

---

## 4. Cloud Messaging (Push Notifications)

### Send a notification to ALL users:
1. Go to **Firebase Console → Cloud Messaging**
2. Click **Create your first campaign** → **Firebase Notification messages**
3. Fill in:
   - **Title:** `🆕 Maya AI Update Available!`
   - **Body:** `Version 1.2.0 brings Hindi support and better math explanations.`
4. **Target:** App = `org.ekatra.alfred`
5. **Additional options → Custom data** (optional):
   - Key: `type`, Value: `update` (or `announcement` or `tip`)
6. Click **Review → Publish**

### Notification types (custom data `type` key):

| type | Channel | Priority | Icon |
|------|---------|----------|------|
| `announcement` | Announcements | Default | 📢 |
| `update` | App Updates | High | 🆕 |
| `tip` | Learning Tips | Low | 💡 |

### Send via API (for automation/backend):
```bash
# Get access token
ACCESS_TOKEN=$(gcloud auth print-access-token)

# Send to all users via topic
curl -X POST \
  "https://fcm.googleapis.com/v1/projects/maya-ai-ekatra/messages:send" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": {
      "topic": "all",
      "data": {
        "type": "announcement",
        "title": "New Feature!",
        "body": "Maya can now help with Hindi language learning."
      }
    }
  }'
```

> **Note:** To use topics, subscribe users in the app by adding:
> `FirebaseMessaging.getInstance().subscribeToTopic("all")`
> (already planned for a future update)

---

## 5. Authentication (already enabled ✅)

Enabled providers:
- ✅ Email/Password
- ✅ Google Sign-In (OAuth client configured)

---

## 6. Crashlytics (already enabled ✅)

Crashes are automatically reported. View at:  
**Firebase Console → Crashlytics**

---

## 7. Analytics (already enabled ✅)

Custom events logged:
- `profile_completed` (country, grade)
- `model_loaded` (model_id)
- `message_sent` (subject)
- `answer_saved`
- `answer_shared`
- `session_started`

View at: **Firebase Console → Analytics → Events**
