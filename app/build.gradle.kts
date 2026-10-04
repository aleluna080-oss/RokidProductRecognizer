plugins {
    id("com.android.application")
}

android {
    namespace = "com.alexluna.rokidproduct"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alexluna.rokidproduct"
        minSdk = 29
        // Conservative sideload target for Rokid/YodaOS compatibility.
        // This app is being sideloaded, not published to Google Play.
        targetSdk = 32
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        // targetSdk 32 is intentional for this Rokid sideload MVP.
        // Suppress only the Google Play target-level lint check.
        disable += "ExpiredTargetSdkVersion"
    }
}

dependencies {
    implementation("androidx.activity:activity:1.10.1")
    implementation("androidx.core:core:1.16.0")

    // Camera frames from the glasses.
    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")

    testImplementation("junit:junit:4.13.2")

    // Bundled/offline ML model. Does NOT require Google Play Services at runtime.
    implementation("com.google.mlkit:image-labeling:17.0.9")
}
