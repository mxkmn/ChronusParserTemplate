@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.android.application)
	alias(libs.plugins.jetbrains.compose)
}

android {
	namespace = "app.chronusparsers.android"
	compileSdk = libs.versions.androidsdk.target.get().toInt()

	defaultConfig {
		applicationId = "mxkmn.chronus.parsers"
		minSdk = libs.versions.androidsdk.min.get().toInt()
		targetSdk = libs.versions.androidsdk.target.get().toInt()
		versionCode = 1
		versionName = "1.0"
	}

	packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"

	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.get())
		targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.get())
	}

	kotlinOptions {
		jvmTarget = libs.versions.jvm.target.get()
	}
}

dependencies {
	implementation(libs.androidx.activity)
	implementation(compose.foundation)
	implementation(libs.decompose.core)

	implementation(projects.appmodules.library.logger)
	implementation(projects.appmodules.model.chronus)
	implementation(projects.appmodules.model.common)
	implementation(projects.appmodules.shared.chronusparsers)
}
