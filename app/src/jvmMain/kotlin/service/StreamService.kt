package dev.petuska.fake.kamera.service

import dev.petuska.fake.kamera.effect.FrameEffect
import dev.petuska.fake.kamera.util.VideoDeviceInput
import dev.petuska.fake.kamera.util.VideoDeviceOutput
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.opencv.core.Mat
import kotlin.time.Duration.Companion.seconds

class StreamService(
  private val input: VideoDeviceInput,
  private val output: VideoDeviceOutput,
  fps: UInt
) : AutoCloseable {
  private val frameDelay = 1.seconds / fps.toInt()

  suspend fun stream(vararg effects: FrameEffect) = stream(effects.toSet())

  suspend fun stream(effects: Set<FrameEffect>) {
    input.open()
    output.open()
    input.stream()
      .filterNot(Mat::empty)
      .let { effects.fold(it) { flow, effect -> flow.map(effect::invoke) } }
      .collectLatest {
        delay(frameDelay)
        output.write(it)
      }
  }

  override fun close() {
    input.close()
    output.close()
  }
}
