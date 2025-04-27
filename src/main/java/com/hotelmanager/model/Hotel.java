package com.hotelmanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@NoArgsConstructor
public class Hotel {
    private String id;
    private String name;
    private List<RoomType> roomTypes;
    private List<Room> rooms;
    private Map<String, Integer> totalRoomsByType;

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
        calculateTotalRoomsByType();
    }

    private void calculateTotalRoomsByType() {
        totalRoomsByType = new HashMap<>();
        for (Room room : rooms) {
            String type = room.roomType();
            totalRoomsByType.put(type, totalRoomsByType.getOrDefault(type, 0) + 1);
        }

        log.debug("Hotel {}: Calculated room totals: {}", id, totalRoomsByType);
    }

    public int getTotalRoomsByType(String roomType) {
        int total = totalRoomsByType.getOrDefault(roomType, 0);
        log.trace("Hotel {}: Total rooms of type {}: {}", id, roomType, total);
        return total;
    }
}