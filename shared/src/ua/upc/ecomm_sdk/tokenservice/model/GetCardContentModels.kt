package ua.upc.ecomm_sdk.tokenservice.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for Get Card Content API.
 * Endpoint: /go/card/content
 */
@Serializable
data class GetCardContentRequest(
    @SerialName("MerchantID")
    val merchantId: String,

    @SerialName("TerminalID")
    val terminalId: String,

    @SerialName("trackingId")
    val trackingId: String,

    @SerialName("contentId")
    val contentId: String,

    @SerialName("Brand")
    val brand: String
)

/**
 * Card content data structure containing encoded visual asset (card symbol, card art).
 */
@Serializable
data class CardContentData(
    @SerialName("contentId")
    val contentId: String? = null,

    @SerialName("contentType")
    val contentType: String? = null,

    @SerialName("mimeType")
    val mimeType: String? = null,

    @SerialName("width")
    val width: Int? = null,

    @SerialName("height")
    val height: Int? = null,

    @SerialName("encodedData")
    val encodedData: String? = null,

    @SerialName("altText")
    val altText: String? = null
)

/**
 * Response payload for Get Card Content API.
 */
@Serializable
data class GetCardContentResponse(
    @SerialName("cardContent")
    val cardContent: CardContentData? = null,

    @SerialName("TranCode")
    val tranCode: String? = null,

    @SerialName("Comment")
    val comment: String? = null
) {
    val isSuccess: Boolean get() = tranCode == "000" || tranCode == null
}
