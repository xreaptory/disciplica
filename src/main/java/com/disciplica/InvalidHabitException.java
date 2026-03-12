package com.disciplica;

/**
 * Thrown when a habit or task is in an invalid state or contains invalid data.
 */
public class InvalidHabitException extends Exception {

    /**
     * Constructs a new InvalidHabitException with the given message.
     *
     * @param message human-readable description of why the habit is invalid
     */
    public InvalidHabitException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidHabitException with a message and an underlying cause.
     *
     * @param message human-readable description of why the habit is invalid
     * @param cause   the underlying exception that triggered this one
     */
    public InvalidHabitException(String message, Throwable cause) {
        super(message, cause);
    }
}
