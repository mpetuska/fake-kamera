package dev.petuska.fake.kamera.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.future.await

class ShellService {
  suspend fun executeSudo(cmd: String, password: String) = execute("echo $password | sudo -S $cmd")
  suspend fun execute(cmd: String): Flow<String> =
      flow {
            val command = arrayOf("/bin/bash", "-c", cmd)
            val pb = Runtime.getRuntime().exec(command)
            pb.onExit().await()
            if (pb.exitValue() == 0) {
              pb.inputStream.bufferedReader().useLines { it.forEach { line -> emit(line) } }
            } else {
              throw IllegalStateException(
                  "Command `$cmd` exited with non-zero value ${pb.exitValue()}")
            }
          }
          .flowOn(Dispatchers.IO)
}
