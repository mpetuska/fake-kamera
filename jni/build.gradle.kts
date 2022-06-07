import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.*

plugins {
  id("convention.mpp")
}

configurations {
  register("native-so")
}

kotlin {
  targets {
    linuxX64 {
      compilations["main"].apply {
        cinterops {
          create("videodev2")
          create("jni") {
            val javaHome = File(System.getProperty("java.home"))
            includeDirs(
              javaHome.resolve("include"),
              javaHome.resolve("include/linux"),
            )
//            execAndCapture("pkg-config --cflags gtk+-3.0")?.let { args ->
//              compilerOpts(*args)
//              println("Compiler Opts: $compilerOpts")
//            }
          }
        }
      }
      binaries {
        sharedLib(listOf(NativeBuildType.RELEASE)) {
          baseName = "fakecam"
          artifacts {
            add("native-so", linkTaskProvider) {
              builtBy(linkTaskProvider)
            }
          }
        }
      }
    }
  }
  sourceSets {
    commonMain {
      dependencies {
      }
    }
    named("linuxX64Main") {
      dependencies {
      }
    }
  }
}
