package xyz.mcxross.ksui.exception

class TransactionNotFoundException(digest: String) : SuiException("Transaction not found for digest: $digest")
