package com.hotelmanager.model.response;

public record AvailabilityResponse(
        String hotelId,
        String roomType,
        String dateRange,
        int availability,
        String message
) {
    @Override
    public String toString() {
        return String.format("Hotel: %s, Room Type: %s, Date Range: %s, Available Rooms: %d",
                hotelId, roomType, dateRange, availability);
    }
}