package dev.petuska.fake.kamera.util

import dev.petuska.fake.kamera.jni.FakeCam
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio

sealed interface VideoDevice : AutoCloseable {
  val path: String
  val opened: Boolean
  val width: Int
  val height: Int
  val channels: Int
  suspend fun open(): Boolean
  fun toInput(): VideoDeviceInput
  fun toOutput(): VideoDeviceOutput
}

class VideoDeviceInput(override val path: String, vararg options: Pair<Int, Double>) : VideoDevice {
  constructor(
      path: String,
      width: Int,
      height: Int,
      channels: Int,
      fps: Int = 60,
      vararg options: Pair<Int, Double>
  ) : this(
      path,
      Videoio.CAP_PROP_FRAME_WIDTH to width.toDouble(),
      Videoio.CAP_PROP_FRAME_HEIGHT to height.toDouble(),
      Videoio.CAP_PROP_CHANNEL to channels.toDouble(),
      Videoio.CAP_PROP_FPS to fps.toDouble(),
      *options)
  private val vc =
      VideoCapture().apply {
        options.forEach { (k, v) -> set(k, v) }
        release()
      }
  override var width: Int = 0
    private set
  override var height: Int = 0
    private set
  override var channels: Int = 0
    private set
  override val opened: Boolean
    get() = vc.isOpened
  override suspend fun open(): Boolean {
    return if (!opened) {
      println("Opening VideoDeviceInput[$path]")
      vc.open(path)
    } else opened
  }

  fun stream(): Flow<Mat> = flow {
    open()
    while (opened) {
      emit(read())
    }
  }

  override fun close() {
    println("Closing VideoDeviceInput[$path]")
    vc.release()
  }

  suspend fun read(): Mat {
    return if (opened) {
      Mat().also(vc::read)
      val mat = Mat()
      if (!vc.read(mat)) {
        println("VideoDeviceInput[$path] produced no frame")
      }
      mat
    } else error("VideoDeviceInput[$path] is not open!")
  }

  override fun toOutput() = VideoDeviceOutput(path, width, height)
  override fun toInput(): VideoDeviceInput = this
}

class VideoDeviceOutput(override val path: String, width: Int = 0, height: Int = 0) : VideoDevice {
  private val fc = FakeCam()
  private var fd: Int = -69
  override val opened: Boolean
    get() = fd >= 0
  override var width: Int = width
    private set
  override var height: Int = height
    private set
  override val channels: Int = 2
  private val dimensions: Triple<Int, Int, Int>
    get() = Triple(width, height, channels)

  suspend fun write(frame: Mat) {
    require(frame.channels() == 3) { "Only 3 channel frames supported" }
    if (frame.width() != width || frame.height() != height) {
      width = frame.width()
      height = frame.height()
      close()
    }
    if (open()) {
      val yuv = MatOfByte()
      Imgproc.cvtColor(frame, yuv, Imgproc.COLOR_BGR2YUV)
      fc.writeFrame(fd, yuv.to2dBytes())
    } else error("VideoDeviceOutput[$path#$fd] is not open! dimensions=$dimensions")
  }

  override suspend fun open(): Boolean {
    if (!opened) {
      println("Opening VideoDeviceOutput[$path#$fd]")
      val exit = fc.open(path, width, height, channels)
      if (exit >= 0) {
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
