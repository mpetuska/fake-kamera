package dev.petuska.fake.kamera.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.kodein.log.Logger
import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio

@Suppress("LongParameterList")
class VideoDeviceInput(
  private val factory: LoggerFactory,
  override val path: String,
  override val fps: Int,
  override val width: Int,
  override val height: Int,
  override val channels: Int = 3,
  vararg val options: Pair<Int, Double>
) : VideoDevice {
  override fun toString(): String = "VideoDeviceInput[$path]"
  private val logger: Logger = factory.newLogger(this::class)

  private val vc = VideoCapture().apply {
    val options = options.toList() + listOf(
      Videoio.CAP_PROP_FRAME_WIDTH to width.toDouble(),
      Videoio.CAP_PROP_FRAME_HEIGHT to height.toDouble(),
      Videoio.CAP_PROP_CHANNEL to channels.toDouble(),
      Videoio.CAP_PROP_FPS to fps.toDouble()
    )
    options.forEach { (k, v) -> set(k, v) }
    release()
  }

  override val opened: Boolean
    get() = vc.isOpened

  override suspend fun open(fps: UInt?): Boolean {
    return if (!opened) {
      logger.info { "Opening $this" }
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
    if (opened) {
      logger.info { "Closing $this" }
      vc.release()
    }
  }

  fun read(): Mat {
    val mat = Mat()
    if (!vc.read(mat)) {
      logger.warning { "$this produced no frame" }
    }
    return mat
  }

  override fun toOutput(channels: Int) = VideoDeviceOutput(
    factory = factory,
    path = path,
    width = width,
    height = height,
    channels = channels,
    fps = fps,
  )

  override fun toInput(channels: Int): VideoDeviceInput =
    VideoDeviceInput(
      factory = factory,
      path = path,
      width = width,
      height = height,
      channels = channels,
      fps = fps,
      options = options
    )
}
