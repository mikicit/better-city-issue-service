package dev.mikita.issueservice.controller.handler;

import java.util.List;

/**
 * Contains information about an error and can be sent to client as JSON to let them know what went wrong.
 */
public class ErrorInfo {
    private String message;
    private List<String> errors;
    private String requestUri;

    /**
     * Instantiates a new Error info.
     */
    public ErrorInfo() {}

    /**
     * Instantiates a new Error info.
     *
     * @param message    the message
     * @param requestUri the request uri
     */
    public ErrorInfo(String message, String requestUri) {
        this.message = message;
        this.requestUri = requestUri;
    }

    /**
     * Instantiates a new Error info.
     *
     * @param message    the message
     * @param requestUri the request uri
     * @param errors     the errors
     */
    public ErrorInfo(String message, String requestUri, List<String> errors) {
        this.message = message;
        this.requestUri = requestUri;
        this.errors = errors;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets request uri.
     *
     * @return the request uri
     */
    public String getRequestUri() {
        return requestUri;
    }

    /**
     * Sets request uri.
     *
     * @param requestUri the request uri
     */
    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    /**
     * Sets errors.
     *
     * @param errors the errors
     */
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    /**
     * Gets errors.
     *
     * @return the errors
     */
    public List<String> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "ErrorInfo{" + requestUri + ", message = " + message + "}";
    }
}
