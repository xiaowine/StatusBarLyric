import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.lsposed.lsparanoid")
}

val localProperties = Properties()
if (rootProject.file("local.properties").canRead()) localProperties.load(rootProject.file("local.properties").inputStream())

android {
    namespace = "statusbar.lyric"
    compileSdk = 34
    val buildTime = System.currentTimeMillis()
    defaultConfig {
        applicationId = "statusbar.lyric.dev"
        minSdk = 26
        targetSdk = 34
        versionCode = 5
        versionName = "1.0.3"
        aaptOptions.cruncherEnabled = false
        aaptOptions.useNewCruncher = false
        buildConfigField("long", "BUILD_TIME", "$buildTime")
        buildConfigField("int", "API_VERSION", "6")
        buildConfigField("int", "CONFIG_VERSION", "1")
    }
    val config = localProperties.getProperty("androidStoreFile")?.let {
        signingConfigs.create("config") {
            storeFile = file(it)
            storePassword = localProperties.getProperty("androidStorePassword")
            keyAlias = localProperties.getProperty("androidKeyAlias")
            keyPassword = localProperties.getProperty("androidKeyPassword")
            enableV3Signing = true
            enableV4Signing = true
        }
    }
    buildTypes {
        all {
            signingConfig = config ?: signingConfigs["debug"]
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
    }
    packaging {
        resources {
            excludes += "**"
        }
    }
    buildFeatures {
        viewBinding = false
    }
    applicationVariants.all {
        outputs.all {
            (this as BaseVariantOutputImpl).outputFileName = "StatusBarLyric-$versionName-$versionCode-$name-$buildTime.apk"
        }
    }
}

kotlin {
    sourceSets.all {
        languageSettings { languageVersion = "2.0" }
    }
}

lsparanoid {
    variantFilter = { variant ->
        if (variant.buildType == "release") {
            seed = System.currentTimeMillis().toInt() + 8848
            classFilter = { true }
            includeDependencies = true
            true
        } else {
            false
        }
    }
}
dependencies {
    compileOnly("de.robv.android.xposed:api:82")
    implementation(project(":blockmiui"))
    implementation(project(":xtoast"))
    implementation("com.github.kyuubiran:EzXHelper:2.2.0")
    implementation("com.github.xiaowine:Lyric-Getter-Api:6.0.0")
    implementation("com.jaredrummler:ktsh:1.0.0")
    implementation("com.github.xiaowine:XKT:1.0.12")
    implementation("com.google.zxing:core:3.5.2")
}