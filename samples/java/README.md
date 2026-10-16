# UPC Token Service SDK - Java Example

Демонстраційний приклад інтеграції SDK у чистих Java-проєктах (Java 8, 11, 17, 21+ / Spring Boot / Quarkus / Jakarta).

## Особливості для Java
SDK надає спеціальний адаптер `UpcTokenServiceJavaClient`, який повертає стандартні Java `CompletableFuture<UpcResponse<T>>`. Це дозволяє використовувати SDK у синхронному або асинхронному стилі без необхідності підключати корутини Kotlin у Java-коді.

## Швидкий старт

```java
import ua.upc.ecomm_sdk.tokenservice.client.UpcResponse;
import ua.upc.ecomm_sdk.tokenservice.client.UpcTokenServiceJavaClient;
import ua.upc.ecomm_sdk.tokenservice.config.UpcTokenServiceEnvironment;
import ua.upc.ecomm_sdk.tokenservice.model.CreateTokenResponse;

// 1. Створення клієнта
try (UpcTokenServiceJavaClient client = UpcTokenServiceJavaClient.create(
        "1750000",
        "E1750000",
        UpcTokenServiceEnvironment.TEST
)) {
    // 2. Асинхронний виклик (CompletableFuture)
    client.createToken("tx-1001", "5506900480100006", "04", "2026", "123")
          .thenAccept(response -> {
              if (response.isSuccess()) {
                  CreateTokenResponse data = response.getOrNull();
                  System.out.println("UPC Token: " + data.getUpcToken());
              } else {
                  System.err.println("Error: " + response.getErrorOrNull().getComment());
              }
          })
          .join();
}
```
