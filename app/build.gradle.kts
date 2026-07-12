import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val keystoreProperties = Properties().apply {
    val propsFile = rootProject.file("keystore/keystore.properties")
    if (propsFile.exists()) {
        load(propsFile.inputStream())
    }
}

android {
    namespace = "com.svendsrud.castradio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.svendsrud.castradio"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "2.0"
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.containsKey("storeFile")) {
                val jksFileName = (keystoreProperties["storeFile"] as String).substringAfterLast("/")
                storeFile = rootProject.file("keystore/$jksFileName")
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // MediaRouteButton krever AppCompat-tema og FragmentActivity; Cast-rammeverket er View-basert.
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.mediarouter:mediarouter:1.6.0")
    implementation("com.google.android.gms:play-services-cast-framework:21.4.0")
    testImplementation("junit:junit:4.13.2")
}
