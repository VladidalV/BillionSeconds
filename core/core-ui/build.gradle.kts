plugins {
    id("kmp-feature-convention")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}

android {
    namespace = "com.example.billionseconds.core.ui"
}
