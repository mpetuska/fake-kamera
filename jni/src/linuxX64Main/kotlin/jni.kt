@file:Suppress(
  "Unused",
  "LongParameterList",
  "UNUSED_PARAMETER",
  "FunctionParameterNaming",
  "FunctionOnlyReturningConstant",
)

import jni.*
import kotlinx.cinterop.*
import platform.posix.*
import videodev2.*

private fun printError(error: String) {
  fprintf(stderr, "%s\n", error)
}

@CName("Java_dev_petuska_fake_kamera_jni_FakeCam_open")
fun fakeCamOpen(
  env: JNIEnvVar,
  obj: jobject,
  device: jstring,
  width: jint,
  height: jint,
  channels: jint,
): jint {
  val out = memScoped {
    val zero: UByteVar = alloc {
      value = 0.convert()
    }
    env.GetStringUTFChars(device, zero.ptr)
  }
  val output = out?.toKStringFromUtf8()
  val dev_fd = open(output, O_RDWR)
  env.ReleaseStringUTFChars(device, out)
  if (dev_fd < 0) {
    printError("ERROR: could not open output device! $output")
    return -2
  }

  memScoped {
    val format = alloc<v4l2_format>()
    memset(format.ptr, 0, sizeOf<v4l2_format>().convert())
    format.type = V4L2_BUF_TYPE_VIDEO_OUTPUT
    if (ioctl(dev_fd, VIDIOC_G_FMT, format.ptr) < 0) {
      printError("ERROR: unable to get video format!")
      return -1
    }

    val frameSize = width * height * channels
    format.fmt.pix.width = width.convert()
    format.fmt.pix.height = height.convert()
    format.fmt.pix.pixelformat = V4L2_PIX_FMT_YUYV
    format.fmt.pix.sizeimage = frameSize.convert()
    format.fmt.pix.field = V4L2_FIELD_NONE.convert()

    if (ioctl(dev_fd, VIDIOC_S_FMT, format.ptr) < 0) {
      printError("ERROR: unable to set video format!")
      return -1
    }
  }
  return dev_fd
}

@CName("Java_dev_petuska_fake_kamera_jni_FakeCam_writeFrame")
fun fakeCamWriteFrame(
  env: JNIEnvVar,
  obj: jobject,
  dev_fd: jint,
  frame: jbyteArray,
  height: jint,
  channels: jint,
): jboolean {
  val frameSize = env.GetArrayLength(frame)!!
  val elements = env.GetByteArrayElements(frame)

  val written = write(dev_fd, elements, frameSize.convert())
  env.ReleaseByteArrayElements(frame, elements, 0)
  return if (written < 0) {
    printError("ERROR: could not write to output device!")
    close(dev_fd)
    false
  } else {
    written == frameSize.toLong()
  }.toByte().toUByte()
}

@CName("Java_dev_petuska_fake_kamera_jni_FakeCam_close")
fun fakeCamClose(
  env: JNIEnvVar,
  obj: jobject,
  dev_fd: jint,
): jint {
  return close(dev_fd)
}
