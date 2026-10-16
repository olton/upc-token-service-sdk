# UPC Token Service SDK - Kotlin Example

Демонстраційний приклад використання SDK в проєктах на Kotlin (Multiplatform, Android, JVM, Backend).

## Підключення
Додайте залежність до вашого `build.gradle.kts` або `module.yaml`:
```kotlin
dependencies {
    implementation("ua.upc.ecomm_sdk:tokenservice:1.0.0")
}
```

## Швидкий старт

```kotlin
import ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceClient
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceEnvironment

// Створення клієнта
val client = UpcTokenServiceClient.create(
    merchantId = "1750000",
    terminalId = "E1750000",
    environment = UpcTokenServiceEnvironment.TEST
)

// Виклик методу (у suspend функції або корутині)
val response = client.createToken(
    trackingId = "tx-1001",
    cardNum = "5506900480100006",
    expMonth = "04",
    expYear = "2026",
    cvNum = "123"
)

response.fold(
    onSuccess = { data -> println("UPC Token: ${data.upcToken}") },
    onError = { err -> println("Помилка: ${err.comment}") }
)
```
