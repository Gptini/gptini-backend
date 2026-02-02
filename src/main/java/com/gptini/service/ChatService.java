package com.gptini.service;

import com.gptini.dto.request.CreateChatRoomRequest;
import com.gptini.dto.response.ChatRoomListResponse;
import com.gptini.dto.response.ChatRoomResponse;
import com.gptini.dto.response.ChatRoomUserResponse;
import com.gptini.dto.response.UserResponse;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatService {

    ChatRoomResponse createRoom(Long userId, CreateChatRoomRequest request);

    List<ChatRoomListResponse> getChatRooms(Long userId);

    ChatRoomResponse getChatRoom(Long userId, Long roomId);

    void leaveRoom(Long userId, Long roomId);

    List<UserResponse> getRoomUsers(Long roomId);

    List<ChatRoomUserResponse> getParticipants(Long userId, Long roomId);

    void updateLastRead(Long userId, Long roomId, Long messageId);

    Optional<LatestMessageInfo> getLatestMessageInfo(Long roomId);

    record LatestMessageInfo(
            String content,
            ZonedDateTime createdAt,
            Long senderId,
            String senderNickname
    ) {}
}
