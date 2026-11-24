package dev.slne.surf.social.api.chat.message

/**
 * Whether a message passed validation, see [MessageValidationError] for the reasons it can fail.
 */
sealed class MessageValidationResult {
    /** The message passed validation. */
    class Success : MessageValidationResult()

    /** The message failed validation because of [error]. */
    data class Failure(val error: MessageValidationError) : MessageValidationResult()

    fun isFailure(): Boolean = this is Failure

    /**
     * @throws IllegalStateException if this is not a [Failure]
     */
    fun getErrorOrThrow(): MessageValidationError =
        (this as? Failure)?.error ?: error("No error found.")
}
