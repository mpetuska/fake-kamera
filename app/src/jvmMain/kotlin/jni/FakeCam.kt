package dev.petuska.fake.kamera.jni

import java.io.File

object FakeCam {
  external fun open(device: String, width: Int, height: Int, channels: Int): Int
  external fun writeFrame(fd: Int, frame: ByteArray): Boolean
  external fun close(fd: Int): Int

  init {
    val arch = getArch()
    NativeUtils.loadLibraryFromJar("/META-INF/native/linux-$arch", arrayOf("fakecam"))
  }

  private fun getArch(): String {
    val arch = System.getProperty("os.arch")
    val dataModel =
      System.getProperty("sun.arch.data.model", System.getProperty("com.ibm.vm.bitmode"))
    val bits: String
    return if (arch == "arm") {
      "arm" + if (File("/lib/arm-linux-gnueabihf").isDirectory) "hf" else "el"
    } else {
      bits =
        if ("32" == dataModel) {
          "32"
        } else if ("64" == dataModel) {
          "64"
        } else {
          if (arch.contains("64") || arch.equals("sparcv9", ignoreCase = true)) "64" else "32"
        }
      bits
    }
  }
}
