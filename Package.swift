// swift-tools-version:5.9
import PackageDescription

// Set by `./gradlew :suikit:writeSuiKitReleaseManifest` when the Release SuiKit workflow publishes.
let release: (url: String, checksum: String)? = (url: "https://github.com/mcxross/ksui/releases/download/v3.2.0-SNAPSHOT.145/SuiKit.xcframework.zip", checksum: "a54b312efaff90d5e128053578f36049a909e22b9c1fb5911060592a0d4b4c92")

// SUIKIT_LOCAL_BUILD, or an unreleased checkout, uses `./gradlew :suikit:assembleSuiKitReleaseXCFramework`.
let suiKit: Target =
  if let release, Context.environment["SUIKIT_LOCAL_BUILD"] == nil {
    .binaryTarget(name: "SuiKit", url: release.url, checksum: release.checksum)
  } else {
    .binaryTarget(name: "SuiKit", path: "suikit/build/XCFrameworks/release/SuiKit.xcframework")
  }

let package = Package(
  name: "ksui",
  platforms: [.iOS(.v15), .macOS(.v12)],
  products: [.library(name: "SuiKit", targets: ["SuiKit"])],
  targets: [suiKit]
)
