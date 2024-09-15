buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.6.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
        classpath("org.lsposed.lsparanoid:gradle-plugin:0.6.0")
    }
}

tasks.register("Delete", Delete::class) {
    delete(rootProject.buildDir)
}