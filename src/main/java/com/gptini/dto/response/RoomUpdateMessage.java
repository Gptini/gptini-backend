package com.gptini.dto.response;

import java.time.LocalDateTime;

public record RoomUpdateMessage(
        String type,
        Long roomId,
        String lastMessage,
        LocalDateTime lastMessageTime,
        Long lastMessageSenderId,
        String lastMessageSenderNickname,
        long unreadCount
) {
    public static RoomUpdateMessage of(
            Long roomId,
            String lastMessage,
            LocalDateTime lastMessageTime,
            Long lastMessageSenderId,
            String lastMessageSenderNickname,
            long unreadCount
    ) {
        return new RoomUpdateMessage(
                "ROOM_UPDATE",
                roomId,
                lastMessage,
                lastMessageTime,
                lastMessageSenderId,
                lastMessageSenderNickname,
                unreadCount
        );
    }
}
