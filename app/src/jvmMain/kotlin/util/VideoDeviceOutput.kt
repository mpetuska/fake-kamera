package dev.petuska.fake.kamera.util

import dev.petuska.fake.kamera.jni.FakeCam
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class VideoDeviceOutput(override val path: String, width: Int = 0, height: Int = 0) : VideoDevice {
  private val fc = FakeCam
  private var fd: Int = -69
  override val opened: Boolean
    get() = fd >= 0
  override var width: Int = width
    private set
  override var height: Int = height
    private set
  override var channels: Int = 2
    private set
  private val dimensions: Triple<Int, Int, Int>
    get() = Triple(width, height, channels)

  suspend fun write(frame: Mat) {
    require(frame.channels() == 3) { "Only 3 channel frames supported" }
    if (frame.width() != width || frame.height() != height) {
      width = frame.width()
      height = frame.height()
      close()
      open()
    }
    val yuv = Mat()
    Imgproc.cvtColor(frame, yuv, Imgproc.COLOR_BGR2YUV)
    if (!fc.writeFrame(fd, yuv.to2dBytes())) {
      error("VideoDeviceOutput[$path#$fd] failed to write frame! dimensions=$dimensions")
    }
  }

  override suspend fun open(fps: UInt?): Boolean {
    if (!opened) {
      println("Opening VideoDeviceOutput[$path#$fd]")
      val exit = fc.open(path, width, height, channels)
      if (exit > 0) {
        fd = exit
        println("Opened VideoDeviceOutput[$path#$fd]; dimensions=$dimensions; exit=$exit")
      } else {
        error("Unable to open VideoDeviceOutput[$path#$fd]; dimensions=$dimensions; exit=$exit")
      }
    }
    return opened
  }

  override fun close() {
    if (opened) {
      println("Closing VideoDeviceOutput[$path#$fd]; dimensions=$dimensions")
      val exit = fc.close(fd)
      if (exit == 0) {
        fd = -69
        println("Closed VideoDeviceOutput[$path#$fd]; dimensions=$dimensions; exit=$exit")
      } else {
        error("Unable to close VideoDeviceOutput[$path#$fd]; dimensions=$dimensions; exit=$exit")
      }
    }
  }

  override fun toInput(): VideoDeviceInput {
    return if (width + height != 0) {
      VideoDeviceInput(path, width, height, 3)
    } else {
      VideoDeviceInput(path)
    }
  }

  override fun toOutput(): VideoDeviceOutput = this
}
