// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "UpcTokenServiceSwiftDemo",
    platforms: [
        .macOS(.v13),
        .iOS(.v16)
    ],
    products: [
        .executable(name: "UpcDemo", targets: ["UpcDemo"])
    ],
    targets: [
        .executableTarget(
            name: "UpcDemo",
            path: "Sources/UpcDemo"
        )
    ]
)
