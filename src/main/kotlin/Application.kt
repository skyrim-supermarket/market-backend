package com.mac350

import io.ktor.server.application.*
import com.mac350.models.*
import com.mac350.plugins.*
import com.mac350.repositories.*
import java.io.File

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    Databases.init()
    configureSerialization()
    configureHTTP()
    configureSecurity()
    configureRouting()

    val uploadDir = File("uploads")
    if(!uploadDir.exists()) uploadDir.mkdirs()
}
