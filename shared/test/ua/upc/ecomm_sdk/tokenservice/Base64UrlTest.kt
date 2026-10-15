package ua.upc.ecomm_sdk.tokenservice

import ua.upc.ecomm_sdk.tokenservice.security.Base64Url
import kotlin.test.Test
import kotlin.test.assertEquals

class Base64UrlTest {

    @Test
    fun testBase64UrlEncodingAndDecoding() {
        val testStrings = listOf(
            "",
            "f",
            "fo",
            "foo",
            "foob",
            "fooba",
            "foobar",
            "Hello, World!",
            "{\"MerchantID\":\"1750000\",\"TerminalID\":\"E1750000\"}",
            "Special chars: ?&%=+/"
        )

        for (s in testStrings) {
            val encoded = Base64Url.encode(s)
            // Must not contain standard base64 '+', '/', or '='
            assertEquals(false, encoded.contains("+"))
            assertEquals(false, encoded.contains("/"))
            assertEquals(false, encoded.contains("="))

            val decoded = Base64Url.decodeToString(encoded)
            assertEquals(s, decoded)
        }
    }

    @Test
    fun testKnownJwsPayloadDecoding() {
        val payloadB64 = "ewogICAgIk1lcmNoYW50SUQiOiAiMTc1MDAwMCIsCiAgICAiVGVybWluYWxJRCI6ICJFMTc1MDAwMCIsCiAgICAiVHJhY2tpbmdJZCI6ICIyMzQxMzUxMmZ3c2QiLAogICAgImNhcmQiOiB7CiAgICAgICAgIkNhcmROdW0iOiAiNTUwNjkwMDQ4MDEwMDAwNiIsCiAgICAgICAgImV4cE1vbnRoIjogIjA0IiwKICAgICAgICAiZXhwWWVhciI6ICIyMDI0IgogICAgfQp9"
        val decoded = Base64Url.decodeToString(payloadB64)
        assertEquals(true, decoded.contains("\"MerchantID\": \"1750000\""))
        assertEquals(true, decoded.contains("\"CardNum\": \"5506900480100006\""))
    }
}
