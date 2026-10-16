package ua.upc.ecomm_sdk.tokenservice.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceConfig
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceEnvironment
import ua.upc.ecomm_sdk.tokenservice.model.*
import java.util.concurrent.CompletableFuture

/**
 * Java-friendly wrapper for [UpcTokenServiceClient], returning standard Java [CompletableFuture].
 */
class UpcTokenServiceJavaClient(
    val delegate: UpcTokenServiceClient,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : AutoCloseable {

    companion object {
        @JvmStatic
        @JvmOverloads
        fun create(
            merchantId: String,
            terminalId: String,
            environment: UpcTokenServiceEnvironment = UpcTokenServiceEnvironment.TEST,
            baseUrl: String? = null,
            useJwsEnvelope: Boolean = true
        ): UpcTokenServiceJavaClient {
            val config = UpcTokenServiceConfig(
                merchantId = merchantId,
                terminalId = terminalId,
                environment = environment,
                customBaseUrl = baseUrl,
                useJwsEnvelope = useJwsEnvelope
            )
            return UpcTokenServiceJavaClient(UpcTokenServiceClient.create(config))
        }

        @JvmStatic
        fun create(config: UpcTokenServiceConfig): UpcTokenServiceJavaClient {
            return UpcTokenServiceJavaClient(UpcTokenServiceClient.create(config))
        }
    }

    fun createToken(request: CreateTokenRequest): CompletableFuture<UpcResponse<CreateTokenResponse>> =
        future { delegate.createToken(request) }

    fun createToken(
        trackingId: String,
        card: CardData
    ): CompletableFuture<UpcResponse<CreateTokenResponse>> =
        future {
            delegate.createToken(
                trackingId = trackingId,
                card = card
            )
        }

    fun createToken(
        trackingId: String,
        cardNum: String,
        expMonth: String,
        expYear: String,
        cvNum: String? = null
    ): CompletableFuture<UpcResponse<CreateTokenResponse>> =
        future {
            delegate.createToken(
                trackingId = trackingId,
                cardNum = cardNum,
                expMonth = expMonth,
                expYear = expYear,
                cvNum = cvNum
            )
        }

    fun refreshToken(request: RefreshTokenRequest): CompletableFuture<UpcResponse<RefreshTokenResponse>> =
        future { delegate.refreshToken(request) }

    fun refreshToken(
        trackingId: String,
        upcToken: String
    ): CompletableFuture<UpcResponse<RefreshTokenResponse>> =
        future {
            delegate.refreshToken(
                trackingId = trackingId,
                upcToken = upcToken
            )
        }

    fun deleteToken(request: DeleteTokenRequest): CompletableFuture<UpcResponse<DeleteTokenResponse>> =
        future { delegate.deleteToken(request) }

    fun deleteToken(
        trackingId: String,
        upcToken: String,
        reasonCode: String? = null,
        reason: String? = null
    ): CompletableFuture<UpcResponse<DeleteTokenResponse>> =
        future {
            delegate.deleteToken(
                trackingId = trackingId,
                upcToken = upcToken,
                reasonCode = reasonCode,
                reason = reason
            )
        }

    fun getTokenStatus(request: GetTokenStatusRequest): CompletableFuture<UpcResponse<GetTokenStatusResponse>> =
        future { delegate.getTokenStatus(request) }

    fun getTokenStatus(
        trackingId: String,
        upcToken: String
    ): CompletableFuture<UpcResponse<GetTokenStatusResponse>> =
        future {
            delegate.getTokenStatus(
                trackingId = trackingId,
                upcToken = upcToken
            )
        }

    fun suspendToken(request: SuspendTokenRequest): CompletableFuture<UpcResponse<SuspendTokenResponse>> =
        future { delegate.suspendToken(request) }

    fun suspendToken(
        trackingId: String,
        upcToken: String,
        reasonCode: String? = null,
        reason: String? = null
    ): CompletableFuture<UpcResponse<SuspendTokenResponse>> =
        future {
            delegate.suspendToken(
                trackingId = trackingId,
                upcToken = upcToken,
                reasonCode = reasonCode,
                reason = reason
            )
        }

    fun resumeToken(request: ResumeTokenRequest): CompletableFuture<UpcResponse<ResumeTokenResponse>> =
        future { delegate.resumeToken(request) }

    fun resumeToken(
        trackingId: String,
        upcToken: String,
        reasonCode: String? = null,
        reason: String? = null
    ): CompletableFuture<UpcResponse<ResumeTokenResponse>> =
        future {
            delegate.resumeToken(
                trackingId = trackingId,
                upcToken = upcToken,
                reasonCode = reasonCode,
                reason = reason
            )
        }

    fun getCardContent(request: GetCardContentRequest): CompletableFuture<UpcResponse<GetCardContentResponse>> =
        future { delegate.getCardContent(request) }

    fun getCardContent(
        trackingId: String,
        contentId: String,
        brand: String = CardBrands.VISA
    ): CompletableFuture<UpcResponse<GetCardContentResponse>> =
        future {
            delegate.getCardContent(
                trackingId = trackingId,
                contentId = contentId,
                brand = brand
            )
        }

    fun getCardMetadata(request: GetCardMetadataRequest): CompletableFuture<UpcResponse<GetCardMetadataResponse>> =
        future { delegate.getCardMetadata(request) }

    fun getCardMetadata(
        trackingId: String,
        upcToken: String
    ): CompletableFuture<UpcResponse<GetCardMetadataResponse>> =
        future {
            delegate.getCardMetadata(
                trackingId = trackingId,
                upcToken = upcToken
            )
        }

    override fun close() {
        delegate.close()
        scope.cancel()
    }

    private fun <T> future(block: suspend () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        scope.launch {
            try {
                val result = block()
                future.complete(result)
            } catch (t: Throwable) {
                future.completeExceptionally(t)
            }
        }
        return future
    }
}

/**
 * Extension function to convert a Kotlin [UpcTokenServiceClient] into a [UpcTokenServiceJavaClient].
 */
fun UpcTokenServiceClient.asJavaClient(): UpcTokenServiceJavaClient = UpcTokenServiceJavaClient(this)
