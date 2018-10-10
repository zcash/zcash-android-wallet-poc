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

dependencies {
    implementation(project(":sdk"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.2.71")
    implementation("androidx.appcompat:appcompat:1.0.0-beta01")
    implementation("android.arch.navigation:navigation-fragment:1.0.0-alpha06")
    implementation("android.arch.navigation:navigation-ui:1.0.0-alpha06")
    implementation("android.arch.navigation:navigation-fragment-ktx:1.0.0-alpha06")
    implementation("android.arch.navigation:navigation-ui-ktx:1.0.0-alpha06")
    implementation("androidx.core:core-ktx:1.0.0")
    implementation("com.google.android.material:material:1.0.0-beta01")
    implementation("androidx.constraintlayout:constraintlayout:1.1.2")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test:runner:1.1.0-alpha4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0-alpha4")
}
