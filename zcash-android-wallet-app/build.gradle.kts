buildscript {
//    ext.kotlin_version = '1.2.71'
    repositories {
        google()
        jcenter()
        
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.3.0-alpha13")
        classpath(kotlin("gradle-plugin", version = "1.2.71"))
    }
}


allprojects {
    repositories {
        google()
        jcenter()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
