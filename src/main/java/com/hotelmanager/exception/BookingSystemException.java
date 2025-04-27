package com.hotelmanager.exception;

public abstract class BookingSystemException extends RuntimeException {
    protected BookingSystemException(String message) {
        super(message);
    }

    protected BookingSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}