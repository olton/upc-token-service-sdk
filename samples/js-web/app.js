/**
 * UPC Token Service SDK - JavaScript / Web Client Demo Application
 *
 * Demonstrates how to use the UPC Token Service SDK (UpcTokenServiceJsClient)
 * in a web browser with JavaScript Promises and async/await.
 */

// Resolves UpcTokenServiceJsClient strictly from the loaded SDK bundle (ecomm-sdk.js)
const getSdkClientClass = () => {
    if (typeof UpcTokenServiceJsClient !== 'undefined') {
        return UpcTokenServiceJsClient;
    }
    const globalSdk = (typeof window !== 'undefined' ? (window['ecomm-sdk-js'] || window['ecomm-sdk'] || window['shared'] || window.UpcSdk) : null);
    if (globalSdk?.ua?.upc?.ecomm_sdk?.tokenservice?.client?.UpcTokenServiceJsClient) {
        return globalSdk.ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceJsClient;
    }
    if (globalSdk?.UpcTokenServiceJsClient) {
        return globalSdk.UpcTokenServiceJsClient;
    }
    try {
        if (typeof require === 'function') {
            const mod = require('ecomm-sdk');
            return mod.UpcTokenServiceJsClient || mod.ua?.upc?.ecomm_sdk?.tokenservice?.client?.UpcTokenServiceJsClient;
        }
    } catch (e) {
        // Ignored in browser
    }
    return null;
};

/**
 * Creates an SDK client instance for the given merchant credentials.
 */
function createClient(merchantId, terminalId, baseUrl = null) {
    const SdkClass = getSdkClientClass();
    if (!SdkClass) {
        const errorMsg = 'UPC Token Service SDK (UpcTokenServiceJsClient) не знайдено. Переконайтеся, що файл ecomm-sdk.js зібрано та підключено через <script src="ecomm-sdk.js"></script>.';
        log(`[SDK ERROR] ${errorMsg}`);
        throw new Error(errorMsg);
    }
    const companion = SdkClass['Companion'];
    const create = SdkClass.create || companion?.create;
    if (typeof create !== 'function') {
        throw new Error('The loaded UPC Token Service SDK does not expose a client factory.');
    }
    return create.call(companion || SdkClass, merchantId, terminalId, 'TEST', baseUrl);
}

// UI Handlers & State
function getClient() {
    const merchantId = document.getElementById('merchantId').value.trim();
    const terminalId = document.getElementById('terminalId').value.trim();
    const baseUrlInput = document.getElementById('baseUrl')?.value?.trim();
    return createClient(merchantId, terminalId, baseUrlInput || null);
}

// Auto-fill Base URL on load
if (typeof document !== 'undefined') {
    const initBaseUrl = () => {
        const baseUrlEl = document.getElementById('baseUrl');
        if (baseUrlEl && !baseUrlEl.value) {
            if (typeof window !== 'undefined' && window.location?.protocol?.startsWith('http')) {
                baseUrlEl.value = window.location.origin;
            } else {
                baseUrlEl.value = 'http://localhost:3000';
            }
        }
    };
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initBaseUrl);
    } else {
        initBaseUrl();
    }
}

function log(text) {
    const el = document.getElementById('outputLog');
    el.textContent = `${new Date().toLocaleTimeString()} - ${text}\n\n` + el.textContent;
}

function updateStatus(status) {
    const badge = document.getElementById('statusBadge');
    badge.textContent = status;
    badge.className = 'status-badge ' + status.toLowerCase();
}

function normalizeResponseForLog(value) {
    if (Array.isArray(value)) {
        return value.map(normalizeResponseForLog);
    }
    if (value && typeof value === 'object') {
        return Object.fromEntries(Object.entries(value).map(([key, nestedValue]) => [
            key.replace(/_1$/, ''),
            normalizeResponseForLog(nestedValue)
        ]));
    }
    return value;
}

function getSdkProperty(value, property) {
    if (!value || typeof value !== 'object') {
        return undefined;
    }
    return value[property] ?? value[`${property}_1`];
}

/**
 * 1. Create Token via SDK (client.createToken)
 */
async function onCreateToken() {
    const client = getClient();
    const cardNum = document.getElementById('cardNum').value.trim();
    const expMonth = document.getElementById('expMonth').value.trim();
    const expYear = document.getElementById('expYear').value.trim();
    const cvNum = document.getElementById('cvNum').value.trim();
    const trackingId = 'tx-web-' + Date.now();

    log(`[SDK CALL] client.createToken(trackingId: "${trackingId}", cardNum: "${cardNum.slice(0, 6)}...${cardNum.slice(-4)}")`);

    try {
        const response = await client.createToken(
            trackingId,
            cardNum,
            expMonth,
            expYear,
            cvNum || null
        );

        log(`[SDK RESPONSE]\n` + JSON.stringify(normalizeResponseForLog(response), null, 2));

        const data = response?.getOrNull ? response.getOrNull() : response;
        const exception = getSdkProperty(response, 'exception');
        const tranCode = getSdkProperty(data, 'tranCode') || getSdkProperty(data, 'TranCode') ||
            getSdkProperty(response, 'tranCode') || getSdkProperty(response, 'TranCode') ||
            getSdkProperty(exception, 'tranCode') || getSdkProperty(exception, 'TranCode');
        const comment = getSdkProperty(data, 'comment') || getSdkProperty(data, 'Comment') ||
            getSdkProperty(response, 'comment') || getSdkProperty(response, 'Comment') ||
            getSdkProperty(exception, 'comment') || getSdkProperty(exception, 'Comment');
        if (!data && !tranCode && !comment && typeof response?.getOrThrow === 'function') {
            try {
                response.getOrThrow();
            } catch (error) {
                const errMsg = error?.message || String(error);
                updateStatus('ERROR: ' + errMsg);
                log(`[SDK ERROR] Create token failed: ${errMsg}`);
                return;
            }
        }
        const token = getSdkProperty(data, 'upcToken') || getSdkProperty(data, 'UPCToken');
        if (token && (tranCode === '000' || !tranCode)) {
            document.getElementById('activeToken').value = token;
            updateStatus('ACTIVE');
            log(`[SDK SUCCESS] Token created successfully: ${token}`);
        } else {
            const errMsg = comment || `Error (TranCode: ${tranCode || 'unknown'})`;
            updateStatus('ERROR: ' + errMsg);
            log(`[SDK ERROR] Create token failed: ${errMsg} (TranCode: ${tranCode || 'N/A'})`);
        }
    } catch (err) {
        log(`[SDK ERROR] Exception: ${err.message}`);
    }
}

/**
 * 2. Get Token Status via SDK (client.getTokenStatus)
 */
async function onGetStatus() {
    const client = getClient();
    const upcToken = document.getElementById('activeToken').value.trim();
    const trackingId = 'tx-web-' + Date.now();

    log(`[SDK CALL] client.getTokenStatus(trackingId: "${trackingId}", upcToken: "${upcToken}")`);

    try {
        const response = await client.getTokenStatus(trackingId, upcToken);
        log(`[SDK RESPONSE]\n` + JSON.stringify(normalizeResponseForLog(response), null, 2));

        const data = response?.getOrNull ? response.getOrNull() : response;
        const status = data?.tokenStatus?.status || data?.TokenStatus?.Status;
        if (status) {
            updateStatus(status);
            log(`[SDK SUCCESS] Token status: ${status}`);
        }
    } catch (err) {
        log(`[SDK ERROR] Exception: ${err.message}`);
    }
}

/**
 * 3. Suspend Token via SDK (client.suspendToken)
 */
async function onSuspendToken() {
    const client = getClient();
    const upcToken = document.getElementById('activeToken').value.trim();
    const trackingId = 'tx-web-' + Date.now();

    log(`[SDK CALL] client.suspendToken(trackingId: "${trackingId}", upcToken: "${upcToken}")`);

    try {
        const response = await client.suspendToken(trackingId, upcToken, '11', 'Customer lock via web UI');
        log(`[SDK RESPONSE]\n` + JSON.stringify(normalizeResponseForLog(response), null, 2));

        const data = response?.getOrNull ? response.getOrNull() : response;
        if (response?.isSuccess || data?.tranCode === '000' || data?.TranCode === '000') {
            updateStatus('SUSPENDED');
            log(`[SDK SUCCESS] Token suspended successfully.`);
        }
    } catch (err) {
        log(`[SDK ERROR] Exception: ${err.message}`);
    }
}

/**
 * 4. Resume Token via SDK (client.resumeToken)
 */
async function onResumeToken() {
    const client = getClient();
    const upcToken = document.getElementById('activeToken').value.trim();
    const trackingId = 'tx-web-' + Date.now();

    log(`[SDK CALL] client.resumeToken(trackingId: "${trackingId}", upcToken: "${upcToken}")`);

    try {
        const response = await client.resumeToken(trackingId, upcToken, null, null);
        log(`[SDK RESPONSE]\n` + JSON.stringify(normalizeResponseForLog(response), null, 2));

        const data = response?.getOrNull ? response.getOrNull() : response;
        if (response?.isSuccess || data?.tranCode === '000' || data?.TranCode === '000') {
            updateStatus('ACTIVE');
            log(`[SDK SUCCESS] Token resumed successfully.`);
        }
    } catch (err) {
        log(`[SDK ERROR] Exception: ${err.message}`);
    }
}

/**
 * 5. Refresh Token via SDK (client.refreshToken)
 */
async function onRefreshToken() {
    const client = getClient();
    const upcToken = document.getElementById('activeToken').value.trim();
    const trackingId = 'tx-web-' + Date.now();

    log(`[SDK CALL] client.refreshToken(trackingId: "${trackingId}", upcToken: "${upcToken}")`);

    try {
        const response = await client.refreshToken(trackingId, upcToken);
        log(`[SDK RESPONSE]\n` + JSON.stringify(normalizeResponseForLog(response), null, 2));
    } catch (err) {
        log(`[SDK ERROR] Exception: ${err.message}`);
    }
}

/**
 * 6. Get Card Metadata via SDK (client.getCardMetadata)
 */
async function onGetMetadata() {
    const client = getClient();
    const upcToken = document.getElementById('activeToken').value.trim();
    const trackingId = 'tx-web-' + Date.now();

    log(`[SDK CALL] client.getCardMetadata(trackingId: "${trackingId}", upcToken: "${upcToken}")`);

    try {
        const response = await client.getCardMetadata(trackingId, upcToken);
        log(`[SDK RESPONSE]\n` + JSON.stringify(normalizeResponseForLog(response), null, 2));
    } catch (err) {
        log(`[SDK ERROR] Exception: ${err.message}`);
    }
}

/**
 * 7. Delete Token via SDK (client.deleteToken)
 */
async function onDeleteToken() {
    const client = getClient();
    const upcToken = document.getElementById('activeToken').value.trim();
    const trackingId = 'tx-web-' + Date.now();

    log(`[SDK CALL] client.deleteToken(trackingId: "${trackingId}", upcToken: "${upcToken}")`);

    try {
        const response = await client.deleteToken(trackingId, upcToken, '10', 'Deleted by user in web demo');
        log(`[SDK RESPONSE]\n` + JSON.stringify(normalizeResponseForLog(response), null, 2));

        const data = response?.getOrNull ? response.getOrNull() : response;
        if (response?.isSuccess || data?.tranCode === '000' || data?.TranCode === '000') {
            updateStatus('DEACTIVATED');
            log(`[SDK SUCCESS] Token deleted (deactivated) successfully.`);
        }
    } catch (err) {
        log(`[SDK ERROR] Exception: ${err.message}`);
    }
}
