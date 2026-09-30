import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use(localProperties::load)
}
val youtubeApiKey = localProperties.getProperty("YOUTUBE_API_KEY")
    ?: System.getenv("YOUTUBE_API_KEY")
    ?: ""
val googleOauthClientId = localProperties.getProperty("GOOGLE_OAUTH_CLIENT_ID")
    ?: System.getenv("GOOGLE_OAUTH_CLIENT_ID")
    ?: ""

android {
    namespace = "tv.publivoretube"
    compileSdk = 36

    defaultConfig {
        applicationId = "tv.publivoretube"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField(
            "String",
            "YOUTUBE_API_KEY",
            "\"$youtubeApiKey\"",
        )

        buildConfigField(
            "String",
            "GOOGLE_OAUTH_CLIENT_ID",
            "\"$googleOauthClientId\"",
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.00")
    implementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.tv:tv-material:1.1.0")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("io.coil-kt:coil-compose:2.7.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
