package dev.mikita.issueservice.exception;

/**
 * Signifies that invalid data have been provided to the application.
 */
public class ValidationException extends BaseException {
    /**
     * Instantiates a new Validation exception.
     *
     * @param message the message
     */
    public ValidationException(String message) {
        super(message);
    }
}
