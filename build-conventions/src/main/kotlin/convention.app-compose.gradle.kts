import ext.MppAppExtension

plugins {
  id("convention.app-mpp")
  id("convention.compose")
}

extensions.configure<MppAppExtension> {
  jvmMainClass.convention(provider { compose.desktop.application.mainClass })
  compose {
    desktop {
      application {
        afterEvaluate {
          mainClass = jvmMainClass.get()
        }
      }
    }
  }
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(compose.runtime)
      }
    }
    jvmMain {
      dependencies {
        implementation(compose.desktop.currentOs)
      }
    }
    jsMain {
      dependencies {
        implementation(compose.web.core)
        implementation(compose.web.svg)
      }
    }
    jsTest {
      dependencies {
        implementation(compose.web.testUtils)
      }
      languageSettings {
        optIn("org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi")
      }
    }
  }
}
