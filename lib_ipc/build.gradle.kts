plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.belltest.ipc"
    compileSdk { version = release(37) }

    defaultConfig { minSdk = 24 }

    buildFeatures { aidl = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
}
