package com.hotelmanager.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RoomType(
        @NotNull(message = "{error.roomType.code.notNull}")
        @Size(min = 1, max = 10, message = "{error.roomType.code.size}")
        String code,

        @NotNull(message = "{error.roomType.description.notNull}")
        String description,

        List<String> amenities,
        List<String> features
) {}
