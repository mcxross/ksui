package xyz.mcxross.ksui.sample


class SigningException : RuntimeException {
 /**
 * Instantiates a new Signing exception.
 * 
 * @param cause the cause
 */
    constructor(cause: Throwable?) : super(cause)
    
    constructor(message: String?) : super(message)
}
