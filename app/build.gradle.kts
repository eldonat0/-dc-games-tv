plugins { id("com.android.application") }

android {
    namespace = "com.dcgames.tv"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.dcgames.tv"
        minSdk = 23
        targetSdk = 35
        versionCode = 4
        versionName = "4.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
