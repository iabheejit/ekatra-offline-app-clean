plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    // Hilt DI
    id("com.google.dagger.hilt.android")
}

import java.time.Instant
import java.time.format.DateTimeFormatter

// Version configuration - Update these for each release
val versionMajor = 1
val versionMinor = 1
val versionPatch = 2
val versionBuild = 1  // Increment for each build

android {
    namespace = "org.ekatra.alfred"
    compileSdk = 35
    ndkVersion = "26.1.10909125"

    defaultConfig {
        applicationId = "org.ekatra.alfred"
        minSdk = 24
        targetSdk = 35
        
        // versionCode must be incremented for each Play Store upload
        // Formula: major*10000 + minor*100 + patch (allows 99 patches per minor)
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        
        // Build metadata
        val buildTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
        buildConfigField("int", "VERSION_BUILD", "$versionBuild")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DCMAKE_BUILD_TYPE=Release",
                    "-DLLAMA_NATIVE=OFF"
                )
                cppFlags += listOf("-O3", "-fPIC")
            }
        }
    }

    // Release signing configuration (fail only on release tasks)
    signingConfigs {
        create("release") {
            val isReleaseTask = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

            val keystoreFile = file("../../keystore/maya-ai-release.keystore")
            val storePass = System.getenv("KEYSTORE_PASSWORD") ?: ""
            val keyPass = System.getenv("KEY_PASSWORD") ?: ""

            if (isReleaseTask) {
                if (!keystoreFile.exists()) {
                    throw GradleException("Release keystore missing at ../keystore/maya-ai-release.keystore")
                }
                if (storePass.isBlank() || keyPass.isBlank()) {
                    throw GradleException("Release signing passwords not set. Please set KEYSTORE_PASSWORD and KEY_PASSWORD env vars.")
                }
                storeFile = keystoreFile
                storePassword = storePass
                keyAlias = "maya-ai"
                keyPassword = keyPass
            } else {
                // For debug/non-release tasks, don't fail the build if keystore/env vars are absent.
                if (keystoreFile.exists()) {
                    storeFile = keystoreFile
                    storePassword = storePass.ifBlank { "debug-pass" }
                    keyAlias = "maya-ai"
                    keyPassword = keyPass.ifBlank { "debug-pass" }
                }
            }
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Require release signing; fail fast if keystore is missing
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true  // Enable BuildConfig generation
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.webkit:webkit:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Room (SQLite)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // NanoHTTPD for simple HTTP server (multi-device mode)
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    
    // Jetpack Compose for native UI
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // === Hilt DI ===
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-android-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // === Firebase ===
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-perf")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-messaging")
    
    // QR Code generation
    implementation("com.google.zxing:core:3.5.3")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    
    // WorkManager for background sync
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // === Testing ===
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.51")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
