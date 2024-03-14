package com.learn.example.demo.Utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VideoCallRoomManager {

    private static final Map<String, Set<String>> rooms = new HashMap<>();

    public static void addParticipant(String roomId, String userId) {
        rooms.computeIfAbsent(roomId, k -> new HashSet<>()).add(userId);
    }

    public static void removeParticipant(String roomId, String userId) {
        rooms.getOrDefault(roomId, new HashSet<>()).remove(userId);
        if (rooms.containsKey(roomId) && rooms.get(roomId).isEmpty()) {
            rooms.remove(roomId);
        }
    }

    public static Set<String> getParticipants(String roomId) {
        return rooms.getOrDefault(roomId, new HashSet<>());
    }
}
