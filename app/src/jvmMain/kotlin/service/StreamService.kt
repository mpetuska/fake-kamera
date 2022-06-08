package dev.petuska.fake.kamera.service

import dev.petuska.fake.kamera.effect.FrameEffect
import dev.petuska.fake.kamera.util.VideoDeviceInput
import dev.petuska.fake.kamera.util.VideoDeviceOutput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.opencv.core.Mat
import java.io.Closeable
import kotlin.time.Duration.Companion.seconds

class StreamService(
  private val input: VideoDeviceInput,
  private val output: VideoDeviceOutput,
  fps: UInt
) : Closeable {
  private val frameDelay = 1.seconds / fps.toInt()

  @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
  suspend fun stream(effect: FrameEffect) {
    input.open()
    output.open()
    input.stream()
      .filterNot(Mat::empty)
      .debounce(frameDelay)
      .mapLatest(effect)
      .collect(output::write)
  }

  override fun close() {
    input.close()
    output.close()
  }
}
