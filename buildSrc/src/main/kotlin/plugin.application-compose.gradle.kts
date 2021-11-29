
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  id("org.jetbrains.compose")
  id("plugin.library")
}

kotlin {
  js {
    binaries.executable()
    browser {
      commonWebpackConfig {
        cssSupport.enabled = true
        devServer = devServer?.copy(open = false, port = 3000)
      }
    }
  }
  sourceSets {
    //    commonMain { dependencies { implementation(compose.desktop.common) } }
    named("jsMain") {
      dependencies {
        implementation(compose.web.core)
        implementation(compose.web.svg)
      }
    }
    named("jsTest") { dependencies { implementation(compose.web.testUtils) } }
    named("jvmMain") { dependencies { implementation(compose.desktop.currentOs) } }
    all { languageSettings { optIn("androidx.compose.ui.ExperimentalComposeUiApi") } }
  }
}

compose {
  desktop {
    application {
      nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        packageName = project.name
        packageVersion = "${project.version}"
      }
    }
  }
}

// Workaround for https://kotlinlang.slack.com/archives/C0B8L3U69/p1633590092096600
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
  rootProject.the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().apply {
    resolution("@webpack-cli/serve", "1.5.2")
  }
}
