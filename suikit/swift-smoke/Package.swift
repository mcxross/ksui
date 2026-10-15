// swift-tools-version:5.9
import PackageDescription

// Checks SuiKit from Swift against ./gradlew :suikit:assembleSuiKitReleaseXCFramework.
// Once Package.swift points at a release, run with SUIKIT_LOCAL_BUILD=1 to test the local build.
let package = Package(
  name: "SuiKitSmoke",
  platforms: [.iOS(.v15), .macOS(.v12)],
  products: [.library(name: "SuiKitSmoke", targets: ["SuiKitSmoke"])],
  dependencies: [.package(path: "../..")],
  targets: [
    .target(name: "SuiKitSmoke", dependencies: [.product(name: "SuiKit", package: "ksui")]),
    .testTarget(name: "SuiKitSmokeTests", dependencies: ["SuiKitSmoke"]),
  ]
)
