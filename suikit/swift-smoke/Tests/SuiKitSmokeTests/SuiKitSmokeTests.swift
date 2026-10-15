import Foundation
import SuiKit
import XCTest

@testable import SuiKitSmoke

final class SuiKitSmokeTests: XCTestCase {
  private let sampleTransactionKindHex =
    "00" + "01" + "00" + "11" + "02" + "0100000000000000" + "0200000000000000" + "01" + "00"
    + String(repeating: "00", count: 31) + "42" + "07" + "636f756e746572" + "09"
    + "62617463685f736574" + "00" + "02" + "00" + "010000"

  func testTransactionKindBytesMatchKotlin() throws {
    let hex = try sampleTransactionKindBytes().map { String(format: "%02x", $0) }.joined()
    XCTAssertEqual(hex, sampleTransactionKindHex)
  }

  func testInvalidMoveCallTargetThrows() {
    let builder = ProgrammableTransactionBuilder()
    XCTAssertThrowsError(try builder.moveCall(target: "not-a-target", typeArgs: [], args: []))
  }

  func testUnresolvedObjectInputThrowsOnBuild() {
    let builder = ProgrammableTransactionBuilder()
    _ = builder.object(id: "0x123")
    XCTAssertThrowsError(try builder.build())
  }

  func testVersionIsExported() {
    XCTAssertFalse(SuiKitInfo.shared.VERSION.isEmpty)
  }
}
