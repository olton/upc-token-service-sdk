package ua.upc.ecomm_sdk.tokenservice.config

import io.ktor.client.*
import kotlinx.serialization.json.Json
import ua.upc.ecomm_sdk.tokenservice.security.DefaultJwsSigner
import ua.upc.ecomm_sdk.tokenservice.security.JwsSigner
import ua.upc.ecomm_sdk.tokenservice.security.JwsVerifier
import ua.upc.ecomm_sdk.tokenservice.security.PermissiveJwsVerifier

/**
 * Configuration options for UPC Token Service SDK client.
 */
data class UpcTokenServiceConfig(
    val merchantId: String,
    val terminalId: String,
    val environment: UpcTokenServiceEnvironment = UpcTokenServiceEnvironment.PRODUCTION,
    val customBaseUrl: String? = null,
    val useJwsEnvelope: Boolean = true,
    val jwsSigner: JwsSigner = DefaultJwsSigner(),
    val jwsVerifier: JwsVerifier = PermissiveJwsVerifier(),
    val httpClient: HttpClient? = null,
    val json: Json = defaultJson
) {
    val effectiveBaseUrl: String
        get() = customBaseUrl?.trimEnd('/') ?: environment.baseUrl.trimEnd('/')

    companion object {
        val defaultJson: Json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            coerceInputValues = true
            prettyPrint = false
        }
    }
}
