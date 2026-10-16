.\js-sdk\gradlew.bat -p js-sdk jsBrowserProductionWebpack
.\js-sdk\gradlew.bat -p js-sdk compileProductionExecutableKotlinJs
Copy-Item .\js-sdk\build\kotlin-webpack\js\productionExecutable\ecomm-sdk.js .\samples\js-web\ecomm-sdk.js
Copy-Item .\js-sdk\build\kotlin-webpack\js\productionExecutable\ecomm-sdk.js.map .\samples\js-web\ecomm-sdk.js.map