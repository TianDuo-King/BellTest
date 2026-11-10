plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.belltest.player"
    compileSdk { version = release(37) }

    defaultConfig { minSdk = 24 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":lib_ipc"))
    implementation(libs.androidx.media3.exoplayer)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.media3.common)
}
