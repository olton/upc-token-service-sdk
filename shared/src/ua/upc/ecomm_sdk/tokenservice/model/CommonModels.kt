package ua.upc.ecomm_sdk.tokenservice.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Common card data used for token creation.
 */
@Serializable
data class CardData(
    @SerialName("CardNum")
    val cardNum: String,

    @SerialName("expMonth")
    val expMonth: String,

    @SerialName("expYear")
    val expYear: String,

    @SerialName("CVNum")
    val cvNum: String? = null
)

/**
 * Masked card info returned in token responses.
 */
@Serializable
data class CardInfo(
    @SerialName("mask")
    val mask: String? = null,

    @SerialName("expMonth")
    val expMonth: String? = null,

    @SerialName("expYear")
    val expYear: String? = null,

    @SerialName("brand")
    val brand: String? = null
)

/**
 * External network token data (e.g. Mastercard / Visa network tokens).
 */
@Serializable
data class ExtDataToken(
    @SerialName("TAVV")
    val tavv: String? = null,

    @SerialName("TokenNumber")
    val tokenNumber: String? = null,

    @SerialName("TokenExpDate")
    val tokenExpDate: String? = null
)

/**
 * Token status description structure.
 */
@Serializable
data class TokenStatusInfo(
    @SerialName("Code")
    val code: String? = null,

    @SerialName("TokenStatus")
    val tokenStatus: String? = null
) {
    val isSuccess: Boolean get() = code == "000" || code == null
    val parsedStatus: TokenStatus get() = TokenStatus.fromValue(tokenStatus)
}

/**
 * Token statuses supported by UPC Token Service.
 */
enum class TokenStatus(val value: String) {
    ACTIVE("ACTIVE"),
    SUSPENDED("SUSPENDED"),
    DEACTIVATED("DEACTIVATED"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun fromValue(value: String?): TokenStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

/**
 * Standard reason codes for token lifecycle modifications (Delete, Suspend, Resume).
 */
object ReasonCodes {
    const val SUSPECTED_FRAUD = "SUSPECTED_FRAUD"
    const val LOST_STOLEN = "LOST_STOLEN"
    const val CARDHOLDER_CONFIRMED = "CARDHOLDER_CONFIRMED"
    const val EXPIRED = "EXPIRED"
    const val OTHER = "OTHER"
}

/**
 * Supported card brands.
 */
object CardBrands {
    const val VISA = "VISA"
    const val MAST = "MAST"
    const val MASTERCARD = "MASTERCARD"
    const val PROSTIR = "PROSTIR"
}

/**
 * Card content types.
 */
object ContentTypes {
    const val CARD_SYMBOL = "cardSymbol"
    const val DIGITAL_CARD_ART = "digitalCardArt"
}
