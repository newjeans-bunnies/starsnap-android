import java.net.URI

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("kotlin-kapt")
    id ("com.google.dagger.hilt.android")
}

val configuredStarsnapApiBaseUrl = providers.gradleProperty("STARSNAP_API_BASE_URL")
    .orElse(providers.environmentVariable("STARSNAP_API_BASE_URL"))

val starsnapApiBaseUrl = configuredStarsnapApiBaseUrl.orNull
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: "http://master.hamtory.com:8080/"

val escapedStarsnapApiBaseUrl = starsnapApiBaseUrl
    .let { if (it.endsWith('/')) it else "$it/" }
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

val validateReleaseApiBaseUrl by tasks.registering {
    group = "verification"
    description = "Requires a non-empty HTTPS StarSnap API URL for release builds."
    inputs.property("starsnapApiBaseUrl", configuredStarsnapApiBaseUrl.orElse(""))

    doLast {
        val releaseUrl = configuredStarsnapApiBaseUrl.orNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: throw GradleException("STARSNAP_API_BASE_URL is required for release builds")
        val uri = URI(releaseUrl)
        if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank()) {
            throw GradleException("STARSNAP_API_BASE_URL must be an absolute HTTPS URL for release builds")
        }
    }
}

tasks.configureEach {
    if (name == "preReleaseBuild") {
        dependsOn(validateReleaseApiBaseUrl)
    }
}

android {
    namespace = "com.sns.starsnap.di"
    compileSdk = 36

    defaultConfig {
        minSdk = 28

        buildConfigField("String", "STARSNAP_API_BASE_URL", "\"$escapedStarsnapApiBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            excludes += "META-INF/gradle/incremental.annotation.processors"
        }
    }
}

dependencies {

    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:model"))


    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.retrofit.core)
    implementation(libs.gson)
    implementation(libs.converter.gson)

    implementation(libs.okhttp.logging)
    implementation(libs.okhttp)
    implementation(platform(libs.okhttp.bom))
    // JavaNetCookieJar을 사용하려면 okhttp-urlconnection 모듈이 필요합니다.
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.12.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
