package ua.upc.ecomm_sdk.tokenservice.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceConfig
import ua.upc.ecomm_sdk.tokenservice.error.UpcApiException
import ua.upc.ecomm_sdk.tokenservice.error.UpcNetworkException
import ua.upc.ecomm_sdk.tokenservice.error.UpcSecurityException
import ua.upc.ecomm_sdk.tokenservice.error.UpcSerializationException
import ua.upc.ecomm_sdk.tokenservice.model.*
import ua.upc.ecomm_sdk.tokenservice.security.Base64Url

/**
 * Default implementation of [UpcTokenServiceClient].
 */
internal class UpcTokenServiceClientImpl(
    override val config: UpcTokenServiceConfig
) : UpcTokenServiceClient {

    private val client: HttpClient = config.httpClient ?: HttpClient()
    private val ownsClient: Boolean = config.httpClient == null

    override suspend fun createToken(request: CreateTokenRequest): UpcResponse<CreateTokenResponse> =
        executeRequest(
            path = "/go/token/create",
            request = request,
            requestSerializer = CreateTokenRequest.serializer(),
            responseSerializer = CreateTokenResponse.serializer()
        )

    override suspend fun refreshToken(request: RefreshTokenRequest): UpcResponse<RefreshTokenResponse> =
        executeRequest(
            path = "/go/token/refresh",
            request = request,
            requestSerializer = RefreshTokenRequest.serializer(),
            responseSerializer = RefreshTokenResponse.serializer()
        )

    override suspend fun deleteToken(request: DeleteTokenRequest): UpcResponse<DeleteTokenResponse> =
        executeRequest(
            path = "/go/token/delete",
            request = request,
            requestSerializer = DeleteTokenRequest.serializer(),
            responseSerializer = DeleteTokenResponse.serializer()
        )

    override suspend fun getTokenStatus(request: GetTokenStatusRequest): UpcResponse<GetTokenStatusResponse> =
        executeRequest(
            path = "/go/token/status",
            request = request,
            requestSerializer = GetTokenStatusRequest.serializer(),
            responseSerializer = GetTokenStatusResponse.serializer()
        )

    override suspend fun suspendToken(request: SuspendTokenRequest): UpcResponse<SuspendTokenResponse> =
        executeRequest(
            path = "/go/token/suspend",
            request = request,
            requestSerializer = SuspendTokenRequest.serializer(),
            responseSerializer = SuspendTokenResponse.serializer()
        )

    override suspend fun resumeToken(request: ResumeTokenRequest): UpcResponse<ResumeTokenResponse> =
        executeRequest(
            path = "/go/token/resume",
            request = request,
            requestSerializer = ResumeTokenRequest.serializer(),
            responseSerializer = ResumeTokenResponse.serializer()
        )

    override suspend fun getCardContent(request: GetCardContentRequest): UpcResponse<GetCardContentResponse> =
        executeRequest(
            path = "/go/card/content",
            request = request,
            requestSerializer = GetCardContentRequest.serializer(),
            responseSerializer = GetCardContentResponse.serializer()
        )

    override suspend fun getCardMetadata(request: GetCardMetadataRequest): UpcResponse<GetCardMetadataResponse> =
        executeRequest(
            path = "/go/card/metadata",
            request = request,
            requestSerializer = GetCardMetadataRequest.serializer(),
            responseSerializer = GetCardMetadataResponse.serializer()
        )

    override fun close() {
        if (ownsClient) {
            client.close()
        }
    }

    private suspend fun <Req, Resp> executeRequest(
        path: String,
        request: Req,
        requestSerializer: KSerializer<Req>,
        responseSerializer: KSerializer<Resp>
    ): UpcResponse<Resp> {
        val url = "${config.effectiveBaseUrl}$path"
        val requestBodyString: String

        try {
            val payloadJson = config.json.encodeToString(requestSerializer, request)
            if (config.useJwsEnvelope) {
                val headerJson = config.json.encodeToString(
                    JwsHeader.serializer(),
                    JwsHeader(alg = config.jwsSigner.algorithm)
                )
                val headerB64 = Base64Url.encode(headerJson)
                val payloadB64 = Base64Url.encode(payloadJson)
                val signature = config.jwsSigner.sign(headerB64, payloadB64)
                val jwsMessage = JwsMessage(headerB64, payloadB64, signature)
                requestBodyString = config.json.encodeToString(JwsMessage.serializer(), jwsMessage)
            } else {
                requestBodyString = payloadJson
            }
        } catch (e: Exception) {
            return UpcResponse.Error(
                exception = UpcSerializationException("Failed to serialize request body: ${e.message}", e)
            )
        }

        val httpResponse: HttpResponse
        val responseBodyText: String
        try {
            httpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(requestBodyString)
            }
            responseBodyText = httpResponse.bodyAsText()
        } catch (e: Exception) {
            return UpcResponse.Error(
                exception = UpcNetworkException("Network error calling $url: ${e.message}", e)
            )
        }

        val statusCode = httpResponse.status.value
        val isHttpOk = httpResponse.status.isSuccess()

        var decodedJson = responseBodyText
        var receivedJws: JwsMessage? = null

        // Try unwrapping JWS if response is in JWS format
        try {
            val potentialJws = config.json.decodeFromString(JwsMessage.serializer(), responseBodyText)
            if (potentialJws.header.isNotBlank() && potentialJws.payload.isNotBlank()) {
                val isValid = config.jwsVerifier.verify(
                    headerBase64Url = potentialJws.header,
                    payloadBase64Url = potentialJws.payload,
                    signatureBase64Url = potentialJws.signature
                )
                if (!isValid) {
                    return UpcResponse.Error(
                        exception = UpcSecurityException("Invalid JWS signature in response"),
                        rawResponse = responseBodyText
                    )
                }
                receivedJws = potentialJws
                decodedJson = Base64Url.decodeToString(potentialJws.payload)
            }
        } catch (_: Exception) {
            // Not a JWS envelope, proceed with direct JSON
        }

        var tranCode: String? = null
        var comment: String? = null
        try {
            val jsonObject = config.json.decodeFromString(JsonObject.serializer(), decodedJson)
            tranCode = jsonObject["TranCode"]?.jsonPrimitive?.content
            comment = jsonObject["Comment"]?.jsonPrimitive?.content
        } catch (_: Exception) {
            // Ignore if not a json object
        }

        if (!isHttpOk) {
            return UpcResponse.Error(
                exception = UpcApiException(
                    message = "HTTP request failed with status $statusCode",
                    tranCode = tranCode,
                    comment = comment,
                    httpStatusCode = statusCode,
                    rawResponse = responseBodyText
                ),
                tranCode = tranCode,
                comment = comment,
                rawResponse = responseBodyText
            )
        }

        if (tranCode != null && tranCode != "000") {
            return UpcResponse.Error(
                exception = UpcApiException(
                    message = "UPC returned non-success transaction code $tranCode",
                    tranCode = tranCode,
                    comment = comment,
                    httpStatusCode = statusCode,
                    rawResponse = decodedJson
                ),
                tranCode = tranCode,
                comment = comment,
                rawResponse = decodedJson
            )
        }

        return try {
            val responseData = config.json.decodeFromString(responseSerializer, decodedJson)
            UpcResponse.Success(
                data = responseData,
                rawJson = decodedJson,
                jwsMessage = receivedJws
            )
        } catch (e: Exception) {
            UpcResponse.Error(
                exception = UpcSerializationException(
                    "Failed to deserialize response body to expected model: ${e.message}",
                    e
                ),
                tranCode = tranCode,
                comment = comment,
                rawResponse = decodedJson
            )
        }
    }
}
