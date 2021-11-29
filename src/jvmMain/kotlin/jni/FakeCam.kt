package dev.petuska.fake.kamera.jni

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.io.IOException
import org.opencv.core.Mat

class FakeCam(device: String, private val width: Int, private val height: Int) : AutoCloseable {
  private val fd: Int
  private val channels: Int = 3
  constructor(
      device: String,
      initialFrame: Mat
  ) : this(device, initialFrame.width(), initialFrame.height()) {
    writeFrame(initialFrame)
  }
  init {
    fd = open(device, width, height)
    println("FD: $fd")
  }

  private external fun open(device: String, width: Int, height: Int): Int
  private external fun writeFrame(fd: Int, frame: ByteArray): Boolean
  private external fun close(fd: Int): Int

  fun writeFrame(frame: ByteArray): Boolean = writeFrame(fd, frame)

  fun writeFrame(mat: Mat) {
    require(mat.width() == width) {
      "FrameWidth[${mat.width()}] does not match CameraWidth[$width]"
    }
    require(mat.height() == height) {
      "FrameHeight[${mat.height()}] does not match CameraHeight[$height]"
    }
    require(mat.channels() == channels) {
      "FrameChannels[${mat.channels()}] does not match CameraChannels[$channels]"
    }
    println("${width}x${height} ${mat.size()}")
    val frame = ByteArray(width * height * 3)
    frame.drawImage(mat.toBufferedImage())
    writeFrame(frame)
  }

  private fun Mat.toBufferedImage(): BufferedImage {
    val image = BufferedImage(width(), height(), BufferedImage.TYPE_3BYTE_BGR)
    val data = (image.raster.dataBuffer as DataBufferByte).data
    get(0, 0, data)
    return image
  }

  // https://github.com/Harium/v4l2fakecam-java/blob/master/src/main/java/examples/CameraExample.java#L66
  private fun ByteArray.drawImage(image: BufferedImage) {
    // Draw at center
    for (y in 0 until image.height) {
      for (x in 0 until image.width) {
        val i: Int = (y * width + x) * 3

        // Flip Horizontally
        val color = Color(image.getRGB(width - 1 - x, y))
        val r: Int = color.red
        val g: Int = color.green
        val b: Int = color.blue
        putPixel(i, r, g, b)
      }
    }
  }

  private fun ByteArray.putPixel(i: Int, r: Int, g: Int, b: Int) {
    this[i] = r.toByte()
    this[i + 1] = g.toByte()
    this[i + 2] = b.toByte()
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
