package com.example.leximaster.domain

sealed interface Result<out T, out E> {
    data class Success<T>(val data: T) : Result<T, Nothing>
    data class Failure<E>(val error: E) : Result<Nothing, E>
}

fun <T> Result<T, *>.successOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Failure -> throw IllegalStateException("Cannot get success from failure")
}
