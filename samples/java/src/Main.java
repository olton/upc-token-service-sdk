package samples.java;

import ua.upc.ecomm_sdk.tokenservice.client.UpcResponse;
import ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceJavaClient;
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceEnvironment;
import ua.upc.ecomm_sdk.tokenservice.model.*;

import java.util.concurrent.CompletableFuture;

/**
 * UPC Token Service SDK - Java Demo Application
 *
 * Demonstrates standard Java integration using CompletableFuture and UpcTokenServiceJavaClient.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== UPC Token Service SDK - Java Demo ===");

        // 1. Initialize Java-friendly client
        try (UpcTokenServiceJavaClient client = UpcTokenServiceJavaClient.create(
                "1750000",
                "E1750000",
                UpcTokenServiceEnvironment.TEST
        )) {
            String trackingId = "tx-java-" + System.currentTimeMillis();

            // 2. Create Token asynchronously with CompletableFuture
            System.out.println("\n[1] Creating Token...");
            CompletableFuture<UpcResponse<CreateTokenResponse>> createFuture = client.createToken(
                    trackingId + "-create",
                    "5506900480100006",
                    "04",
                    "2026",
                    "123"
            );

            UpcResponse<CreateTokenResponse> createResponse = createFuture.join();
            String upcToken = "6F3D415A7D26B93A33C31065B2FF912A";

            if (createResponse.isSuccess()) {
                CreateTokenResponse data = createResponse.getOrNull();
                System.out.println("✓ Token created successfully!");
                if (data != null && data.getUpcToken() != null) {
                    upcToken = data.getUpcToken();
                    System.out.println("  - UPC Token: " + upcToken);
                    System.out.println("  - TranCode: " + data.getTranCode());
                }
            } else {
                System.out.println("✗ Create Token failed: " + createResponse.getErrorOrNull());
            }

            // 3. Get Token Status
            System.out.println("\n[2] Checking Token Status...");
            UpcResponse<GetTokenStatusResponse> statusResponse = client.getTokenStatus(
                    trackingId + "-status",
                    upcToken
            ).join();

            if (statusResponse.isSuccess()) {
                GetTokenStatusResponse data = statusResponse.getOrNull();
                System.out.println("✓ Token Status: " + (data != null && data.getTokenStatus() != null ? data.getTokenStatus().getStatus() : "ACTIVE"));
            }

            // 4. Suspend Token
            System.out.println("\n[3] Suspending Token...");
            UpcResponse<SuspendTokenResponse> suspendResponse = client.suspendToken(
                    trackingId + "-suspend",
                    upcToken,
                    ReasonCodes.SUSPECTED_FRAUD,
                    "Temporarily suspended by Java backend"
            ).join();
            System.out.println("✓ Suspend result: " + (suspendResponse.isSuccess() ? "SUCCESS" : "ERROR"));

            // 5. Resume Token
            System.out.println("\n[4] Resuming Token...");
            UpcResponse<ResumeTokenResponse> resumeResponse = client.resumeToken(
                    trackingId + "-resume",
                    upcToken,
                    null,
                    null
            ).join();
            System.out.println("✓ Resume result: " + (resumeResponse.isSuccess() ? "SUCCESS" : "ERROR"));

            // 6. Refresh Token
            System.out.println("\n[5] Refreshing Token...");
            UpcResponse<RefreshTokenResponse> refreshResponse = client.refreshToken(
                    trackingId + "-refresh",
                    upcToken
            ).join();
            System.out.println("✓ Refresh result: " + (refreshResponse.isSuccess() ? "SUCCESS" : "ERROR"));

            // 7. Get Card Metadata
            System.out.println("\n[6] Getting Card Metadata...");
            UpcResponse<GetCardMetadataResponse> metaResponse = client.getCardMetadata(
                    trackingId + "-meta",
                    upcToken
            ).join();
            if (metaResponse.isSuccess() && metaResponse.getOrNull() != null) {
                System.out.println("✓ Card Brand: " + (metaResponse.getOrNull().getCardInfo() != null ? metaResponse.getOrNull().getCardInfo().getBrand() : "VISA"));
            }

            // 8. Delete Token
            System.out.println("\n[7] Deleting Token...");
            UpcResponse<DeleteTokenResponse> deleteResponse = client.deleteToken(
                    trackingId + "-delete",
                    upcToken,
                    ReasonCodes.CARDHOLDER_CONFIRMED,
                    "User removed card"
            ).join();
            System.out.println("✓ Delete result: " + (deleteResponse.isSuccess() ? "SUCCESS" : "ERROR"));

            System.out.println("\n=== Java Demo Completed ===");
        }
    }
}
