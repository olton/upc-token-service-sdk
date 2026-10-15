package ua.upc.ecomm_sdk.tokenservice.security

/**
 * Interface for signing JWS payload in UPC Token Service requests.
 */
interface JwsSigner {
    /**
     * Algorithm name for JWS header (e.g., "RS256").
     */
    val algorithm: String get() = "RS256"

    /**
     * Signs the input data (header.payload) and returns base64url encoded signature or raw signature.
     */
    fun sign(headerBase64Url: String, payloadBase64Url: String): String
}

/**
 * Default pass-through or placeholder JWS signer.
 */
class DefaultJwsSigner(
    override val algorithm: String = "RS256",
    private val staticSignature: String = ""
) : JwsSigner {
    override fun sign(headerBase64Url: String, payloadBase64Url: String): String {
        return staticSignature
    }
}
