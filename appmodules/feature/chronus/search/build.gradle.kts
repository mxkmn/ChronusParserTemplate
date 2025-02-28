import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.android.library)
	alias(libs.plugins.jetbrains.compose)
	alias(libs.plugins.kotlin.compose)
}

kotlin {
	applyDefaultHierarchyTemplate()
	jvm()
	iosArm64() // iOS
	iosSimulatorArm64() // macOS Apple Silicon
	iosX64() // macOS Intel

	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.target.get()))
		}
	}

	sourceSets {
		commonMain.dependencies {
			implementation(projects.appmodules.library.logger)
			implementation(projects.appmodules.library.navigation)
			implementation(projects.appmodules.library.ui)

			implementation(projects.appmodules.model.common)
			implementation(projects.appmodules.model.chronus)

			implementation(projects.appmodules.datasource.network.chronus.main)
		}
	}
}

android {
	namespace = "feature.chronus.search"
	compileSdk = libs.versions.androidsdk.target.get().toInt()

	defaultConfig {
		minSdk = libs.versions.androidsdk.min.get().toInt()
	}

	compileOptions {
		sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.get())
		targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.get())
	}
}
