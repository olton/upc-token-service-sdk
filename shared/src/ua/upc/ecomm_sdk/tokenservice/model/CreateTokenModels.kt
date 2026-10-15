package ua.upc.ecomm_sdk.tokenservice.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for Create Token API.
 * Endpoint: /go/token/create
 */
@Serializable
data class CreateTokenRequest(
    @SerialName("MerchantID")
    val merchantId: String,

    @SerialName("TerminalID")
    val terminalId: String,

    @SerialName("TrackingId")
    val trackingId: String,

    @SerialName("card")
    val card: CardData
)

/**
 * Response payload for Create Token API.
 */
@Serializable
data class CreateTokenResponse(
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

    @SerialName("useMCToken")
    val useMCToken: Boolean? = null,

    @SerialName("useVisaToken")
    val useVisaToken: Boolean? = null,

    @SerialName("TranCode")
    val tranCode: String? = null,

    @SerialName("Comment")
    val comment: String? = null
) {
    val isSuccess: Boolean get() = tranCode == "000" || tranCode == null
}
