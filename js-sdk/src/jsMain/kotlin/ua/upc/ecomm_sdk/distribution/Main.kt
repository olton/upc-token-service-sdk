@file:OptIn(ExperimentalJsExport::class)

package ua.upc.ecomm_sdk.distribution

import ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceJsClient

@JsExport
fun createUpcTokenServiceClient(
    merchantId: String,
    terminalId: String,
    environment: String = "TEST",
    baseUrl: String? = null
): UpcTokenServiceJsClient = UpcTokenServiceJsClient.create(
    merchantId = merchantId,
    terminalId = terminalId,
    environment = environment,
    baseUrl = baseUrl
)

fun main() = Unit