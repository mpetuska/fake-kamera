package dev.petuska.fake.kamera.util

import dev.petuska.fake.kamera.jni.FakeCam
import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class VideoDeviceOutput(
  private val factory: LoggerFactory,
  override val path: String,
  override val fps: Int,
  width: Int,
  height: Int,
  channels: Int = 2,
) : VideoDevice {
  override fun toString(): String = "VideoDeviceOutput[$path#$fd]"
  private val logger = factory.newLogger(this::class)
  private val fc = FakeCam
  private var fd: Int = -69
  override val opened: Boolean
    get() = fd >= 0
  private var closing = false
  override var width: Int = width
    private set
  override var height: Int = height
    private set
  override var channels: Int = channels
    private set
  private val dimensions: Triple<Int, Int, Int>
    get() = Triple(width, height, channels)

  suspend fun write(frame: Mat) {
    if (closing) {
      logger.warning { "$this is closing and is unable to write frames" }
    } else if (!opened) {
      logger.warning { "$this is not opened and is unable to write frames" }
    } else {

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
        logger.error { "$this failed to write frame! dimensions=$dimensions" }
      }
    }
  }

  override suspend fun open(fps: UInt?): Boolean {
    if (!opened && !closing) {
      logger.info { "Opening $this" }
      val exit = fc.open(path, width, height, channels)
      if (exit > 0) {
        fd = exit
        logger.info { "Opened $this; dimensions=$dimensions; exit=$exit" }
      } else {
        logger.error { "Unable to open $this; dimensions=$dimensions; exit=$exit" }
      }
    }
    return opened
  }

  override fun close() {
    if (opened && !closing) {
      closing = true
      logger.info { "Closing $this; dimensions=$dimensions" }
      val exit = fc.close(fd)
      if (exit == 0) {
        logger.info { "Closed $this; dimensions=$dimensions; exit=$exit" }
        fd = -69
      } else {
        logger.error { "Unable to close $this; dimensions=$dimensions; exit=$exit" }
      }
      closing = false
    }
  }

  override fun toInput(channels: Int): VideoDeviceInput = VideoDeviceInput(
    factory = factory,
    path = path,
    width = width,
    height = height,
    channels = channels,
    fps = fps
  )

  override fun toOutput(channels: Int): VideoDeviceOutput = VideoDeviceOutput(
    factory = factory,
    path = path,
    width = width,
    height = height,
    channels = channels,
    fps = fps
  )
}
