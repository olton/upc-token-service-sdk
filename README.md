> This is a Kotlin Multiplatform project targeting Android, Desktop (JVM), iOS, Web, built with
the [Kotlin Toolchain](https://kotlin-toolchain.org/dev/).

### UPC Token Service SDK

SDK для інтеграції з API UPC Token Service (Український процесинговий центр).
Підтримує роботу на всіх платформах Kotlin Multiplatform (JVM/Android/iOS/Wasm).

#### Підтримувані методи API:
1. **Create Token** (`/go/token/create`) — Створення токену на основі реквізитів платіжної картки (для PCI DSS мерчантів).
2. **Refresh Token** (`/go/token/refresh`) — Оновлення динамічної криптограми / даних токену.
3. **Delete Token** (`/go/token/delete`) — Деактивація (видалення) токену.
4. **Get Token Status** (`/go/token/status`) — Отримання поточного статусу токену (ACTIVE, SUSPENDED, DEACTIVATED).
5. **Suspend Token** (`/go/token/suspend`) — Призупинення дії токену.
6. **Resume Token** (`/go/token/resume`) — Відновлення дії призупиненого токену.
7. **Get Card Content** (`/go/card/content`) — Отримання візуальних даних картки (зображення, логотип платіжної системи).
8. **Get Card Metadata** (`/go/card/metadata`) — Отримання метаданих картки та емітента.

#### Приклад використання:

```kotlin
import ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceClient
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceEnvironment
import ua.upc.ecomm_sdk.tokenservice.model.*

// Створення клієнта
val client = UpcTokenServiceClient.create(
    merchantId = "1750000",
    terminalId = "E1750000",
    environment = UpcTokenServiceEnvironment.TEST
)

// 1. Create Token
val createResponse = client.createToken(
    trackingId = "tx-12345",
    cardNum = "5506900480100006",
    expMonth = "04",
    expYear = "2026",
    cvNum = "123"
)
createResponse.fold(
    onSuccess = { data -> println("Token created: ${data.upcToken}") },
    onError = { error -> println("Error: ${error.comment}") }
)

// 2. Get Token Status
val statusResponse = client.getTokenStatus(
    trackingId = "tx-12346",
    upcToken = "6F3D415A7D26B93A33C31065B2FF912A"
)

// 3. Suspend Token
val suspendResponse = client.suspendToken(
    trackingId = "tx-12347",
    upcToken = "6F3D415A7D26B93A33C31065B2FF912A",
    reasonCode = ReasonCodes.SUSPECTED_FRAUD,
    reason = "User requested suspension"
)

// 4. Resume Token
val resumeResponse = client.resumeToken(
    trackingId = "tx-12348",
    upcToken = "6F3D415A7D26B93A33C31065B2FF912A"
)

// 5. Refresh Token
val refreshResponse = client.refreshToken(
    trackingId = "tx-12349",
    upcToken = "6F3D415A7D26B93A33C31065B2FF912A"
)

// 6. Delete Token
val deleteResponse = client.deleteToken(
    trackingId = "tx-12350",
    upcToken = "6F3D415A7D26B93A33C31065B2FF912A",
    reasonCode = ReasonCodes.CARDHOLDER_CONFIRMED,
    reason = "Token removed by user"
)

// 7. Get Card Content
val contentResponse = client.getCardContent(
    trackingId = "tx-12351",
    contentId = "c0092844da004f4b9ad4c196ac0e7103",
    brand = CardBrands.VISA
)

// 8. Get Card Metadata
val metadataResponse = client.getCardMetadata(
    trackingId = "tx-12352",
    upcToken = "6F3D415A7D26B93A33C31065B2FF912A"
)
```

---

- [/androidApp](./androidApp) contains the Android application.
- [/desktopApp](./desktopApp) contains the desktop (JVM) application.
- [/iosApp](./iosApp) contains the iOS application.
- [/shared](./shared) holds the code shared across your applications — Compose UI, business logic, and platform-specific
  implementations. [src](./shared/src) is for common code; the sibling `src@<platform>` folders (for example
  `src@android`) hold code compiled only for the platform named in the folder.
- [/webApp](./webApp) contains the web application, compiled to WebAssembly with Kotlin/Wasm.

The `kotlin` (macOS/Linux) and `kotlin.bat` (Windows) scripts in the project root are self-bootstrapping wrappers for
the Kotlin Toolchain: they download the pinned toolchain version on first use, so no separate installation is required.
Build the whole project with `./kotlin build`.

### Running

Use the run configurations provided by the run widget in your IDE's toolbar. You can also run each module from the
command line:

- Android app: `./kotlin run -m androidApp`
- Desktop app: `./kotlin run -m desktopApp`
- iOS app: `./kotlin run -m iosApp`
- Web app: `./kotlin run -m webApp`

### Testing

Run the project's tests with `./kotlin test`, or `./kotlin test -m <module>` for a single module.

---

Learn more
about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html), [Kotlin Toolchain](https://kotlin-toolchain.org/dev/), [Compose Multiplatform](https://kotlinlang.org/compose-multiplatform/), [Kotlin/Wasm](https://kotl.in/wasm/).
