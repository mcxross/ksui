package xyz.mcxross.ksui.exception

import xyz.mcxross.ksui.model.EventFilter

/**
 * Custom exception to be thrown when the [EventFilter] type is not recognized.
 *
 * @param message A detailed message explaining the reason for the exception.
 */
class UnknownEventFilterException(message: String) : Exception(message)
