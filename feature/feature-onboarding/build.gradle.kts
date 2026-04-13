plugins { id("kmp-feature-convention") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreUi)
        }
    }
}

android { namespace = "com.example.billionseconds.feature.onboarding" }
