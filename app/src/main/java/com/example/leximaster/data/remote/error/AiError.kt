package com.example.leximaster.data.remote.error

sealed class AiError(val message: String) {
    data class NetworkError(val msg: String) : AiError(msg)
    data class ApiError(val code: String, val msg: String) : AiError(msg)
    data class SerializationError(val msg: String) : AiError(msg)
    data class RateLimitError(val msg: String) : AiError(msg)
    data class ParsingError(val msg: String) : AiError(msg)
    data class UnknownError(val msg: String) : AiError(msg)
}