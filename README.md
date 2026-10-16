# UPC Token Service SDK

Єдиний кросплатформний Kotlin Multiplatform (KMP) SDK (`shared`) для взаємодії з API UPC Token Service (Український процесинговий центр).

Модуль `shared` виступає **єдиним джерелом істини (Single Source of Truth)** для всієї бізнес-логіки, протоколів безпеки (JWS), серіалізації моделей та клієнтів для всіх платформ:
- **Kotlin** (Multiplatform / Android / JVM / iOS / JS) — нативні корутини (`suspend` функції).
- **Java** (Java 8, 11, 17, 21+, Spring Boot) — через вбудований у `shared` адаптер `UpcTokenServiceJavaClient` (`CompletableFuture`).
- **JavaScript (Web / Browser)** — через вбудований у `shared` адаптер `UpcTokenServiceJsClient` (JavaScript `Promise`).
- **NodeJS** (Backend) — через вбудований у `shared` адаптер `UpcTokenServiceJsClient` (`Promise` / `async/await`).
- **Swift / iOS** (iOS, macOS) — через скомпільований з `shared` нативний фреймворк (`shared.framework` / Swift Concurrency `async/await`).

---

## Підтримувані методи API (всі 8 операцій)
1. **Create Token** (`/go/token/create`) — Створення токену на основі реквізитів платіжної картки (для PCI DSS мерчантів).
2. **Refresh Token** (`/go/token/refresh`) — Оновлення динамічної криптограми / даних токену.
3. **Delete Token** (`/go/token/delete`) — Деактивація (видалення) токену.
4. **Get Token Status** (`/go/token/status`) — Отримання поточного статусу токену (ACTIVE, SUSPENDED, DEACTIVATED).
5. **Suspend Token** (`/go/token/suspend`) — Призупинення дії токену.
6. **Resume Token** (`/go/token/resume`) — Відновлення дії призупиненого токену.
7. **Get Card Content** (`/go/card/content`) — Отримання візуальних даних картки (зображення, логотип платіжної системи).
8. **Get Card Metadata** (`/go/card/metadata`) — Отримання метаданих картки та емітента.

---

## Архітектура: KMP модуль `shared` як єдине джерело

```
                              ┌────────────────────────────────────────┐
                              │      shared (KMP Core Module)          │
                              │  - JWS Signer / Verifier               │
                              │  - Data Models & Serialization         │
                              │  - UpcTokenServiceClient (Ktor Engine) │
                              └──────────────────┬─────────────────────┘
                                                 │
          ┌──────────────────────────┬───────────┴───────────────┬──────────────────────────┐
          ▼                          ▼                           ▼                          ▼
   [Kotlin Client]            [Java Interop]              [JS / Node Interop]        [Swift / iOS]
UpcTokenServiceClient    UpcTokenServiceJavaClient    UpcTokenServiceJsClient       shared.framework
  (Coroutines / KMP)       (CompletableFuture)               (Promise)             (Swift async/await)
          │                          │                           │                          │
          ▼                          ▼                           ▼                          ▼
   samples/kotlin              samples/java             samples/js-web & nodejs        samples/swift
```

---

## Демонстраційні додатки (Samples & Demos)

- 📱 [**Kotlin Demo** (`samples/kotlin`)](./samples/kotlin) — Multiplatform / Android / JVM додаток з корутинами.
- ☕ [**Java Demo** (`samples/java`)](./samples/java) — Чистий Java-проєкт з використанням `CompletableFuture`.
- 🌐 [**JavaScript Web Demo** (`samples/js-web`)](./samples/js-web) — Інтерактивний веб-додаток у браузері (`index.html`).
- 🟢 [**NodeJS Demo** (`samples/nodejs`)](./samples/nodejs) — Бекенд-додаток на базі `Promise` / `async/await`.
- 🍎 [**Swift Demo** (`samples/swift`)](./samples/swift) — Нативний Swift додаток (iOS / macOS) на базі Swift Concurrency.

---

## Приклади використання за технологіями

### 1. Kotlin (Multiplatform / Android / JVM)

```kotlin
import ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceClient
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceEnvironment

val client = UpcTokenServiceClient.create(
    merchantId = "1750000",
    terminalId = "E1750000",
    environment = UpcTokenServiceEnvironment.TEST
)

val response = client.createToken(
    trackingId = "tx-1001",
    cardNum = "5506900480100006",
    expMonth = "04",
    expYear = "2026",
    cvNum = "123"
)

response.fold(
    onSuccess = { data -> println("Token: ${data.upcToken}") },
    onError = { err -> println("Error: ${err.comment}") }
)
```

### 2. Java (Java 8, 11, 17, 21+ / Spring Boot)

```java
import ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceJavaClient;
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceEnvironment;

try (UpcTokenServiceJavaClient client = UpcTokenServiceJavaClient.create(
        "1750000",
        "E1750000",
        UpcTokenServiceEnvironment.TEST
)) {
    client.createToken("tx-1001", "5506900480100006", "04", "2026", "123")
          .thenAccept(response -> {
              if (response.isSuccess()) {
                  System.out.println("Token: " + response.getOrNull().getUpcToken());
              }
          })
          .join();
}
```

### 3. JavaScript (Web / Browser)

```javascript
import { UpcTokenServiceJsClient } from './app.js';

// Створення клієнта з єдиного KMP ядра
const client = UpcTokenServiceJsClient.create(
    "1750000",
    "E1750000",
    "TEST"
);

const response = await client.createToken(
    "tx-web-1001",
    "5506900480100006",
    "04",
    "2026",
    "123"
);

if (response.isSuccess) {
    console.log("Token:", response.getOrNull().upcToken);
}
```

### 4. NodeJS (Backend)

```javascript
const { UpcTokenServiceJsClient } = require('./index.js');

// Створення клієнта з єдиного KMP ядра
const client = UpcTokenServiceJsClient.create(
    "1750000",
    "E1750000",
    "TEST"
);

const response = await client.createToken(
    "tx-node-101",
    "5506900480100006",
    "04",
    "2026",
    "123"
);

console.log("Response:", response);
```

### 5. Swift (iOS / macOS)

```swift
import shared // KMP Framework

let client = UpcTokenServiceClient.companion.create(
    merchantId: "1750000",
    terminalId: "E1750000",
    environment: UpcTokenServiceEnvironment.test
)

Task {
    let response = try await client.createToken(
        trackingId: "tx-swift-101",
        cardNum: "5506900480100006",
        expMonth: "04",
        expYear: "2026",
        cvNum: "123"
    )
    if response.isSuccess {
        print("Token: \(response.getOrNull()?.upcToken ?? "")")
    }
}
```

---

## Збірка, створення білдів та бандлів

У проєкті використовується інструмент збірки Kotlin Multiplatform (Amper CLI `kotlin` / `kotlin.bat`).

### 1. Команди компіляції та збірки

- **Повна збірка для всіх платформ:**
  ```bash
  # Linux / macOS
  ./kotlin build

  # Windows (PowerShell / CMD)
  .\kotlin.bat build
  ```

- **Таргетована збірка під конкретну платформу:**
  ```bash
  # Тільки JVM (JAR бібліотека для Java / Kotlin backend)
  .\kotlin.bat build -p jvm

  # Тільки Android (AAR бібліотека для Android додатків)
  .\kotlin.bat build -p android

  # Тільки JavaScript KLib для Kotlin/JS споживачів
  .\kotlin.bat build -p js

  # Тільки iOS (Apple Framework для iOS / macOS / Swift)
  .\kotlin.bat build -p iosArm64 -p iosSimulatorArm64
  ```

- **Запуск модульних та інтеграційних тестів:**
  ```bash
  .\kotlin.bat test -p jvm -p android
  ```

---

### 2. Використання зібраних артефактів

#### 🌐 JavaScript / Web та Node.js
Команда `kotlin.bat build -p js` компілює `shared` у Kotlin/JS `.klib`. Для готового executable та браузерного бандла використовується окремий Gradle linking-проєкт `js-sdk`, який посилається безпосередньо на `shared/src` і `shared/src@js` (дубльованої реалізації SDK немає). Потрібен JDK 17+; доданий Gradle Wrapper сам завантажить Gradle 8.14.3.

З кореня репозиторію виконайте через доданий Gradle Wrapper (він використовує Gradle 8.14.3):
```powershell
.\js-sdk\gradlew.bat -p js-sdk jsBrowserProductionWebpack
.\js-sdk\gradlew.bat -p js-sdk compileProductionExecutableKotlinJs
```

Браузерний артефакт створюється в `js-sdk/build/distributions/ecomm-sdk.js`; Node.js executable генерується Kotlin/JS Node target у `js-sdk/build/`. Подробиці та доступна експортована функція описані в `js-sdk/README.md`.

Файл `samples/js-web/ecomm-sdk.js` не є результатом компіляції KMP-модуля і не повинен поширюватися як офіційний SDK-артефакт.

Для Kotlin/JS споживачів доступний результат `.klib` у `build/tasks/_shared_compileJs/shared.klib`.

#### ☕ JVM / Android артефакти (JAR / AAR)
- Після виконання `.\kotlin.bat build` згенеровані артефакти розміщуються в каталозі `build/tasks/`:
  - `_shared_jarJvm/shared.jar` — JAR бібліотека для Java / Kotlin серверних та десктопних додатків.
  - `_shared_aarAndroidDebug/shared.aar` — AAR бібліотека для Android.
- Підключаються як залежності в `build.gradle.kts` або Maven `pom.xml`.

#### 🍎 Apple / iOS Framework (`shared.framework`)
- Компілюється за допомогою таргета `iosArm64` / `iosSimulatorArm64`.
- Підключається у Xcode проєкт як Framework або Swift Package для використання у Swift через `import shared`.
