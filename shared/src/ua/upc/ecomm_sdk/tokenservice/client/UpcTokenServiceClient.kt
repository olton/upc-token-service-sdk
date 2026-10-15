package ua.upc.ecomm_sdk.tokenservice.client

import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceConfig
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceEnvironment
import ua.upc.ecomm_sdk.tokenservice.model.*

/**
 * Main client interface for UPC Token Service SDK.
 *
 * Implements all 8 core API methods:
 * 1. Create Token
 * 2. Refresh Token
 * 3. Delete Token
 * 4. Get Token Status
 * 5. Suspend Token
 * 6. Resume Token
 * 7. Get Card Content
 * 8. Get Card Metadata
 */
interface UpcTokenServiceClient : AutoCloseable {

    /**
     * Active client configuration.
     */
    val config: UpcTokenServiceConfig

    /**
     * 1. Create Token
     * Creates a new token for card details (PCI DSS merchants).
     * Endpoint: /go/token/create
     */
    suspend fun createToken(request: CreateTokenRequest): UpcResponse<CreateTokenResponse>

    suspend fun createToken(
        trackingId: String,
        card: CardData
    ): UpcResponse<CreateTokenResponse> = createToken(
        CreateTokenRequest(
            merchantId = config.merchantId,
            terminalId = config.terminalId,
            trackingId = trackingId,
            card = card
        )
    )

    suspend fun createToken(
        trackingId: String,
        cardNum: String,
        expMonth: String,
        expYear: String,
        cvNum: String? = null
    ): UpcResponse<CreateTokenResponse> = createToken(
        trackingId = trackingId,
        card = CardData(cardNum = cardNum, expMonth = expMonth, expYear = expYear, cvNum = cvNum)
    )

    /**
     * 2. Refresh Token
     * Refreshes dynamic cryptogram / token data for an existing token.
     * Endpoint: /go/token/refresh
     */
    suspend fun refreshToken(request: RefreshTokenRequest): UpcResponse<RefreshTokenResponse>

    suspend fun refreshToken(
        trackingId: String,
        upcToken: String
    ): UpcResponse<RefreshTokenResponse> = refreshToken(
        RefreshTokenRequest(
            merchantId = config.merchantId,
            terminalId = config.terminalId,
            trackingId = trackingId,
            upcToken = upcToken
        )
    )

    /**
     * 3. Delete Token
     * Deactivates an existing token.
     * Endpoint: /go/token/delete
     */
    suspend fun deleteToken(request: DeleteTokenRequest): UpcResponse<DeleteTokenResponse>

    suspend fun deleteToken(
        trackingId: String,
        upcToken: String,
        reasonCode: String? = null,
        reason: String? = null
    ): UpcResponse<DeleteTokenResponse> = deleteToken(
        DeleteTokenRequest(
            merchantId = config.merchantId,
            terminalId = config.terminalId,
            trackingId = trackingId,
            upcToken = upcToken,
            reasonCode = reasonCode,
            reason = reason
        )
    )

    /**
     * 4. Get Token Status
     * Retrieves the current status (ACTIVE, SUSPENDED, DEACTIVATED) of a token.
     * Endpoint: /go/token/status
     */
    suspend fun getTokenStatus(request: GetTokenStatusRequest): UpcResponse<GetTokenStatusResponse>

    suspend fun getTokenStatus(
        trackingId: String,
        upcToken: String
    ): UpcResponse<GetTokenStatusResponse> = getTokenStatus(
        GetTokenStatusRequest(
            merchantId = config.merchantId,
            terminalId = config.terminalId,
            trackingId = trackingId,
            upcToken = upcToken
        )
    )

    /**
     * 5. Suspend Token
     * Suspends an active token.
     * Endpoint: /go/token/suspend
     */
    suspend fun suspendToken(request: SuspendTokenRequest): UpcResponse<SuspendTokenResponse>

    suspend fun suspendToken(
        trackingId: String,
        upcToken: String,
        reasonCode: String? = null,
        reason: String? = null
    ): UpcResponse<SuspendTokenResponse> = suspendToken(
        SuspendTokenRequest(
            merchantId = config.merchantId,
            terminalId = config.terminalId,
            trackingId = trackingId,
            upcToken = upcToken,
            reasonCode = reasonCode,
            reason = reason
        )
    )

    /**
     * 6. Resume Token
     * Resumes a previously suspended token.
     * Endpoint: /go/token/resume
     */
    suspend fun resumeToken(request: ResumeTokenRequest): UpcResponse<ResumeTokenResponse>

    suspend fun resumeToken(
        trackingId: String,
        upcToken: String,
        reasonCode: String? = null,
        reason: String? = null
    ): UpcResponse<ResumeTokenResponse> = resumeToken(
        ResumeTokenRequest(
            merchantId = config.merchantId,
            terminalId = config.terminalId,
            trackingId = trackingId,
            upcToken = upcToken,
            reasonCode = reasonCode,
            reason = reason
        )
    )

    /**
     * 7. Get Card Content
     * Retrieves visual card assets (image, symbol, card art) by content ID.
     * Endpoint: /go/card/content
     */
    suspend fun getCardContent(request: GetCardContentRequest): UpcResponse<GetCardContentResponse>

    suspend fun getCardContent(
        trackingId: String,
        contentId: String,
        brand: String = CardBrands.VISA
    ): UpcResponse<GetCardContentResponse> = getCardContent(
        GetCardContentRequest(
            merchantId = config.merchantId,
            terminalId = config.terminalId,
            trackingId = trackingId,
            contentId = contentId,
            brand = brand
        )
    )

    /**
     * 8. Get Card Metadata
     * Retrieves card and issuer metadata associated with a token.
     * Endpoint: /go/card/metadata
     */
    suspend fun getCardMetadata(request: GetCardMetadataRequest): UpcResponse<GetCardMetadataResponse>

    suspend fun getCardMetadata(
        trackingId: String,
        upcToken: String
    ): UpcResponse<GetCardMetadataResponse> = getCardMetadata(
        GetCardMetadataRequest(
            merchantId = config.merchantId,
            terminalId = config.terminalId,
            trackingId = trackingId,
            upcToken = upcToken
        )
    )

    companion object {
        /**
         * Creates a new [UpcTokenServiceClient] with specified [config].
         */
        fun create(config: UpcTokenServiceConfig): UpcTokenServiceClient =
            UpcTokenServiceClientImpl(config)

        /**
         * Convenience factory for creating a client.
         */
        fun create(
            merchantId: String,
            terminalId: String,
            environment: UpcTokenServiceEnvironment = UpcTokenServiceEnvironment.PRODUCTION
        ): UpcTokenServiceClient = create(
            UpcTokenServiceConfig(
                merchantId = merchantId,
                terminalId = terminalId,
                environment = environment
            )
        )
    }
}
