package com.example.leximaster.presentation.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResource(
        @param:StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText {
        constructor(@StringRes resId: Int, vararg args: Any) : this(resId, args.toList())

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as StringResource
            return resId == other.resId && args == other.args
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + args.hashCode()
            return result
        }
    }
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringResource -> stringResource(resId, *args.toTypedArray())
}
