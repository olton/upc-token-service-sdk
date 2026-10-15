package ua.upc.ecomm_sdk.tokenservice

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import ua.upc.ecomm_sdk.tokenservice.client.UpcResponse
import ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceClient
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceConfig
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceEnvironment
import ua.upc.ecomm_sdk.tokenservice.model.*
import ua.upc.ecomm_sdk.tokenservice.security.Base64Url
import ua.upc.ecomm_sdk.tokenservice.security.JwsVerifier
import kotlin.test.*

class UpcTokenServiceClientTest {

    private fun createMockClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
        useJws: Boolean = true,
        verifier: JwsVerifier? = null
    ): UpcTokenServiceClient {
        val mockEngine = MockEngine { request ->
            handler(request)
        }
        val httpClient = HttpClient(mockEngine)
        val config = UpcTokenServiceConfig(
            merchantId = "1750000",
            terminalId = "E1750000",
            environment = UpcTokenServiceEnvironment.TEST,
            useJwsEnvelope = useJws,
            jwsVerifier = verifier ?: object : JwsVerifier {
                override fun verify(headerBase64Url: String, payloadBase64Url: String, signatureBase64Url: String) = true
            },
            httpClient = httpClient
        )
        return UpcTokenServiceClient.create(config)
    }

    private fun wrapInJws(payloadJson: String): String {
        val header = Base64Url.encode("{\"alg\":\"RS512\"}")
        val payload = Base64Url.encode(payloadJson)
        val sig = "mock_sig"
        return "{\"header\":\"$header\",\"payload\":\"$payload\",\"signature\":\"$sig\"}"
    }

    @Test
    fun testCreateTokenSuccess() = runTest {
        val samplePayload = """
            {
                "MerchantID": "1750000",
                "TerminalID": "E1750000",
                "fullTrackingId": "23413512fwsd",
                "UPCToken": "6F3D415A7D26B93A33C31065B2FF912A",
                "cardInfo": {
                    "mask": "550690******0006",
                    "expMonth": "04",
                    "expYear": "2024",
                    "brand": "MAST"
                },
                "extDataToken": {
                    "TAVV": "AQAAAAAAPReETR8AmTCDgrIAAAA=",
                    "TokenNumber": "5167974868281209",
                    "TokenExpDate": "2404"
                },
                "useMCToken": true,
                "TranCode": "000"
            }
        """.trimIndent()

        val client = createMockClient({ request ->
            assertEquals("/go/token/create", request.url.encodedPath)
            respond(
                content = ByteReadChannel(wrapInJws(samplePayload)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val response = client.createToken(
            trackingId = "23413512fwsd",
            cardNum = "5506900480100006",
            expMonth = "04",
            expYear = "2024",
            cvNum = "698"
        )

        assertTrue(response.isSuccess)
        val data = response.getOrThrow()
        assertEquals("6F3D415A7D26B93A33C31065B2FF912A", data.upcToken)
        assertEquals("550690******0006", data.cardInfo?.mask)
        assertEquals("MAST", data.cardInfo?.brand)
        assertEquals("5167974868281209", data.extDataToken?.tokenNumber)
        assertEquals(true, data.useMCToken)
        assertEquals("000", data.tranCode)
        client.close()
    }

    @Test
    fun testRefreshTokenSuccess() = runTest {
        val samplePayload = """
            {
                "MerchantID": "7651243",
                "TerminalID": "E7651243",
                "fullTrackingId": "23413512fsed",
                "UPCToken": "F79CA27007895919848F76AE440654F3",
                "cardInfo": {
                    "mask": "444111******3749",
                    "expMonth": "07",
                    "expYear": "2024",
                    "brand": "VISA"
                },
                "extDataToken": {
                    "TAVV": "AQAAAAAAR/ECAnsAmTCDgrIAAAA=",
                    "TokenNumber": "4627611592920218",
                    "TokenExpDate": "2407"
                },
                "useVisaToken": true,
                "TranCode": "000"
            }
        """.trimIndent()

        val client = createMockClient({ request ->
            assertEquals("/go/token/refresh", request.url.encodedPath)
            respond(
                content = ByteReadChannel(wrapInJws(samplePayload)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val response = client.refreshToken(
            trackingId = "23413512fsed",
            upcToken = "F79CA27007895919848F76AE440654F3"
        )

        assertTrue(response.isSuccess)
        val data = response.getOrThrow()
        assertEquals("F79CA27007895919848F76AE440654F3", data.upcToken)
        assertEquals("VISA", data.cardInfo?.brand)
        assertEquals(true, data.useVisaToken)
        assertEquals("4627611592920218", data.extDataToken?.tokenNumber)
        client.close()
    }

    @Test
    fun testDeleteTokenSuccess() = runTest {
        val samplePayload = """
            {
                "FullTrackingId": "M1752739TtestDeleteI1664361872375",
                "tokenStatus": {
                    "Code": "000",
                    "TokenStatus": "DEACTIVATED"
                },
                "TranCode": "000",
                "Comment": "Token, trackingId = M1752739TtestDeleteI1664361872375 status = DEACTIVATED"
            }
        """.trimIndent()

        val client = createMockClient({ request ->
            assertEquals("/go/token/delete", request.url.encodedPath)
            respond(
                content = ByteReadChannel(wrapInJws(samplePayload)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val response = client.deleteToken(
            trackingId = "testDelete",
            upcToken = "D0AE1B969DD8D0952376D0B4281AFBCA",
            reasonCode = ReasonCodes.SUSPECTED_FRAUD,
            reason = "Lost/stolen device"
        )

        assertTrue(response.isSuccess)
        val data = response.getOrThrow()
        assertEquals("DEACTIVATED", data.tokenStatus?.tokenStatus)
        assertEquals(TokenStatus.DEACTIVATED, data.tokenStatus?.parsedStatus)
        client.close()
    }

    @Test
    fun testGetTokenStatusSuccess() = runTest {
        val samplePayload = """
            {
                "FullTrackingId": "M7651243TtestGetStatusI1664268340031",
                "tokenStatus": {
                    "Code": "000",
                    "TokenStatus": "ACTIVE"
                },
                "UPCToken": "1185421D698E7655B02F9153ADBD0B7E",
                "TranCode": "000",
                "Comment": "Token, trackingId = M7651243TtestGetStatusI1664268340031 status = ACTIVE"
            }
        """.trimIndent()

        val client = createMockClient({ request ->
            assertEquals("/go/token/status", request.url.encodedPath)
            respond(
                content = ByteReadChannel(wrapInJws(samplePayload)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val response = client.getTokenStatus(
            trackingId = "testGetStatus",
            upcToken = "1185421D698E7655B02F9153ADBD0B7E"
        )

        assertTrue(response.isSuccess)
        val data = response.getOrThrow()
        assertEquals(TokenStatus.ACTIVE, data.currentStatus)
        assertEquals("1185421D698E7655B02F9153ADBD0B7E", data.upcToken)
        client.close()
    }

    @Test
    fun testSuspendTokenSuccess() = runTest {
        val samplePayload = """
            {
                "FullTrackingId": "M7651243TtestSuspendI1664268230671",
                "tokenStatus": {
                    "Code": "000",
                    "TokenStatus": "SUSPENDED"
                },
                "TranCode": "000",
                "Comment": "Token, trackingId = M7651243TtestSuspendI1664268230671 status = SUSPENDED"
            }
        """.trimIndent()

        val client = createMockClient({ request ->
            assertEquals("/go/token/suspend", request.url.encodedPath)
            respond(
                content = ByteReadChannel(wrapInJws(samplePayload)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val response = client.suspendToken(
            trackingId = "testSuspend",
            upcToken = "1185421D698E7655B02F9153ADBD0B7E",
            reasonCode = ReasonCodes.SUSPECTED_FRAUD,
            reason = "test"
        )

        assertTrue(response.isSuccess)
        val data = response.getOrThrow()
        assertEquals(TokenStatus.SUSPENDED, data.tokenStatus?.parsedStatus)
        client.close()
    }

    @Test
    fun testResumeTokenSuccess() = runTest {
        val samplePayload = """
            {
                "FullTrackingId": "M7651243TtestResumeI1664265176662",
                "tokenStatus": {
                    "Code": "000",
                    "TokenStatus": "ACTIVE"
                },
                "TranCode": "000",
                "Comment": "Token, trackingId = M7651243TtestResumeI1664265176662 status = ACTIVE"
            }
        """.trimIndent()

        val client = createMockClient({ request ->
            assertEquals("/go/token/resume", request.url.encodedPath)
            respond(
                content = ByteReadChannel(wrapInJws(samplePayload)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val response = client.resumeToken(
            trackingId = "testResume",
            upcToken = "1185421D698E7655B02F9153ADBD0B7E"
        )

        assertTrue(response.isSuccess)
        val data = response.getOrThrow()
        assertEquals(TokenStatus.ACTIVE, data.tokenStatus?.parsedStatus)
        client.close()
    }

    @Test
    fun testGetCardContentSuccess() = runTest {
        val samplePayload = """
            {
                "cardContent": {
                    "contentId": "c0092844da004f4b9ad4c196ac0e7103",
                    "contentType": "cardSymbol",
                    "mimeType": "image/png",
                    "width": 100,
                    "height": 100,
                    "encodedData": "iVBORw0KGgoAAAANSUhEUgAAAG...",
                    "altText": "cardSymbol"
                },
                "TranCode": "000"
            }
        """.trimIndent()

        val client = createMockClient({ request ->
            assertEquals("/go/card/content", request.url.encodedPath)
            respond(
                content = ByteReadChannel(wrapInJws(samplePayload)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val response = client.getCardContent(
            trackingId = "testContent",
            contentId = "c0092844da004f4b9ad4c196ac0e7103",
            brand = CardBrands.VISA
        )

        assertTrue(response.isSuccess)
        val data = response.getOrThrow()
        assertEquals("c0092844da004f4b9ad4c196ac0e7103", data.cardContent?.contentId)
        assertEquals("cardSymbol", data.cardContent?.contentType)
        assertEquals("image/png", data.cardContent?.mimeType)
        assertEquals(100, data.cardContent?.width)
        assertEquals(100, data.cardContent?.height)
        client.close()
    }

    @Test
    fun testGetCardMetadataSuccess() = runTest {
        val samplePayload = """
            {
                "metaData": {
                    "trackingId": "M7651243TMetadataI1665334777321",
                    "brand": "VISA",
                    "vPanEnrollmentID": "21136e23750f9d0970061193a6b23602",
                    "paymentInstrument": {
                        "last4": 2637,
                        "expirationDate": {
                            "month": "12",
                            "year": "2022"
                        },
                        "cvv2PrintedInd": "Y",
                        "expDatePrintedInd": "Y",
                        "paymentAccountReference": "V0010013022129675550055114826",
                        "enabledServices": {
                            "merchantPresentedQR": "N"
                        }
                    },
                    "tokens": [
                        {
                            "tokenId": "937a524263059f84e2211049591f3202",
                            "tokenStatus": "ACTIVE"
                        }
                    ],
                    "cardMetaData": {
                        "backgroundColor": "0x7aff54",
                        "foregroundColor": "0x7aff54",
                        "labelColor": "0x7aff54",
                        "contactNumber": "1-800-123-4567",
                        "contactName": "Default",
                        "privacyPolicyURL": "https://www.visa.com.sg/legal/privacy-policy.html",
                        "termsAndConditionsURL": "https://www.visa.com.sg/support.html",
                        "shortDescription": "My Test Description",
                        "longDescription": "This is a LONG test description",
                        "cardData": [
                            {
                                "contentId": "c0092844da004f4b9ad4c196ac0e7103",
                                "contentType": "cardSymbol"
                            },
                            {
                                "contentId": "c6d4c5965a0a4b6b983a9187d8bc0c52",
                                "contentType": "digitalCardArt"
                            }
                        ],
                        "issuerFlags": {
                            "deviceBinding": true,
                            "cardholderVerification": true,
                            "trustedBeneficiaryEnrollment": false,
                            "delegatedAuthenticationSupported": true
                        }
                    }
                },
                "TranCode": "000"
            }
        """.trimIndent()

        val client = createMockClient({ request ->
            assertEquals("/go/card/metadata", request.url.encodedPath)
            respond(
                content = ByteReadChannel(wrapInJws(samplePayload)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val response = client.getCardMetadata(
            trackingId = "Metadata",
            upcToken = "1185421D698E7655B02F9153ADBD0B7E"
        )

        assertTrue(response.isSuccess)
        val data = response.getOrThrow()
        assertEquals("VISA", data.metaData?.brand)
        assertEquals(2637, data.metaData?.paymentInstrument?.last4)
        assertEquals("12", data.metaData?.paymentInstrument?.expirationDate?.month)
        assertEquals("2022", data.metaData?.paymentInstrument?.expirationDate?.year)
        assertEquals(1, data.metaData?.tokens?.size)
        assertEquals("ACTIVE", data.metaData?.tokens?.first()?.tokenStatus)
        assertEquals(2, data.metaData?.cardMetaData?.cardData?.size)
        assertEquals(true, data.metaData?.cardMetaData?.issuerFlags?.deviceBinding)
        client.close()
    }

    @Test
    fun testDirectJsonModeWithoutJws() = runTest {
        val samplePayload = """
            {
                "MerchantID": "1750000",
                "TerminalID": "E1750000",
                "fullTrackingId": "23413512fwsd",
                "UPCToken": "6F3D415A7D26B93A33C31065B2FF912A",
                "cardInfo": {
                    "mask": "550690******0006",
                    "expMonth": "04",
                    "expYear": "2024",
                    "brand": "MAST"
                },
                "TranCode": "000"
            }
        """.trimIndent()

        val client = createMockClient({ request ->
            // Direct JSON body sent
            val body = (request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
            assertTrue(body.contains("\"CardNum\":\"5506900480100006\""))
            respond(
                content = ByteReadChannel(samplePayload),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }, useJws = false)

        val response = client.createToken(
            trackingId = "23413512fwsd",
            cardNum = "5506900480100006",
            expMonth = "04",
            expYear = "2024"
        )

        assertTrue(response.isSuccess)
        assertEquals("6F3D415A7D26B93A33C31065B2FF912A", response.getOrThrow().upcToken)
        client.close()
    }

    @Test
    fun testApiErrorNonZeroTranCode() = runTest {
        val samplePayload = """
            {
                "TranCode": "100",
                "Comment": "Card expired or invalid"
            }
        """.trimIndent()

        val client = createMockClient({ _ ->
            respond(
                content = ByteReadChannel(wrapInJws(samplePayload)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val response = client.getTokenStatus(
            trackingId = "test",
            upcToken = "invalid_token"
        )

        assertTrue(response.isError)
        val err = response as UpcResponse.Error
        assertEquals("100", err.tranCode)
        assertEquals("Card expired or invalid", err.comment)
        client.close()
    }

    @Test
    fun testHttpErrorStatusCode() = runTest {
        val client = createMockClient({ _ ->
            respond(
                content = ByteReadChannel("Internal Server Error"),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        })

        val response = client.getTokenStatus(
            trackingId = "test",
            upcToken = "some_token"
        )

        assertTrue(response.isError)
        client.close()
    }

    @Test
    fun testJwsSignatureVerificationFailure() = runTest {
        val samplePayload = "{\"TranCode\":\"000\"}"
        val rejectingVerifier = object : JwsVerifier {
            override fun verify(headerBase64Url: String, payloadBase64Url: String, signatureBase64Url: String) = false
        }

        val client = createMockClient({ _ ->
            respond(
                content = ByteReadChannel(wrapInJws(samplePayload)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }, verifier = rejectingVerifier)

        val response = client.getTokenStatus(
            trackingId = "test",
            upcToken = "some_token"
        )

        assertTrue(response.isError)
        client.close()
    }

    @Test
    fun testUpcResponseFoldAndGetters() {
        val successResponse: UpcResponse<String> = UpcResponse.Success("data_value", "{\"raw\":\"json\"}")
        assertEquals("data_value", successResponse.getOrNull())
        assertEquals("data_value", successResponse.getOrThrow())
        val foldSuccess = successResponse.fold(
            onSuccess = { it.uppercase() },
            onError = { "error" }
        )
        assertEquals("DATA_VALUE", foldSuccess)

        val errorResponse: UpcResponse<String> = UpcResponse.Error(
            exception = ua.upc.ecomm_sdk.tokenservice.error.UpcApiException("Error occurred", tranCode = "101", comment = "Declined"),
            tranCode = "101",
            comment = "Declined"
        )
        assertNull(errorResponse.getOrNull())
        assertFailsWith<ua.upc.ecomm_sdk.tokenservice.error.UpcApiException> {
            errorResponse.getOrThrow()
        }
        val foldError = errorResponse.fold(
            onSuccess = { "success" },
            onError = { "${it.tranCode}:${it.comment}" }
        )
        assertEquals("101:Declined", foldError)
    }

    @Test
    fun testConvenienceFactoryMethods() {
        val client = UpcTokenServiceClient.create(
            merchantId = "1234567",
            terminalId = "E1234567",
            environment = UpcTokenServiceEnvironment.PRODUCTION
        )
        assertEquals("1234567", client.config.merchantId)
        assertEquals("E1234567", client.config.terminalId)
        assertEquals(UpcTokenServiceEnvironment.PRODUCTION, client.config.environment)
        assertEquals("https://secure.upc.ua", client.config.effectiveBaseUrl)
        client.close()
    }
}
