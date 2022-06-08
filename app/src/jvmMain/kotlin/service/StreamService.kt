package dev.petuska.fake.kamera.service

import dev.petuska.fake.kamera.effect.FrameEffect
import dev.petuska.fake.kamera.util.VideoDeviceInput
import dev.petuska.fake.kamera.util.VideoDeviceOutput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger
import org.opencv.core.Mat
import java.io.Closeable
import kotlin.time.Duration.Companion.seconds

class StreamService(
  factory: LoggerFactory,
  private val input: VideoDeviceInput,
  private val output: VideoDeviceOutput,
  fps: UInt
) : Closeable {
  private val logger = factory.newLogger(this::class)
  private val frameDelay = 1.seconds / fps.toInt()

  @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
  suspend fun stream(effect: FrameEffect) {
    input.open()
    output.open()
    input.stream()
      .filterNot(Mat::empty)
      .mapLatest(effect)
      .debounce(frameDelay)
      .onEach { logger.debug { "Writing frame" } }
      .collect(output::write)
  }

  override fun close() {
    input.close()
    output.close()
  }
}
