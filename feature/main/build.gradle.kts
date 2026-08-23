plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.photo.starsnap.main"
    compileSdk = 36

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.13"
//    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "META-INF/gradle/incremental.annotation.processors"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:di"))
    implementation(project(":core:model"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)
    // Material icons (extended) for Icons.Filled / Icons.Default
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.compose.runtime)


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.lifecycle.livedata.core)

    implementation(libs.androidx.paging.compose)

    implementation(platform(libs.google.firebase.bom))
    implementation(libs.google.firebase.auth)

    // removed google-services plugin from library/feature module — plugin must be applied only on the application module
    implementation(libs.google.android.gms.auth)

    implementation(libs.google.accompanist.animations)

    implementation(libs.google.identity.googleid)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.airbnb.android.lottie.compose)

    implementation(libs.okhttp)
    implementation(platform(libs.okhttp.bom))

    kapt(libs.hilt.compiler)

    implementation(libs.material)

    implementation(libs.landscapist.glide)

    implementation(libs.kizitonwose.calendar.compose)

//    implementation(libs.ucrop)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
