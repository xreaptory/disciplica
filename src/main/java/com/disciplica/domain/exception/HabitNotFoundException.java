package com.disciplica.domain.exception;

public class HabitNotFoundException extends Exception {
    public HabitNotFoundException(String message) {
        super(message);
    }
    public HabitNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

