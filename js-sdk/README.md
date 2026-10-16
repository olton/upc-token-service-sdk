# Kotlin/JS distribution

This standalone Gradle build links the Kotlin/JS executable from the sources in `../shared`; it does not contain a second implementation of the SDK.

## Build

Install JDK 17+, then run from the repository root using the included Gradle Wrapper (Gradle 8.14.3):

```powershell
.\js-sdk\gradlew.bat -p js-sdk jsBrowserProductionWebpack
.\js-sdk\gradlew.bat -p js-sdk compileProductionExecutableKotlinJs
```

The browser bundle is generated at `js-sdk/build/distributions/ecomm-sdk.js`. The Node.js executable is generated in `js-sdk/build/` by the Kotlin/JS Node target. These are distinct distribution targets; Node.js consumers should use the generated Node executable rather than loading the browser bundle.

`createUpcTokenServiceClient(merchantId, terminalId, environment, baseUrl)` is exported from the linked module and returns the shared `UpcTokenServiceJsClient`, whose API methods return JavaScript `Promise`s.

The browser bundle calls the UPC gateway from the browser. For gateways that do not permit CORS, configure `baseUrl` to the local proxy origin, for example `http://localhost:3000` when running `node samples/js-web/proxy.js`.