package com.hotelmanager.model;


import jakarta.validation.constraints.NotNull;

public record Room(
        @NotNull(message = "{error.room.roomType.notNull}")
        String roomType,

        @NotNull(message = "{error.room.roomId.notNull}")
        String roomId
) {}