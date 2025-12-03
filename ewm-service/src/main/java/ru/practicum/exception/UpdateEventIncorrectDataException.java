package ru.practicum.exception;

public class UpdateEventIncorrectDataException extends RuntimeException {
    public UpdateEventIncorrectDataException(String message) {
        super(message);
    }
}