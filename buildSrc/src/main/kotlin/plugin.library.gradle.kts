plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("io.kotest.multiplatform")
  id("dev.petuska.klip")
  id("plugin.common")
}

kotlin {
  js {
    useCommonJs()
    browser()
  }
  jvm()
  sourceSets {
    commonMain {
      dependencies { implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_") }
    }
    commonTest {
      dependencies {
        implementation("io.kotest:kotest-framework-engine:_")
        implementation("io.kotest:kotest-assertions-core:_")
      }
    }
    named("jvmTest") { dependencies { implementation("io.kotest:kotest-runner-junit5:_") } }
  }
}
