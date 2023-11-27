package dev.mikita.issueservice.exception;

/**
 * The type Base exception.
 */
public class BaseException extends RuntimeException {
    /**
     * Instantiates a new Base exception.
     */
    public BaseException() {
    }

    /**
     * Instantiates a new Base exception.
     *
     * @param message the message
     */
    public BaseException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Base exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Base exception.
     *
     * @param cause the cause
     */
    public BaseException(Throwable cause) {
        super(cause);
    }
}
