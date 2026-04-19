package xyz.mcxross.ksui.core.exception

class SigningException : RuntimeException {
  constructor(cause: Throwable?) : super(cause)

  constructor(message: String?) : super(message)
}
