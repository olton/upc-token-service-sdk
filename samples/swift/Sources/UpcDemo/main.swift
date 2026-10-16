import Foundation

/**
 * UPC Token Service SDK - Swift Demo Application
 *
 * Demonstrates native Swift (iOS / macOS) integration with async/await.
 */

// Models mimicking the KMP Swift Framework exports
public struct CardData {
    public let cardNum: String
    public let expMonth: String
    public let expYear: String
    public let cvNum: String?
}

public struct CreateTokenResponse {
    public let upcToken: String?
    public let tranCode: String?
    public let comment: String?
    public let maskedPan: String?
}

public struct TokenStatusResponse {
    public let status: String
    public let expDate: String
}

public enum UpcResponse<T> {
    case success(T)
    case error(code: String, message: String)
}

public final class UpcTokenServiceSwiftClient {
    public let merchantId: String
    public let terminalId: String
    public let baseUrl: String

    public init(merchantId: String, terminalId: String, baseUrl: String = "https://ecg.test.upc.ua") {
        self.merchantId = merchantId
        self.terminalId = terminalId
        self.baseUrl = baseUrl
    }

    public func createToken(
        trackingId: String,
        cardNum: String,
        expMonth: String,
        expYear: String,
        cvNum: String? = nil
    ) async throws -> UpcResponse<CreateTokenResponse> {
        print("[Swift SDK] Creating token for trackingId: \(trackingId)...")
        
        // Simulating async network call via UPC Token Service API
        try await Task.sleep(nanoseconds: 100_000_000)
        
        let masked = "\(cardNum.prefix(6))******\(cardNum.suffix(4))"
        let response = CreateTokenResponse(
            upcToken: "6F3D415A7D26B93A33C31065B2FF912A",
            tranCode: "000",
            comment: "Approved",
            maskedPan: masked
        )
        return .success(response)
    }

    public func getTokenStatus(
        trackingId: String,
        upcToken: String
    ) async throws -> UpcResponse<TokenStatusResponse> {
        print("[Swift SDK] Querying token status for \(upcToken)...")
        try await Task.sleep(nanoseconds: 50_000_000)
        return .success(TokenStatusResponse(status: "ACTIVE", expDate: "2028-12-31"))
    }

    public func suspendToken(
        trackingId: String,
        upcToken: String,
        reasonCode: String = "11",
        reason: String? = "Customer lock"
    ) async throws -> UpcResponse<String> {
        print("[Swift SDK] Suspending token \(upcToken)...")
        try await Task.sleep(nanoseconds: 50_000_000)
        return .success("Token suspended successfully")
    }

    public func resumeToken(
        trackingId: String,
        upcToken: String
    ) async throws -> UpcResponse<String> {
        print("[Swift SDK] Resuming token \(upcToken)...")
        try await Task.sleep(nanoseconds: 50_000_000)
        return .success("Token resumed successfully")
    }

    public func refreshToken(
        trackingId: String,
        upcToken: String
    ) async throws -> UpcResponse<String> {
        print("[Swift SDK] Refreshing token cryptogram for \(upcToken)...")
        try await Task.sleep(nanoseconds: 50_000_000)
        return .success("Token refreshed successfully")
    }

    public func deleteToken(
        trackingId: String,
        upcToken: String,
        reasonCode: String = "10",
        reason: String? = "User removed card"
    ) async throws -> UpcResponse<String> {
        print("[Swift SDK] Deleting token \(upcToken)...")
        try await Task.sleep(nanoseconds: 50_000_000)
        return .success("Token deactivated successfully")
    }
}

// Swift Execution Runner
@main
struct AppRunner {
    static func main() async {
        print("=== UPC Token Service SDK - Swift Demo ===")

        let client = UpcTokenServiceSwiftClient(
            merchantId: "1750000",
            terminalId: "E1750000"
        )

        let trackingId = "tx-swift-\(Int(Date().timeIntervalSince1970))"

        do {
            // 1. Create Token
            print("\n[1] Create Token:")
            let createRes = try await client.createToken(
                trackingId: "\(trackingId)-create",
                cardNum: "5506900480100006",
                expMonth: "04",
                expYear: "2026",
                cvNum: "123"
            )

            var activeToken = "6F3D415A7D26B93A33C31065B2FF912A"
            switch createRes {
            case .success(let data):
                activeToken = data.upcToken ?? activeToken
                print("✓ Token Created: \(activeToken)")
                print("  Masked PAN: \(data.maskedPan ?? "")")
            case .error(let code, let msg):
                print("✗ Create failed [\(code)]: \(msg)")
            }

            // 2. Get Status
            print("\n[2] Get Token Status:")
            let statusRes = try await client.getTokenStatus(
                trackingId: "\(trackingId)-status",
                upcToken: activeToken
            )
            if case .success(let status) = statusRes {
                print("✓ Status: \(status.status) (Expires: \(status.expDate))")
            }

            // 3. Suspend Token
            print("\n[3] Suspend Token:")
            _ = try await client.suspendToken(
                trackingId: "\(trackingId)-suspend",
                upcToken: activeToken
            )
            print("✓ Token Suspended.")

            // 4. Resume Token
            print("\n[4] Resume Token:")
            _ = try await client.resumeToken(
                trackingId: "\(trackingId)-resume",
                upcToken: activeToken
            )
            print("✓ Token Resumed.")

            // 5. Refresh Token
            print("\n[5] Refresh Token:")
            _ = try await client.refreshToken(
                trackingId: "\(trackingId)-refresh",
                upcToken: activeToken
            )
            print("✓ Token Refreshed.")

            // 6. Delete Token
            print("\n[6] Delete Token:")
            _ = try await client.deleteToken(
                trackingId: "\(trackingId)-delete",
                upcToken: activeToken
            )
            print("✓ Token Deactivated.")

            print("\n=== Swift Demo Completed Successfully ===")
        } catch {
            print("Error executing Swift demo: \(error)")
        }
    }
}
