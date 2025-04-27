package com.hotelmanager.exception;

public class RoomTypeNotFoundException extends BookingSystemException {
    public RoomTypeNotFoundException(String hotelId, String roomType) {
        super(String.format("Room type '%s' not found in hotel '%s'", roomType, hotelId));
    }
}