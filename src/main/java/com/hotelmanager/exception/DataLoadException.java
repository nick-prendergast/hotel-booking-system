package com.hotelmanager.exception;

public class DataLoadException extends BookingSystemException {
    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataLoadException(String message) {
        super(message);
    }
}