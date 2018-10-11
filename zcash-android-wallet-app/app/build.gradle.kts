import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    id("kotlin-android-extensions")
    id("kotlin-android")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(28)
        applicationId = "cash.z.wallet.app"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

// Note: we use string primitives for dependencies rather than something fancy (like buildSrc
// variables) so that we can leverage the built-in lint check that alerts us about new versions
dependencies {
    compile(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation(project(":zcash-android-welding"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.0.0")
    implementation("android.arch.navigation:navigation-fragment:1.0.0-alpha06")
    implementation("android.arch.navigation:navigation-ui:1.0.0-alpha06")
    implementation("android.arch.navigation:navigation-fragment-ktx:1.0.0-alpha06")
    implementation("android.arch.navigation:navigation-ui-ktx:1.0.0-alpha06")
    implementation("androidx.core:core-ktx:1.0.0")
    implementation("com.google.android.material:material:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test:runner:1.1.0-beta01")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0-beta01")
}
