package com.disciplica.domain.exception;

public class InvalidHabitException extends Exception {
    public InvalidHabitException(String message) {
        super(message);
    }
    public InvalidHabitException(String message, Throwable cause) {
        super(message, cause);
    }
}

