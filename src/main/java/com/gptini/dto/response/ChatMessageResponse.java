package com.gptini.dto.response;

import com.gptini.entity.ChatMessageEntity;
import com.gptini.enums.MessageType;

import java.time.ZonedDateTime;

public record ChatMessageResponse(
        Long messageId,
        Long roomId,
        Long senderId,
        String senderNickname,
        String senderProfileImageUrl,
        MessageType type,
        String content,
        String fileUrl,
        String fileName,
        ZonedDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessageEntity message) {
        return new ChatMessageResponse(
                message.getMessageId(),
                message.getRoomId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getSender().getProfileImageUrl(),
                message.getType(),
                message.getContent(),
                message.getFileUrl(),
                message.getFileName(),
                message.getCreatedAt()
        );
    }
}
