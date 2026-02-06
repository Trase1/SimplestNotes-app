import java.util.Properties
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

plugins {
    alias(libs.plugins.android.application)
}

val appVersionName = providers.gradleProperty("APP_VERSION_NAME").orNull ?: error("Missing APP_VERSION_NAME in gradle.properties")

// ---- keystore.properties loader (root/keystore.properties) ----
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore/keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun ks(key: String): String =
    keystoreProps.getProperty(key) ?: error("Missing $key in keystore.properties")

// ---- Task: copy produced APK to a renamed file (AGP 9+ safe) ----
abstract class CopyRenamedApkTask : DefaultTask() {

    @get:InputDirectory
    abstract val apkDir: DirectoryProperty

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @get:OutputFile
    abstract val outputApk: RegularFileProperty

    @TaskAction
    fun run() {
        val dir = apkDir.get()

        val built = builtArtifactsLoader.get().load(dir)
            ?: error("No built artifacts found in: ${dir.asFile.absolutePath}")

        val element = built.elements.singleOrNull()
            ?: error("Expected exactly 1 APK, found ${built.elements.size}: ${built.elements.map { it.outputFile }}")

        val inFile = dir.asFile.resolve(element.outputFile)
        val outFile = outputApk.get().asFile

        outFile.parentFile.mkdirs()
        inFile.copyTo(outFile, overwrite = true)
    }
}

android {
    namespace = "com.traseapps.simplestNotes"
    compileSdk = 36

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    defaultConfig {
        applicationId = "com.traseapps.simplestNotes"
        minSdk = 24
        targetSdk = 36
        versionCode = 5
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(ks("RELEASE_STORE_FILE"))
            storePassword = ks("RELEASE_STORE_PASSWORD")
            keyAlias = ks("RELEASE_KEY_ALIAS")
            keyPassword = ks("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            proguardFiles("proguard-rules.pro")
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        checkReleaseBuilds = true
        abortOnError = true
    }

    buildFeatures {
        viewBinding = true
    }
}

// ---- Variant API: copy APK after assemble ----
androidComponents {
    onVariants { variant ->
        val appName = "simplestNotes"
        val outName = "${appName}_v${appVersionName}_${variant.name}.apk"

        val cap = variant.name.replaceFirstChar { it.uppercaseChar() }

        val copyTask = tasks.register("copy${cap}Apk", CopyRenamedApkTask::class.java) {
            // APK is a DIRECTORY in AGP 9
            apkDir.set(variant.artifacts.get(SingleArtifact.APK))
            builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())

            outputApk.set(layout.buildDirectory.file("outputs/renamed_apk/${variant.name}/$outName"))
        }

        // DON'T use tasks.named("assemble$cap") — it may not exist yet.
        // matching(...).configureEach is safe even if the task is created later.
        tasks.matching { it.name == "assemble$cap" }.configureEach {
            finalizedBy(copyTask)
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
    testImplementation(libs.room.testing)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
