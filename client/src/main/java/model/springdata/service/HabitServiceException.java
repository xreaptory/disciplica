package model.springdata.service;

public class HabitServiceException extends RuntimeException {
    public HabitServiceException(String message) {
        super(message);
    }

    public HabitServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
