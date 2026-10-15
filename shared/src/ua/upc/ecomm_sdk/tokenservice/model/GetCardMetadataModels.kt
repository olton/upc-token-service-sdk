package ua.upc.ecomm_sdk.tokenservice.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for Get Card Metadata API.
 * Endpoint: /go/card/metadata
 */
@Serializable
data class GetCardMetadataRequest(
    @SerialName("MerchantID")
    val merchantId: String,

    @SerialName("TerminalID")
    val terminalId: String,

    @SerialName("trackingId")
    val trackingId: String,

    @SerialName("UPCToken")
    val upcToken: String
)

/**
 * Expiration date of payment instrument.
 */
@Serializable
data class ExpirationDate(
    @SerialName("month")
    val month: String? = null,

    @SerialName("year")
    val year: String? = null
)

/**
 * Enabled services for card/token.
 */
@Serializable
data class EnabledServices(
    @SerialName("merchantPresentedQR")
    val merchantPresentedQR: String? = null
)

/**
 * Payment instrument details.
 */
@Serializable
data class PaymentInstrument(
    @SerialName("last4")
    val last4: Int? = null,

    @SerialName("expirationDate")
    val expirationDate: ExpirationDate? = null,

    @SerialName("cvv2PrintedInd")
    val cvv2PrintedInd: String? = null,

    @SerialName("expDatePrintedInd")
    val expDatePrintedInd: String? = null,

    @SerialName("paymentAccountReference")
    val paymentAccountReference: String? = null,

    @SerialName("enabledServices")
    val enabledServices: EnabledServices? = null
)

/**
 * Token info item in metadata.
 */
@Serializable
data class MetadataTokenInfo(
    @SerialName("tokenId")
    val tokenId: String? = null,

    @SerialName("tokenStatus")
    val tokenStatus: String? = null
)

/**
 * Card data reference item (e.g. contentId and contentType).
 */
@Serializable
data class CardDataItem(
    @SerialName("contentId")
    val contentId: String? = null,

    @SerialName("contentType")
    val contentType: String? = null
)

/**
 * Issuer flags indicating supported verification features.
 */
@Serializable
data class IssuerFlags(
    @SerialName("deviceBinding")
    val deviceBinding: Boolean? = null,

    @SerialName("cardholderVerification")
    val cardholderVerification: Boolean? = null,

    @SerialName("trustedBeneficiaryEnrollment")
    val trustedBeneficiaryEnrollment: Boolean? = null,

    @SerialName("delegatedAuthenticationSupported")
    val delegatedAuthenticationSupported: Boolean? = null
)

/**
 * Visual & issuer card metadata.
 */
@Serializable
data class CardMetaData(
    @SerialName("backgroundColor")
    val backgroundColor: String? = null,

    @SerialName("foregroundColor")
    val foregroundColor: String? = null,

    @SerialName("labelColor")
    val labelColor: String? = null,

    @SerialName("contactNumber")
    val contactNumber: String? = null,

    @SerialName("contactName")
    val contactName: String? = null,

    @SerialName("privacyPolicyURL")
    val privacyPolicyURL: String? = null,

    @SerialName("termsAndConditionsURL")
    val termsAndConditionsURL: String? = null,

    @SerialName("shortDescription")
    val shortDescription: String? = null,

    @SerialName("longDescription")
    val longDescription: String? = null,

    @SerialName("cardData")
    val cardData: List<CardDataItem> = emptyList(),

    @SerialName("issuerFlags")
    val issuerFlags: IssuerFlags? = null
)

/**
 * Card metadata result root container.
 */
@Serializable
data class CardMetaDataResult(
    @SerialName("trackingId")
    val trackingId: String? = null,

    @SerialName("brand")
    val brand: String? = null,

    @SerialName("vPanEnrollmentID")
    val vPanEnrollmentID: String? = null,

    @SerialName("paymentInstrument")
    val paymentInstrument: PaymentInstrument? = null,

    @SerialName("tokens")
    val tokens: List<MetadataTokenInfo> = emptyList(),

    @SerialName("cardMetaData")
    val cardMetaData: CardMetaData? = null
)

/**
 * Response payload for Get Card Metadata API.
 */
@Serializable
data class GetCardMetadataResponse(
    @SerialName("metaData")
    val metaData: CardMetaDataResult? = null,

    @SerialName("TranCode")
    val tranCode: String? = null,

    @SerialName("Comment")
    val comment: String? = null
) {
    val isSuccess: Boolean get() = tranCode == "000" || tranCode == null
}
