package ua.upc.ecomm_sdk.tokenservice.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for Refresh Token API.
 * Endpoint: /go/token/refresh
 */
@Serializable
data class RefreshTokenRequest(
    @SerialName("MerchantID")
    val merchantId: String,

    @SerialName("TerminalID")
    val terminalId: String,

    @SerialName("TrackingId")
    val trackingId: String,

    @SerialName("UPCToken")
    val upcToken: String
)

/**
 * Response payload for Refresh Token API.
 */
@Serializable
data class RefreshTokenResponse(
    @SerialName("MerchantID")
    val merchantId: String? = null,

    @SerialName("TerminalID")
    val terminalId: String? = null,

    @SerialName("fullTrackingId")
    val fullTrackingId: String? = null,

    @SerialName("UPCToken")
    val upcToken: String? = null,

    @SerialName("cardInfo")
    val cardInfo: CardInfo? = null,

    @SerialName("extDataToken")
    val extDataToken: ExtDataToken? = null,

    @SerialName("useVisaToken")
    val useVisaToken: Boolean? = null,

    @SerialName("useMCToken")
    val useMCToken: Boolean? = null,

    @SerialName("TranCode")
    val tranCode: String? = null,

    @SerialName("Comment")
    val comment: String? = null
) {
    val isSuccess: Boolean get() = tranCode == "000" || tranCode == null
}
