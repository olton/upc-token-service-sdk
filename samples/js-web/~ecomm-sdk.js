(function (global, factory) {
    if (typeof exports === 'object' && typeof module !== 'undefined') {
        module.exports = factory();
    } else if (typeof define === 'function' && define.amd) {
        define([], factory);
    } else {
        const root = typeof globalThis !== 'undefined' ? globalThis : global || self;
        const exports = factory();
        root.UpcTokenServiceJsClient = exports.UpcTokenServiceJsClient;
        root['ecomm-sdk'] = exports;
        if (!root.ua) root.ua = {};
        if (!root.ua.upc) root.ua.upc = {};
        if (!root.ua.upc.ecomm_sdk) root.ua.upc.ecomm_sdk = {};
        if (!root.ua.upc.ecomm_sdk.tokenservice) root.ua.upc.ecomm_sdk.tokenservice = {};
        if (!root.ua.upc.ecomm_sdk.tokenservice.client) root.ua.upc.ecomm_sdk.tokenservice.client = {};
        root.ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceJsClient = exports.UpcTokenServiceJsClient;
    }
}(this, function () {
    'use strict';

    /**
     * UPC Token Service Environments
     */
    const UpcTokenServiceEnvironment = {
        PRODUCTION: 'https://secure.upc.ua',
        TEST: 'https://ecg.test.upc.ua'
    };

    /**
     * Wraps API responses into a typed UpcResponse structure
     */
    function createUpcResponse(data, httpStatus = 200, error = null) {
        const tranCode = data?.tranCode || data?.TranCode;
        const isSuccess = !error && httpStatus >= 200 && httpStatus < 300 && (!tranCode || tranCode === '000');
        const comment = data?.comment || data?.Comment || (error ? error.message : '');

        const responseObj = {
            isSuccess,
            isError: !isSuccess,
            getOrNull: () => data || null,
            exceptionOrNull: () => (isSuccess ? null : (error || new Error(comment || `Error TranCode: ${tranCode || httpStatus}`))),
            tranCode: tranCode || (error ? 'ERR' : String(httpStatus)),
            comment: comment,
            ...(data || {})
        };

        return responseObj;
    }

    /**
     * JavaScript / NodeJS SDK Client for UPC Token Service
     */
    class UpcTokenServiceJsClient {
        constructor(config = {}) {
            this.merchantId = config.merchantId;
            this.terminalId = config.terminalId;
            this.environment = config.environment || 'TEST';
            this.customBaseUrl = config.customBaseUrl || null;
            this.useJwsEnvelope = config.useJwsEnvelope !== false;

            if (this.customBaseUrl) {
                this.baseUrl = this.customBaseUrl.replace(/\/+$/, '');
            } else if (this.environment && this.environment.toUpperCase().startsWith('PROD')) {
                this.baseUrl = UpcTokenServiceEnvironment.PRODUCTION;
            } else {
                this.baseUrl = UpcTokenServiceEnvironment.TEST;
            }
        }

        static create(merchantId, terminalId, environment = 'TEST', baseUrl = null, useJwsEnvelope = true) {
            return new UpcTokenServiceJsClient({
                merchantId,
                terminalId,
                environment,
                customBaseUrl: baseUrl,
                useJwsEnvelope
            });
        }

        async _executeRequest(endpoint, payload) {
            const url = `${this.baseUrl}${endpoint}`;
            const bodyStr = JSON.stringify(payload);

            try {
                if (typeof fetch === 'function') {
                    const response = await fetch(url, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Accept': 'application/json'
                        },
                        body: bodyStr
                    });

                    const text = await response.text();
                    let data;
                    try {
                        data = JSON.parse(text);
                    } catch (e) {
                        data = { tranCode: String(response.status), comment: text };
                    }

                    return createUpcResponse(data, response.status);
                } else {
                    // NodeJS https / http fallback
                    return new Promise((resolve) => {
                        const parsedUrl = new URL(url);
                        const isHttps = parsedUrl.protocol === 'https:';
                        const httpModule = isHttps ? require('https') : require('http');

                        const options = {
                            hostname: parsedUrl.hostname,
                            port: parsedUrl.port || (isHttps ? 443 : 80),
                            path: parsedUrl.pathname + parsedUrl.search,
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                                'Accept': 'application/json',
                                'Content-Length': Buffer.byteLength(bodyStr)
                            }
                        };

                        const req = httpModule.request(options, (res) => {
                            let responseBody = '';
                            res.on('data', chunk => { responseBody += chunk; });
                            res.on('end', () => {
                                let data;
                                try {
                                    data = JSON.parse(responseBody);
                                } catch (e) {
                                    data = { tranCode: String(res.statusCode), comment: responseBody };
                                }
                                resolve(createUpcResponse(data, res.statusCode));
                            });
                        });

                        req.on('error', (err) => {
                            resolve(createUpcResponse({
                                tranCode: 'ERR',
                                comment: err.message
                            }, 500, err));
                        });

                        req.write(bodyStr);
                        req.end();
                    });
                }
            } catch (err) {
                return createUpcResponse({
                    tranCode: 'ERR',
                    comment: err.message
                }, 500, err);
            }
        }

        /**
         * 1. Create Token
         */
        async createToken(trackingId, cardNum, expMonth, expYear, cvNum = null) {
            return this._executeRequest('/go/token/create', {
                MerchantID: this.merchantId,
                TerminalID: this.terminalId,
                TrackingId: trackingId,
                card: {
                    CardNum: cardNum,
                    ExpMonth: expMonth,
                    ExpYear: expYear,
                    CVNum: cvNum || null
                }
            });
        }

        /**
         * 2. Refresh Token
         */
        async refreshToken(trackingId, upcToken) {
            return this._executeRequest('/go/token/refresh', {
                MerchantID: this.merchantId,
                TerminalID: this.terminalId,
                TrackingId: trackingId,
                UPCToken: upcToken
            });
        }

        /**
         * 3. Delete Token
         */
        async deleteToken(trackingId, upcToken, reasonCode = null, reason = null) {
            const payload = {
                MerchantID: this.merchantId,
                TerminalID: this.terminalId,
                TrackingId: trackingId,
                UPCToken: upcToken
            };
            if (reasonCode) payload.reasonCode = reasonCode;
            if (reason) payload.reason = reason;
            return this._executeRequest('/go/token/delete', payload);
        }

        /**
         * 4. Get Token Status
         */
        async getTokenStatus(trackingId, upcToken) {
            return this._executeRequest('/go/token/status', {
                MerchantID: this.merchantId,
                TerminalID: this.terminalId,
                TrackingId: trackingId,
                UPCToken: upcToken
            });
        }

        /**
         * 5. Suspend Token
         */
        async suspendToken(trackingId, upcToken, reasonCode = null, reason = null) {
            const payload = {
                MerchantID: this.merchantId,
                TerminalID: this.terminalId,
                TrackingId: trackingId,
                UPCToken: upcToken
            };
            if (reasonCode) payload.reasonCode = reasonCode;
            if (reason) payload.reason = reason;
            return this._executeRequest('/go/token/suspend', payload);
        }

        /**
         * 6. Resume Token
         */
        async resumeToken(trackingId, upcToken, reasonCode = null, reason = null) {
            const payload = {
                MerchantID: this.merchantId,
                TerminalID: this.terminalId,
                TrackingId: trackingId,
                UPCToken: upcToken
            };
            if (reasonCode) payload.reasonCode = reasonCode;
            if (reason) payload.reason = reason;
            return this._executeRequest('/go/token/resume', payload);
        }

        /**
         * 7. Get Card Content
         */
        async getCardContent(trackingId, contentId, brand = 'VISA') {
            return this._executeRequest('/go/card/content', {
                MerchantID: this.merchantId,
                TerminalID: this.terminalId,
                TrackingId: trackingId,
                contentId: contentId,
                brand: brand
            });
        }

        /**
         * 8. Get Card Metadata
         */
        async getCardMetadata(trackingId, upcToken) {
            return this._executeRequest('/go/card/metadata', {
                MerchantID: this.merchantId,
                TerminalID: this.terminalId,
                TrackingId: trackingId,
                UPCToken: upcToken
            });
        }

        close() {
            // Cleanup resources if needed
        }
    }

    return {
        UpcTokenServiceJsClient,
        UpcTokenServiceEnvironment
    };
}));
