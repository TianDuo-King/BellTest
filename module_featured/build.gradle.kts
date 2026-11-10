plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.belltest.featured"
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
    implementation(project(":lib_model"))
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
}
