@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.android.library)
	alias(libs.plugins.jetbrains.compose)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	applyDefaultHierarchyTemplate()

	jvm()

	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.target.get()))
		}
	}

	listOf(
		iosArm64(), // iOS
		iosSimulatorArm64(), // macOS Apple Silicon
		iosX64(), // macOS Intel
	).takeIf { "XCODE_VERSION_MAJOR" in System.getenv().keys } // Export the framework only for Xcode builds
		?.forEach {
			it.binaries.framework {
				// framework is exported for ios

				baseName = "shared" // Used in Swift export

				export(libs.decompose.core)
				export(libs.essenty.lifecycle)
				export(projects.appmodules.library.logger)
			}
		}

	sourceSets {
		commonMain.dependencies {
			implementation(projects.appmodules.library.logger)
			implementation(projects.appmodules.library.navigation)
			implementation(projects.appmodules.library.ui)

			implementation(projects.appmodules.model.common)
			implementation(projects.appmodules.model.chronus)

			implementation(projects.appmodules.feature.chronus.contributor)
			implementation(projects.appmodules.feature.chronus.places)
			implementation(projects.appmodules.feature.chronus.schedule)
			implementation(projects.appmodules.feature.chronus.search)
		}
		iosMain.dependencies {
			api(libs.decompose.core)
			api(libs.essenty.lifecycle)

			api(projects.appmodules.library.logger)
		}
	}
}

android {
	namespace = "com.example.myapplication.compose"
	compileSdk = libs.versions.androidsdk.target.get().toInt()

	defaultConfig {
		minSdk = libs.versions.androidsdk.min.get().toInt()
	}

	compileOptions {
		sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.get())
		targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.get())
	}
}
