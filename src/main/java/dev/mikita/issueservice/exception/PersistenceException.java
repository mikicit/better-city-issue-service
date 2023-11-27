package dev.mikita.issueservice.exception;

/**
 * The type Persistence exception.
 */
public class PersistenceException extends BaseException {
    /**
     * Instantiates a new Persistence exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Persistence exception.
     *
     * @param cause the cause
     */
    public PersistenceException(Throwable cause) {
        super(cause);
    }
}
