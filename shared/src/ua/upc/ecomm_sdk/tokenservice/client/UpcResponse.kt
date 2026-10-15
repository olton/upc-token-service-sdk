package ua.upc.ecomm_sdk.tokenservice.client

import ua.upc.ecomm_sdk.tokenservice.error.UpcTokenServiceException
import ua.upc.ecomm_sdk.tokenservice.model.JwsMessage

/**
 * Result wrapper for UPC Token Service operations.
 */
sealed class UpcResponse<out T> {
    data class Success<T>(
        val data: T,
        val rawJson: String,
        val jwsMessage: JwsMessage? = null
    ) : UpcResponse<T>()

    data class Error(
        val exception: UpcTokenServiceException,
        val tranCode: String? = null,
        val comment: String? = null,
        val rawResponse: String? = null
    ) : UpcResponse<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }

    inline fun <R> fold(
        onSuccess: (data: T) -> R,
        onError: (error: Error) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(this)
    }
}
