import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.android.library)
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
			implementation(libs.ktor.client.core)
			implementation(libs.ktor.client.content.negotiation)
			implementation(libs.ktor.serialization)
			implementation(libs.charsets)

			implementation(projects.appmodules.library.logger)
			implementation(projects.appmodules.model.common)
			implementation(projects.appmodules.model.chronus)

			implementation(projects.appmodules.datasource.network.chronus.irkutskIrnitu)
			implementation(projects.appmodules.datasource.network.chronus.irkutskIguImit)
			implementation(projects.appmodules.datasource.network.chronus.yourcityYouruniversity)
		}
		androidMain.dependencies {
			implementation(libs.ktor.client.okhttp)
		}
		jvmMain.dependencies {
			implementation(libs.ktor.client.okhttp)
		}
		iosMain.dependencies {
			implementation(libs.ktor.client.darwin)
		}
	}
}

android {
	namespace = "datasource.network.chronusparsers.main"
	compileSdk = libs.versions.androidsdk.target.get().toInt()

	defaultConfig {
		minSdk = libs.versions.androidsdk.min.get().toInt()
	}

	compileOptions {
		sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.get())
		targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.get())
	}
}
