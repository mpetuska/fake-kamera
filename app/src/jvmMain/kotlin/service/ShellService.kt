package dev.petuska.fake.kamera.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.future.await

object ShellService {
  suspend fun executeSudo(cmd: String, password: String) = execute("echo $password | sudo -S $cmd", true)
  suspend fun execute(cmd: String, sensitive: Boolean = false): Flow<Pair<Boolean, String>> = flow {
    val command = arrayOf("/bin/bash", "-c", cmd)
    val pb = Runtime.getRuntime().exec(command)
    pb.onExit().await()
    pb.inputReader().useLines { it.forEach { line -> emit(false to line) } }
    pb.errorReader().useLines {
      it.forEach { line ->
        if (!line.startsWith("[sudo]")) emit(true to line)
      }
    }
    if (pb.exitValue() != 0) {
      val safeCmd = if (sensitive) cmd.replace(".".toRegex(), "*") else cmd
      emit(true to "Command `$safeCmd` exited with non-zero value ${pb.exitValue()}")
    }
  }.flowOn(Dispatchers.IO)
}
