plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.locket"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.locket"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")

    // ViewPager2 for swiping functionality
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Gesture detection
    implementation("androidx.core:core-ktx:1.10.1")

    // CircleImageView for round profile images
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}