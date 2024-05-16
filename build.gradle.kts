buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
        classpath("org.lsposed.lsparanoid:gradle-plugin:0.6.0")
    }
}

tasks.register("Delete", Delete::class) {
    delete(rootProject.buildDir)
}