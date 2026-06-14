package com.example.leximaster.data.remote.error

sealed class WordnikError(val message: String) {
    data class NetworkError(val msg: String) : WordnikError(msg)
    data class ApiError(val code: Int, val msg: String) : WordnikError(msg)
    data class SerializationError(val msg: String) : WordnikError(msg)
    data class InvalidApiKey(val msg: String) : WordnikError(msg)
    data class UnknownError(val msg: String) : WordnikError(msg)
}
