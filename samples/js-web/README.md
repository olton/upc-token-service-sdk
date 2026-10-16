# UPC Token Service SDK - JavaScript (Web) Demo

Інтерактивний демонстраційний додаток для використання SDK у веб-браузері (JavaScript, HTML5, React, Vue, Angular) на основі єдиного KMP клієнта `UpcTokenServiceJsClient`.

## Запуск демо у браузері

Для уникнення блокування браузерного CORS при зверненні до платіжного шлюзу рекомендується запускати демо через локальний проксі-сервер:

1. **Запуск локального dev/proxy сервера:**
   ```bash
   node samples/js-web/proxy.js
   ```
2. **Відкрийте сторінку в браузері:**
   Перейдіть за адресою [http://localhost:3000](http://localhost:3000).

3. **Збірка SDK-бандла:** з кореня репозиторію виконайте ` .\js-sdk\gradlew.bat -p js-sdk jsBrowserProductionWebpack`. Gradle Wrapper завантажить потрібний Gradle, якщо його ще немає. Бандл, скомпільований із джерел `shared`, з'явиться у `js-sdk/build/kotlin-webpack/js/productionExecutable/ecomm-sdk.js`. Для підключення його до демо скопіюйте артефакт у `samples/js-web/ecomm-sdk.js`; не використовуйте однойменний source-файл як зібраний SDK.

4. **Використання дій життєвого циклу токену:**
   - **Створити токен (Create Token)** — токенізація платіжної картки.
   - **Статус (Get Status)** — перевірка активності токену.
   - **Призупинити (Suspend)** — тимчасове блокування.
   - **Відновити (Resume)** — повторна активація.
   - **Оновити (Refresh)** — генерація нової динамічної криптограми.
   - **Метадані (Metadata)** — отримання даних картки та емітента.
   - **Видалити (Delete)** — деактивація токену.

## Використання у JavaScript / TypeScript проєктах

```javascript
import { UpcTokenServiceJsClient } from 'ecomm-sdk';

// Створення клієнта з KMP SDK ядра
const client = UpcTokenServiceJsClient.create(
    "1750000",
    "E1750000",
    "TEST"
);

// Створення токену
const response = await client.createToken(
    "tx-web-1001",
    "5506900480100006",
    "04",
    "2026",
    "123"
);

if (response.isSuccess) {
    const data = response.getOrNull();
    console.log('UPC Token:', data.upcToken);
}
```
