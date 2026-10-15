package ua.upc.ecomm_sdk.tokenservice.error

/**
 * Base exception for UPC Token Service SDK.
 */
open class UpcTokenServiceException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when the API returns an error response or non-successful transaction code.
 */
class UpcApiException(
    message: String,
    val tranCode: String? = null,
    val comment: String? = null,
    val httpStatusCode: Int? = null,
    val rawResponse: String? = null,
    cause: Throwable? = null
) : UpcTokenServiceException(
    message = "UPC API Error [TranCode: $tranCode, Status: $httpStatusCode]: $message (Comment: $comment)",
    cause = cause
)

/**
 * Exception thrown when a network communication error occurs.
 */
class UpcNetworkException(
    message: String,
    cause: Throwable? = null
) : UpcTokenServiceException(message, cause)

/**
 * Exception thrown when serialization or JWS parsing fails.
 */
class UpcSerializationException(
    message: String,
    cause: Throwable? = null
) : UpcTokenServiceException(message, cause)

/**
 * Exception thrown when JWS signature verification fails.
 */
class UpcSecurityException(
    message: String,
    cause: Throwable? = null
) : UpcTokenServiceException(message, cause)
