plugins {
    `kotlin-dsl`
}

// Make the version catalog accessible to convention plugins via alias(libs.plugins.X)
dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    // Plugin classpaths for convention scripts
    // Keep in sync with gradle/libs.versions.toml
    implementation(libs.plugins.kotlinMultiplatform.map {
        "org.jetbrains.kotlin:kotlin-gradle-plugin:${it.version}"
    })
    implementation(libs.plugins.androidLibrary.map {
        "com.android.tools.build:gradle:${it.version}"
    })
    implementation(libs.plugins.composeMultiplatform.map {
        "org.jetbrains.compose:compose-gradle-plugin:${it.version}"
    })
    // composeCompiler and kotlinSerialization are bundled inside kotlin-gradle-plugin
}
