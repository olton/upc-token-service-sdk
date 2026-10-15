package ua.upc.ecomm_sdk.tokenservice.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for Resume Token API.
 * Endpoint: /go/token/resume
 */
@Serializable
data class ResumeTokenRequest(
    @SerialName("MerchantID")
    val merchantId: String,

    @SerialName("TerminalID")
    val terminalId: String,

    @SerialName("trackingId")
    val trackingId: String,

    @SerialName("UPCToken")
    val upcToken: String,

    @SerialName("ReasonCode")
    val reasonCode: String? = null,

    @SerialName("Reason")
    val reason: String? = null
)

/**
 * Response payload for Resume Token API.
 */
@Serializable
data class ResumeTokenResponse(
    @SerialName("FullTrackingId")
    val fullTrackingId: String? = null,

    @SerialName("tokenStatus")
    val tokenStatus: TokenStatusInfo? = null,

    @SerialName("TranCode")
    val tranCode: String? = null,

    @SerialName("Comment")
    val comment: String? = null
) {
    val isSuccess: Boolean get() = tranCode == "000" || tranCode == null
}
