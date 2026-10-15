package ua.upc.ecomm_sdk.tokenservice.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JWS Envelope message wrapping requests and responses in UPC Token Service.
 */
@Serializable
data class JwsMessage(
    @SerialName("header")
    val header: String,

    @SerialName("payload")
    val payload: String,

    @SerialName("signature")
    val signature: String
)

/**
 * JWS Header data structure.
 */
@Serializable
data class JwsHeader(
    @SerialName("alg")
    val alg: String = "RS256",

    @SerialName("typ")
    val typ: String? = "JWT"
)
