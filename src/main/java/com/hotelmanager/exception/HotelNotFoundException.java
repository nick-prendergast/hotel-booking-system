package com.hotelmanager.exception;

public class HotelNotFoundException extends BookingSystemException {
    public HotelNotFoundException(String hotelId) {
        super("Hotel not found: " + hotelId);
    }
}
