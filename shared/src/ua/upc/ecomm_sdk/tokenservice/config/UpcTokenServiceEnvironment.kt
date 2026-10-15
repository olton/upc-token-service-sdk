package ua.upc.ecomm_sdk.tokenservice.config

/**
 * Environments for UPC Token Service.
 */
enum class UpcTokenServiceEnvironment(val baseUrl: String) {
    /**
     * Production environment: https://secure.upc.ua
     */
    PRODUCTION("https://secure.upc.ua"),

    /**
     * Test / Sandbox environment: https://ecg.test.upc.ua
     */
    TEST("https://ecg.test.upc.ua")
}
