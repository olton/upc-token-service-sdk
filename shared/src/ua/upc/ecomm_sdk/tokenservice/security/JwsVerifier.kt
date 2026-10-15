package ua.upc.ecomm_sdk.tokenservice.security

/**
 * Interface for verifying JWS signatures from UPC Token Service responses.
 */
interface JwsVerifier {
    /**
     * Verifies the signature of JWS message.
     * Returns true if signature is valid or if verification is bypassed.
     */
    fun verify(headerBase64Url: String, payloadBase64Url: String, signatureBase64Url: String): Boolean
}

/**
 * Permissive verifier that accepts all signatures (for test or when signature verification is handled elsewhere).
 */
class PermissiveJwsVerifier : JwsVerifier {
    override fun verify(headerBase64Url: String, payloadBase64Url: String, signatureBase64Url: String): Boolean = true
}
