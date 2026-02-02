package com.gptini.dto.response;

import com.gptini.entity.ChatRoomUserEntity;
import com.gptini.entity.UserEntity;

public record ChatRoomUserResponse(
        Long id,
        String nickname,
        String profileImageUrl,
        Long lastReadMessageId
) {
    public static ChatRoomUserResponse from(ChatRoomUserEntity chatRoomUser) {
        UserEntity user = chatRoomUser.getUser();
        return new ChatRoomUserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                chatRoomUser.getLastReadMessageId()
        );
    }
}
