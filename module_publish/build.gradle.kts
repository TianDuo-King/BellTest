plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.belltest.publish"
    compileSdk { version = release(37) }
    defaultConfig { minSdk = 24 }
    buildFeatures { viewBinding = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":lib_base"))
    implementation(project(":lib_resources"))
}
