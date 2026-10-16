# UPC Token Service SDK - NodeJS Backend Example

Приклад інтеграції UPC Token Service SDK для бекенд-серверів на NodeJS (Express, Fastify, NestJS, Koa, AWS Lambda) з використанням KMP `shared` клієнта (`UpcTokenServiceJsClient`).

## Запуск демо
1. Перейдіть до папки `samples/nodejs`:
   ```bash
   cd samples/nodejs
   ```
2. Запустіть скрипт:
   ```bash
   node index.js
   ```

## Приклад коду

```javascript
const { UpcTokenServiceJsClient } = require('ecomm-sdk');

// Створення екземпляра клієнта з єдиного KMP ядра
const client = UpcTokenServiceJsClient.create(
    "1750000",
    "E1750000",
    "TEST"
);

async function main() {
    const tokenResponse = await client.createToken(
        "tx-node-101",
        "5506900480100006",
        "04",
        "2026",
        "123"
    );

    if (tokenResponse.isSuccess) {
        console.log("Token Response:", tokenResponse.getOrNull());
    }
}

main();
```
