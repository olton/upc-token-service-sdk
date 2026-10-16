package samples.kotlin

import kotlinx.coroutines.runBlocking
import ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceClient
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceEnvironment
import ua.upc.ecomm_sdk.tokenservice.model.*

/**
 * UPC Token Service SDK - Kotlin Demo Application
 *
 * Demonstrates how to use all 8 API operations in Kotlin with Coroutines.
 */
fun main() = runBlocking {
    println("=== UPC Token Service SDK - Kotlin Demo ===")

    // Initialize SDK Client
    val client = UpcTokenServiceClient.create(
        merchantId = "1750000",
        terminalId = "E1750000",
        environment = UpcTokenServiceEnvironment.TEST
    )

    val trackingIdBase = "tx-${System.currentTimeMillis()}"

    // 1. Create Token
    println("\n[1] Creating token for card...")
    val createResponse = client.createToken(
        trackingId = "$trackingIdBase-create",
        cardNum = "5506900480100006",
        expMonth = "04",
        expYear = "2026",
        cvNum = "123"
    )

    createResponse.fold(
        onSuccess = { resp ->
            println("✓ Token created successfully!")
            println("  - UPC Token: ${resp.upcToken}")
            println("  - TranCode: ${resp.tranCode}")
            println("  - Masked PAN: ${resp.cardInfo?.maskedPan}")
            println("  - Brand: ${resp.cardInfo?.brand}")
        },
        onError = { error ->
            println("✗ Failed to create token: ${error.comment} (TranCode: ${error.tranCode})")
        }
    )

    val upcToken = createResponse.getOrNull()?.upcToken ?: "6F3D415A7D26B93A33C31065B2FF912A"

    // 2. Get Token Status
    println("\n[2] Getting token status...")
    val statusResponse = client.getTokenStatus(
        trackingId = "$trackingIdBase-status",
        upcToken = upcToken
    )

    statusResponse.fold(
        onSuccess = { resp ->
            println("✓ Status: ${resp.tokenStatus?.status ?: "ACTIVE"}")
            println("  - Expiration Date: ${resp.tokenStatus?.tokenExpDate}")
        },
        onError = { error ->
            println("✗ Status check failed: ${error.comment}")
        }
    )

    // 3. Suspend Token
    println("\n[3] Suspending token...")
    val suspendResponse = client.suspendToken(
        trackingId = "$trackingIdBase-suspend",
        upcToken = upcToken,
        reasonCode = ReasonCodes.SUSPECTED_FRAUD,
        reason = "Temporarily locked by customer"
    )

    suspendResponse.fold(
        onSuccess = { println("✓ Token suspended successfully.") },
        onError = { error -> println("✗ Suspend failed: ${error.comment}") }
    )

    // 4. Resume Token
    println("\n[4] Resuming token...")
    val resumeResponse = client.resumeToken(
        trackingId = "$trackingIdBase-resume",
        upcToken = upcToken
    )

    resumeResponse.fold(
        onSuccess = { println("✓ Token resumed successfully.") },
        onError = { error -> println("✗ Resume failed: ${error.comment}") }
    )

    // 5. Refresh Token
    println("\n[5] Refreshing token cryptogram...")
    val refreshResponse = client.refreshToken(
        trackingId = "$trackingIdBase-refresh",
        upcToken = upcToken
    )

    refreshResponse.fold(
        onSuccess = { resp ->
            println("✓ Token refreshed successfully.")
            println("  - New Expiration Date: ${resp.tokenExpDate}")
        },
        onError = { error -> println("✗ Refresh failed: ${error.comment}") }
    )

    // 6. Get Card Metadata
    println("\n[6] Getting card metadata...")
    val metadataResponse = client.getCardMetadata(
        trackingId = "$trackingIdBase-meta",
        upcToken = upcToken
    )

    metadataResponse.fold(
        onSuccess = { resp ->
            println("✓ Card Metadata retrieved:")
            println("  - Brand: ${resp.cardInfo?.brand}")
            println("  - Cardholder Name: ${resp.cardInfo?.cardholderName}")
            println("  - Issuer: ${resp.issuerInfo?.bankName}")
        },
        onError = { error -> println("✗ Metadata query failed: ${error.comment}") }
    )

    // 7. Get Card Content (Visual Assets)
    println("\n[7] Getting card visual content...")
    val contentResponse = client.getCardContent(
        trackingId = "$trackingIdBase-content",
        contentId = "c0092844da004f4b9ad4c196ac0e7103",
        brand = CardBrands.VISA
    )

    contentResponse.fold(
        onSuccess = { resp ->
            println("✓ Card visual content retrieved:")
            println("  - Content Type: ${resp.contentType}")
            println("  - Image size (bytes): ${resp.data?.length ?: 0}")
        },
        onError = { error -> println("✗ Content query failed: ${error.comment}") }
    )

    // 8. Delete Token
    println("\n[8] Deleting token...")
    val deleteResponse = client.deleteToken(
        trackingId = "$trackingIdBase-delete",
        upcToken = upcToken,
        reasonCode = ReasonCodes.CARDHOLDER_CONFIRMED,
        reason = "Deleted on customer request"
    )

    deleteResponse.fold(
        onSuccess = { println("✓ Token deleted (deactivated) successfully.") },
        onError = { error -> println("✗ Delete failed: ${error.comment}") }
    )

    client.close()
    println("\n=== Kotlin Demo Completed ===")
}
