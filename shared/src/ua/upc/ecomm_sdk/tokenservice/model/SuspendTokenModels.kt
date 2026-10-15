package ua.upc.ecomm_sdk.tokenservice.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for Suspend Token API.
 * Endpoint: /go/token/suspend
 */
@Serializable
data class SuspendTokenRequest(
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
 * Response payload for Suspend Token API.
 */
@Serializable
data class SuspendTokenResponse(
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
