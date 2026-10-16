package ua.upc.ecomm_sdk.tokenservice.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.promise
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceConfig
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceEnvironment
import ua.upc.ecomm_sdk.tokenservice.model.*
import kotlin.js.Promise

/**
 * JavaScript / NodeJS-friendly wrapper for [UpcTokenServiceClient], returning standard JavaScript [Promise].
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class UpcTokenServiceJsClient internal constructor(
    private val delegate: UpcTokenServiceClient,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {

    companion object {
        fun create(
            merchantId: String,
            terminalId: String,
            environment: String = "TEST",
            baseUrl: String? = null,
            useJwsEnvelope: Boolean = true
        ): UpcTokenServiceJsClient {
            val env = when (environment.uppercase()) {
                "PRODUCTION", "PROD" -> UpcTokenServiceEnvironment.PRODUCTION
                else -> UpcTokenServiceEnvironment.TEST
            }
            val config = UpcTokenServiceConfig(
                merchantId = merchantId,
                terminalId = terminalId,
                environment = env,
                customBaseUrl = baseUrl,
                useJwsEnvelope = useJwsEnvelope
            )
            return UpcTokenServiceJsClient(UpcTokenServiceClient.create(config))
        }
    }

    fun createToken(
        trackingId: String,
        cardNum: String,
        expMonth: String,
        expYear: String,
        cvNum: String? = null
    ): Promise<UpcResponse<CreateTokenResponse>> =
        scope.promise {
            delegate.createToken(
                trackingId = trackingId,
                cardNum = cardNum,
                expMonth = expMonth,
                expYear = expYear,
                cvNum = cvNum
            )
        }

    fun refreshToken(
        trackingId: String,
        upcToken: String
    ): Promise<UpcResponse<RefreshTokenResponse>> =
        scope.promise {
            delegate.refreshToken(
                trackingId = trackingId,
                upcToken = upcToken
            )
        }

    fun deleteToken(
        trackingId: String,
        upcToken: String,
        reasonCode: String? = null,
        reason: String? = null
    ): Promise<UpcResponse<DeleteTokenResponse>> =
        scope.promise {
            delegate.deleteToken(
                trackingId = trackingId,
                upcToken = upcToken,
                reasonCode = reasonCode,
                reason = reason
            )
        }

    fun getTokenStatus(
        trackingId: String,
        upcToken: String
    ): Promise<UpcResponse<GetTokenStatusResponse>> =
        scope.promise {
            delegate.getTokenStatus(
                trackingId = trackingId,
                upcToken = upcToken
            )
        }

    fun suspendToken(
        trackingId: String,
        upcToken: String,
        reasonCode: String? = null,
        reason: String? = null
    ): Promise<UpcResponse<SuspendTokenResponse>> =
        scope.promise {
            delegate.suspendToken(
                trackingId = trackingId,
                upcToken = upcToken,
                reasonCode = reasonCode,
                reason = reason
            )
        }

    fun resumeToken(
        trackingId: String,
        upcToken: String,
        reasonCode: String? = null,
        reason: String? = null
    ): Promise<UpcResponse<ResumeTokenResponse>> =
        scope.promise {
            delegate.resumeToken(
                trackingId = trackingId,
                upcToken = upcToken,
                reasonCode = reasonCode,
                reason = reason
            )
        }

    fun getCardContent(
        trackingId: String,
        contentId: String,
        brand: String = CardBrands.VISA
    ): Promise<UpcResponse<GetCardContentResponse>> =
        scope.promise {
            delegate.getCardContent(
                trackingId = trackingId,
                contentId = contentId,
                brand = brand
            )
        }

    fun getCardMetadata(
        trackingId: String,
        upcToken: String
    ): Promise<UpcResponse<GetCardMetadataResponse>> =
        scope.promise {
            delegate.getCardMetadata(
                trackingId = trackingId,
                upcToken = upcToken
            )
        }

    fun close() {
        delegate.close()
        scope.cancel()
    }
}

/**
 * Extension function to convert a Kotlin [UpcTokenServiceClient] into a [UpcTokenServiceJsClient].
 */
fun UpcTokenServiceClient.asJsClient(): UpcTokenServiceJsClient = UpcTokenServiceJsClient(this)
