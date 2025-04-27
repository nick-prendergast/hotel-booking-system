package com.hotelmanager.exception;

public class BookingSystemException extends RuntimeException {
    public BookingSystemException(String message) {
        super(message);
    }

    public BookingSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}