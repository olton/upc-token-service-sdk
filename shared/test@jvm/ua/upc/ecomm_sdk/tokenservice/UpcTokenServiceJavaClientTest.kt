package ua.upc.ecomm_sdk.tokenservice

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceClient
import ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceJavaClient
import ua.upc.ecomm_sdk.tokenservice.client.asJavaClient
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceConfig
import ua.upc.ecomm_sdk.tokenservice.security.JwsVerifier
import ua.upc.ecomm_sdk.tokenservice.model.*

class UpcTokenServiceJavaClientTest {

    @Test
    fun testJavaClientCompletableFutureSuccess() {
        val jsonPayload = """
            {
                "TranCode": "000",
                "trackingId": "tx-java-1",
                "UPCToken": "TOKEN123",
                "cardInfo": {
                    "brand": "VISA",
                    "maskedPan": "411111******1111"
                }
            }
        """.trimIndent()

        val mockEngine = MockEngine { _ ->
            respond(
                content = jsonPayload,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val config = UpcTokenServiceConfig(
            merchantId = "1750000",
            terminalId = "E1750000",
            useJwsEnvelope = false,
            jwsVerifier = object : JwsVerifier {
                override fun verify(headerBase64Url: String, payloadBase64Url: String, signatureBase64Url: String) = true
            },
            httpClient = HttpClient(mockEngine)
        )

        val kotlinClient = UpcTokenServiceClient.create(config)
        val javaClient = kotlinClient.asJavaClient()

        val future = javaClient.createToken(
            trackingId = "tx-java-1",
            cardNum = "4111111111111111",
            expMonth = "12",
            expYear = "2028"
        )

        val response = future.get()
        assertTrue(response.isSuccess)
        assertEquals("TOKEN123", response.getOrNull()?.upcToken)

        javaClient.close()
    }
}
