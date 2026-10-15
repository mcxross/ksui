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

  func testClientFailuresBecomeSwiftErrors() async throws {
    let settings = SuiSettings(
      network: Network.custom, fullNode: "http://127.0.0.1:9/graphql", faucet: nil, indexer: nil,
      clientConfig: ClientConfig.companion.default_, fullNodeConfig: FullNodeConfig(headers: nil),
      indexerConfig: IndexerConfig(headers: nil),
      faucetConfig: FaucetConfig(headers: nil, authToken: nil))
    let sui = Sui(config: SuiConfig(settings: settings), makeDefault: false)
    let signer = try Account.companion.create(scheme: SignatureScheme.ed25519)
    let options = ExecuteTransactionBlockResponseOptions(
      showBalanceChanges: false, showEffects: false, showRawEffects: false, showEvents: false,
      showInput: false, showObjectChanges: false, showRawInput: false)
    do {
      _ = try await sui.signAndExecuteTransactionBlock(
        signer: signer, ptb: try ProgrammableTransactionBuilder().build(), gasBudget: 1_000_000,
        options: options)
      XCTFail("Executing against an unreachable node should fail")
    } catch {}
  }

  func testInvalidPrivateKeyThrows() {
    XCTAssertThrowsError(
      try Account.companion.import(str: "not a private key", scheme: SignatureScheme.ed25519))
  }

  func testVersionIsExported() {
    XCTAssertFalse(SuiKitInfo.shared.VERSION.isEmpty)
  }
}
