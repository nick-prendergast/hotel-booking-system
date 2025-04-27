package com.hotelmanager.model;


import java.util.List;

public record RoomType(
        String code,

        String description,

        List<String> amenities,
        List<String> features
) {
}
