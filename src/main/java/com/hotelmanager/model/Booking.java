package com.hotelmanager.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record Booking(
        @NotNull(message = "{error.booking.hotelId.notNull}")
        String hotelId,

        @NotNull(message = "{error.booking.roomType.notNull}")
        String roomType,

        @NotNull(message = "{error.booking.roomRate.notNull}")
        String roomRate,

        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate arrival,

        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate departure
) {}