// swift-tools-version:5.9
import PackageDescription

// Checks SuiKit from Swift. Build the package first: ./gradlew :suikit:assembleSuiKitSwiftPackage
let package = Package(
  name: "SuiKitSmoke",
  platforms: [.iOS(.v15), .macOS(.v12)],
  products: [.library(name: "SuiKitSmoke", targets: ["SuiKitSmoke"])],
  dependencies: [.package(path: "../build/swiftpm/SuiKit")],
  targets: [
    .target(name: "SuiKitSmoke", dependencies: [.product(name: "SuiKit", package: "SuiKit")]),
    .testTarget(name: "SuiKitSmokeTests", dependencies: ["SuiKitSmoke"]),
  ]
)
