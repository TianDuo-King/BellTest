plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.belltest"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.belltest"
        minSdk = 24
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
        viewBinding = true
    }
}

dependencies {
    implementation(project(":lib_base"))
    implementation(project(":lib_resources"))
    implementation(project(":module_feed"))
    implementation(project(":module_featured"))
    implementation(project(":module_message"))
    implementation(project(":module_user"))
    implementation(project(":service_player"))
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(project(":lib_ipc"))
}
