package ua.upc.ecomm_sdk.tokenservice.security

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Cross-platform Base64Url encoder and decoder (RFC 4648 §5).
 */
@OptIn(ExperimentalEncodingApi::class)
object Base64Url {

    fun encode(data: ByteArray): String =
        Base64.UrlSafe.encode(data).trimEnd('=')

    fun encode(string: String): String =
        encode(string.encodeToByteArray())

    fun decode(string: String): ByteArray {
        val clean = string.trim().replace("\r", "").replace("\n", "").trimEnd('=')
        val pad = (4 - clean.length % 4) % 4
        val padded = clean + "=".repeat(pad)
        return Base64.UrlSafe.decode(padded)
    }

    fun decodeToString(string: String): String =
        decode(string).decodeToString()
}
