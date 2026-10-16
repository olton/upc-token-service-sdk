# UPC Token Service SDK - Swift (iOS / macOS) Example

Демонстраційний приклад інтеграції UPC Token Service SDK у проєктах на Swift (iOS, iPadOS, macOS, watchOS, visionOS) з використанням Swift Concurrency (`async/await`).

## Інтеграція Kotlin Multiplatform фреймворку в iOS/macOS
Kotlin SDK збирається у нативний XCFramework для Apple платформ:
```bash
./kotlin build
```
Після додавання `ecomm_sdk.xcframework` у ваш Xcode проєкт, всі методи доступні як нативні `async throws` функції Swift.

## Швидкий старт на Swift

```swift
import Foundation
// import UpcTokenServiceSDK

let client = UpcTokenServiceSwiftClient(
    merchantId: "1750000",
    terminalId: "E1750000"
)

// Виклик методу у Task / async контексті
Task {
    do {
        let response = try await client.createToken(
            trackingId: "tx-swift-101",
            cardNum: "5506900480100006",
            expMonth: "04",
            expYear: "2026",
            cvNum: "123"
        )
        
        if case .success(let data) = response {
            print("Token Created: \(data.upcToken ?? "")")
        }
    } catch {
        print("Error: \(error)")
    }
}
```

## Запуск демонстраційного додатку
Запустіть за допомогою Swift Package Manager:
```bash
swift run
```
