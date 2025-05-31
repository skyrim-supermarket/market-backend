package com.mac350

import io.ktor.server.application.*
import com.mac350.models.*
import com.mac350.plugins.*
import com.mac350.repositories.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val repository = FakeClientRepo()

    configureSerialization()
    configureHTTP()
    configureSecurity()
    configureRouting(repository)
    configureDatabases()
}
