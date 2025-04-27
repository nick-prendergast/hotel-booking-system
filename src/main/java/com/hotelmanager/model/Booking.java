package com.hotelmanager.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record Booking(
        String hotelId,

        String roomType,

        String roomRate,

        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate arrival,

        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate departure
) {
}