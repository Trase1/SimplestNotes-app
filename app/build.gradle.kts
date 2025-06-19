plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.SimplestNotes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.SimplestNotes"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            proguardFiles("proguard-rules.pro")
            versionNameSuffix = "-debug"
        }
        getByName("debug")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildToolsVersion = "35.0.1"

    lint {
        checkReleaseBuilds = true
        abortOnError = true
    }

    buildFeatures {
        viewBinding = true
    }

    signingConfigs {
        create("release") {
            storeFile = file(project.property("RELEASE_STORE_FILE") as String)
            storePassword = project.property("RELEASE_STORE_PASSWORD") as String
            keyAlias = project.property("RELEASE_KEY_ALIAS") as String
            keyPassword = project.property("RELEASE_KEY_PASSWORD") as String
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.room)
    implementation(libs.core)
    implementation(libs.test.core)
    annotationProcessor(libs.room.annotation.processor)
    implementation(libs.room.rxjava3)
    implementation(libs.rxandroid)
    implementation(libs.rxjava)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation(libs.room.testing)

}