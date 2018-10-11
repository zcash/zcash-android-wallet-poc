//Explicitly set `rootProject.buildFileName`
// avoids project import issues caused by Android Studio silently adding a `build.gradle` file
rootProject.buildFileName = "build.gradle.kts"

include(":app", ":zcash-android-welding")
