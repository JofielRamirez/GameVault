plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

room {
    // Exported schema JSON files are versioned in Git to support future migrations
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.jofielramirez.gamevault"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.jofielramirez.gamevault"
        // API 21 was requested, but current AndroidX/Compose libraries declare minSdk 23
        // and Firebase Crashlytics 21.x declares minSdk 24, so the manifest merger
        // rejects anything lower.
        minSdk = 24
        // Latest stable Android API level installed in the SDK
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            // Robolectric needs the merged Android resources and manifest
            isIncludeAndroidResources = true
            all {
                // Robolectric downloads the Android framework jars into ~/.m2 by default;
                // keep them next to the Gradle caches instead (GRADLE_USER_HOME).
                it.systemProperty(
                    "maven.repo.local",
                    gradle.gradleUserHomeDir.resolve("robolectric-m2").absolutePath
                )
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Error tracking (Firebase Crashlytics)
    implementation(libs.firebase.crashlytics)

    // Local data storage (Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
