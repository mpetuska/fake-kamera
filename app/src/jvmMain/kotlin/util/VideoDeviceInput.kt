package dev.petuska.fake.kamera.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio

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
    *options
  )

  private val vc = VideoCapture().apply {
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

  override suspend fun open(fps: UInt?): Boolean {
    return if (!opened) {
      println("Opening VideoDeviceInput[$path]")
      fps?.let { vc.set(Videoio.CAP_PROP_FPS, fps.toDouble()) }
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
//    Mat().also(vc::read)
    val mat = Mat()
    if (!vc.read(mat)) {
      println("VideoDeviceInput[$path] produced no frame")
    }
    return mat
  }

  override fun toOutput() = VideoDeviceOutput(path, width, height)
  override fun toInput(): VideoDeviceInput = this
}
