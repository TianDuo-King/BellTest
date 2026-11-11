plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.belltest.base"
    compileSdk { version = release(37) }

    defaultConfig { minSdk = 24 }

    buildFeatures { viewBinding = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.androidx.fragment.ktx)
}
