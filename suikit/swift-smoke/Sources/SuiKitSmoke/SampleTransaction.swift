import Foundation
import SuiKit

/// Builds the transaction that ksui's `TransactionBytesTest` pins, and returns its kind bytes.
public func sampleTransactionKindBytes() throws -> Data {
  let builder = ProgrammableTransactionBuilder()
  // Pure vector<u64>[1, 2].
  let values: [UInt8] = [2, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0]
  let input = builder.pure(bytes: kotlinBytes(values))
  _ = try builder.moveCall(
    target: "0x42::counter::batch_set", typeArgs: [], args: [Argument.GasCoin.shared, input])
  return data(try builder.build().toTransactionKindBcs())
}

func kotlinBytes(_ bytes: [UInt8]) -> KotlinByteArray {
  let array = KotlinByteArray(size: Int32(bytes.count))
  for (index, byte) in bytes.enumerated() {
    array.set(index: Int32(index), value: Int8(bitPattern: byte))
  }
  return array
}

func data(_ bytes: KotlinByteArray) -> Data {
  // `Data` and `Bool` must still mean Foundation's and Swift's types in a file importing SuiKit.
  let empty: Bool = bytes.size == 0
  return empty ? Data() : Data((0..<bytes.size).map { UInt8(bitPattern: bytes.get(index: $0)) })
}
