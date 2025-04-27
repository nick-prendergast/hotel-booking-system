package com.hotelmanager.model.response;

public record ConfigurationResult(
        boolean isValid,
        String hotelsFile,
        String bookingsFile,
        String errorMessage
) {
    public static ConfigurationResult valid(String hotelsFile, String bookingsFile) {
        return new ConfigurationResult(true, hotelsFile, bookingsFile, null);
    }

    public static ConfigurationResult invalid(String errorMessage) {
        return new ConfigurationResult(false, null, null, errorMessage);
    }
}