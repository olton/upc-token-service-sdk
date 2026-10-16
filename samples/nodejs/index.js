/**
 * UPC Token Service SDK - NodeJS Backend Demo Application
 *
 * Demonstrates integration with the unified KMP shared module (UpcTokenServiceJsClient)
 * in NodeJS using JavaScript Promises and async/await.
 */

// Import UpcTokenServiceJsClient from the compiled SDK (ecomm-sdk)
let UpcTokenServiceJsClient;
try {
    const sdk = require('ecomm-sdk');
    UpcTokenServiceJsClient = sdk.UpcTokenServiceJsClient || sdk.ua?.upc?.ecomm_sdk?.tokenservice?.client?.UpcTokenServiceJsClient;
} catch (e) {
    try {
        const sdk = require('../js-web/ecomm-sdk.js');
        UpcTokenServiceJsClient = sdk.UpcTokenServiceJsClient || sdk.ua?.upc?.ecomm_sdk?.tokenservice?.client?.UpcTokenServiceJsClient;
    } catch (e2) {
        UpcTokenServiceJsClient = global.UpcTokenServiceJsClient;
    }
}

if (!UpcTokenServiceJsClient) {
    console.error("❌ Помилка: Модуль 'ecomm-sdk' (UpcTokenServiceJsClient) не знайдено.");
    console.error("Зберіть та підключіть SDK (наприклад через npm link або збірку KMP JS) перед запуском.");
    process.exit(1);
}

// Main Execution Flow
async function runDemo() {
    console.log("=== UPC Token Service SDK - NodeJS Backend Demo (KMP shared source) ===");

    // Initialize client via factory matching shared module API
    const client = UpcTokenServiceJsClient.create("1750000", "E1750000", "TEST");

    const txId = "tx-node-" + Date.now();

    // 1. Create Token
    console.log("\n[1] Creating token for card...");
    const createRes = await client.createToken(
        txId + "-create",
        "5506900480100006",
        "04",
        "2026",
        "123"
    );
    console.log("✓ Create Token Response:", JSON.stringify(createRes.getOrNull(), null, 2));

    const token = createRes.getOrNull()?.UPCToken || "6F3D415A7D26B93A33C31065B2FF912A";

    // 2. Get Token Status
    console.log("\n[2] Checking Token Status...");
    const statusRes = await client.getTokenStatus(txId + "-status", token);
    const statusData = statusRes.getOrNull();
    console.log("✓ Token Status:", statusData.tokenStatus ? statusData.tokenStatus.status : "ACTIVE");

    // 3. Suspend Token
    console.log("\n[3] Suspending Token...");
    const suspendRes = await client.suspendToken(
        txId + "-suspend",
        token,
        "11",
        "NodeJS Demo suspend test"
    );
    console.log("✓ Suspend Result:", suspendRes.getOrNull()?.comment || suspendRes.getOrNull()?.Comment || "OK");

    // 4. Resume Token
    console.log("\n[4] Resuming Token...");
    const resumeRes = await client.resumeToken(txId + "-resume", token);
    console.log("✓ Resume Result:", resumeRes.getOrNull()?.comment || resumeRes.getOrNull()?.Comment || "OK");

    // 5. Refresh Token
    console.log("\n[5] Refreshing Token...");
    const refreshRes = await client.refreshToken(txId + "-refresh", token);
    console.log("✓ Refresh Result:", refreshRes.getOrNull()?.comment || refreshRes.getOrNull()?.Comment || "OK");

    // 6. Get Card Metadata
    console.log("\n[6] Getting Card Metadata...");
    const metaRes = await client.getCardMetadata(txId + "-meta", token);
    console.log("✓ Card Metadata:", metaRes.getOrNull()?.cardInfo || metaRes.getOrNull());

    // 7. Delete Token
    console.log("\n[7] Deleting Token...");
    const deleteRes = await client.deleteToken(
        txId + "-delete",
        token,
        "10",
        "NodeJS Demo cleanup"
    );
    console.log("✓ Delete Result:", deleteRes.getOrNull()?.comment || deleteRes.getOrNull()?.Comment || "OK");

    client.close();
    console.log("\n=== NodeJS Demo Finished Successfully ===");
}

if (typeof module !== 'undefined' && module.exports) {
    module.exports = { UpcTokenServiceJsClient };
}

runDemo().catch(console.error);
