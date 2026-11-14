import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
}

room {
    // Exported schema JSON files are versioned in Git to support future migrations
    schemaDirectory("$projectDir/schemas")
}

// RAWG API key for the game search. It is read from local.properties (never committed to Git):
//   RAWG_API_KEY=your_key
// Without a key the app still works; the search box just says it is not configured.
val rawgApiKey: String = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}.getProperty("RAWG_API_KEY")?.trim().orEmpty().ifEmpty { System.getenv("RAWG_API_KEY").orEmpty() }

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

        buildConfigField("String", "RAWG_API_KEY", "\"$rawgApiKey\"")
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
        buildConfig = true
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
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    // Error tracking (Firebase Crashlytics)
    implementation(libs.firebase.crashlytics)

    // Local data storage (Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Game search (RAWG web API) and cover images
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
