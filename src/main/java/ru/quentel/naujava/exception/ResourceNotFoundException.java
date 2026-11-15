package ru.quentel.naujava.exception;

/**
 * Ресурс не найден -> HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}