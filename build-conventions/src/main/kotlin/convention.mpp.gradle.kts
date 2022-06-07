

plugins {
  id("convention.common")
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

kotlin {
  sourceSets {
    configureEach {
      languageSettings {
        optIn("kotlin.RequiresOptIn")
        optIn("kotlinx.serialization.ExperimentalSerializationApi")
      }
    }
    commonMain {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")
      }
    }
    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }
}
