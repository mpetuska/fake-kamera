package dev.petuska.fake.kamera.jni

import dev.petuska.fake.kamera.util.to2dBytes
import java.io.File
import java.io.IOException
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgproc.Imgproc

class FakeCam(
    device: String,
    private val width: Int,
    private val height: Int,
    private val channels: Int
) : AutoCloseable {
  private val fd: Int
  constructor(
      device: String,
      initialFrame: Mat
  ) : this(device, initialFrame.width(), initialFrame.height(), initialFrame.channels()) {
    writeFrame(initialFrame)
  }
  init {
    fd = open(device, width, height, 2)
    require(fd >= 0) { "Unable to open $device [$fd]" }
    require(channels == 3) { "Only channels=3 supported" }
  }

  private external fun open(device: String, width: Int, height: Int, channels: Int): Int
  private external fun writeFrame(fd: Int, frame: ByteArray): Boolean
  private external fun close(fd: Int): Int

  fun writeFrame(frame: ByteArray): Boolean = writeFrame(fd, frame)

  fun writeFrame(frame: Mat) {
    require(frame.width() == width) {
      "FrameWidth[${frame.width()}] does not match CameraWidth[$width]"
    }
    require(frame.height() == height) {
      "FrameHeight[${frame.height()}] does not match CameraHeight[$height]"
    }
    require(frame.channels() == channels) {
      "FrameChannels[${frame.channels()}] does not match CameraChannels[$channels]"
    }
    println("${width}x${height}:${channels} ${frame.size()}:${frame.channels()}")

    val yuv = MatOfByte()
    Imgproc.cvtColor(frame, yuv, Imgproc.COLOR_BGR2YUV)
    writeFrame(yuv.to2dBytes())
  }

  override fun close() {
    close(fd)
  }

  companion object {
    private fun isArm(): Boolean {
      return System.getProperty("os.arch") == "arm"
    }
    private fun getFloat(): String {
      return if (isArm()) {
        if (File("/lib/arm-linux-gnueabihf").isDirectory) "hf" else "el"
      } else ""
    }
    private fun getArch(): String {
      val arch = System.getProperty("os.arch")
      val dataModel =
          System.getProperty("sun.arch.data.model", System.getProperty("com.ibm.vm.bitmode"))
      val bits: String
      return if (isArm()) {
        "arm" + getFloat()
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
    init {
      val arch = getArch()
      try {
        NativeUtils.loadLibraryFromJar("/META-INF/native/linux-$arch", arrayOf("fakecam"))
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
  }
}
