package ua.upc.ecomm_sdk.tokenservice.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for Get Token Status API.
 * Endpoint: /go/token/status
 */
@Serializable
data class GetTokenStatusRequest(
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
 * Response payload for Get Token Status API.
 */
@Serializable
data class GetTokenStatusResponse(
    @SerialName("FullTrackingId")
    val fullTrackingId: String? = null,

    @SerialName("tokenStatus")
    val tokenStatus: TokenStatusInfo? = null,

    @SerialName("UPCToken")
    val upcToken: String? = null,

    @SerialName("TranCode")
    val tranCode: String? = null,

    @SerialName("Comment")
    val comment: String? = null
) {
    val isSuccess: Boolean get() = tranCode == "000" || tranCode == null

    /**
     * Extracts token status from either structured [tokenStatus] or parsed from [comment].
     * (UPC sometimes returns status in comment like "status = ACTIVE").
     */
    val currentStatus: TokenStatus
        get() {
            if (tokenStatus?.parsedStatus != null && tokenStatus.parsedStatus != TokenStatus.UNKNOWN) {
                return tokenStatus.parsedStatus
            }
            if (comment != null) {
                val match = Regex("status\\s*=\\s*([A-Za-z_]+)", RegexOption.IGNORE_CASE).find(comment)
                if (match != null) {
                    return TokenStatus.fromValue(match.groupValues[1])
                }
            }
            return TokenStatus.UNKNOWN
        }
}
