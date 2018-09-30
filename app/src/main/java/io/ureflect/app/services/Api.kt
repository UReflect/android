package io.ureflect.app.services

object Api {
    init {
        println("Api service initialized")
    }

    fun log(message: String = "") : Boolean {
        println("Message from API Log $message")
        return message.isNotEmpty()
    }
}