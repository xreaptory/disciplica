package com.disciplica;

/**
 * Thrown when a requested habit or task cannot be found.
 */
public class HabitNotFoundException extends Exception {

    /**
     * Constructs a new HabitNotFoundException with the given message.
     *
     * @param message human-readable description of what was not found
     */
    public HabitNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new HabitNotFoundException with a message and an underlying cause.
     *
     * @param message human-readable description of what was not found
     * @param cause   the underlying exception that triggered this one
     */
    public HabitNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
