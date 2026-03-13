# Firebase Authentication Setup Required

## Current Issue
The app is showing `CONFIGURATION_NOT_FOUND` error because Firebase Authentication is not enabled in the Firebase Console.

## Steps to Fix

### 1. Enable Firebase Authentication

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **maya-ai-ekatra**
3. Click on **Authentication** in the left sidebar
4. Click **Get Started** if you haven't already

### 2. Enable Email/Password Authentication

1. In Authentication, click on **Sign-in method** tab
2. Find **Email/Password** in the list
3. Click on it to expand
4. Toggle **Enable** to ON
5. Click **Save**

### 3. Download Updated google-services.json

1. Go to **Project Settings** (gear icon) → **General**
2. Scroll down to **Your apps** section
3. Find your Android app (org.ekatra.alfred)
4. Click **google-services.json** download button
5. Replace `ekatra-android/app/google-services.json` with the new file
6. Rebuild the app

### 4. Enable Google Sign-In (Optional)

If you want to re-enable Google Sign-In:

1. In Firebase Console → **Authentication** → **Sign-in method**
2. Enable **Google** provider
3. Download the new `google-services.json` (it will now have OAuth clients)
4. In ProfileActivity.kt, uncomment the Google Sign-In button code
5. Rebuild

## Quick Fix for Testing

For immediate testing without Firebase, skip authentication temporarily by:
- Creating a simplified login flow that doesn't use Firebase Auth
- OR using the anonymous authentication mode

Contact the Firebase project owner to enable these features.
