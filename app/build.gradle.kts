plugins {
  id("convention.app-compose")
}

configurations {
  register("jni")
}

dependencies {
  "jni"(project(":jni", configuration = "native-so"))
}

mppApp {
  jvmMainClass.set("dev.petuska.fake.kamera.MainKt")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
      }
    }
    jsMain {
      dependencies {
      }
    }
    jvmMain {
      dependencies {
        implementation("org.reduxkotlin:redux-kotlin-threadsafe:_")
        implementation("org.reduxkotlin:redux-kotlin-thunk:_")
        implementation("org.kodein.di:kodein-di-framework-compose:_")
        implementation("org.openpnp:opencv:_")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:_")
      }
    }
  }
}

tasks {
  jvmJar {
    into("META-INF/native/linux-64") {
      from(configurations["jni"]) {
        duplicatesStrategy = DuplicatesStrategy.WARN
      }
    }
  }
}
