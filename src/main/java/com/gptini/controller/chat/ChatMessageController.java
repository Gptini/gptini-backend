package com.gptini.controller.chat;

import com.gptini.auth.UserPrincipal;
import com.gptini.dto.request.SendMessageRequest;
import com.gptini.dto.request.UpdateReadRequest;
import com.gptini.dto.response.ChatMessageResponse;
import com.gptini.dto.response.RoomUpdateMessage;
import com.gptini.entity.ChatRoomUserEntity;
import com.gptini.repository.ChatRoomUserRepository;
import com.gptini.service.ChatMessageService;
import com.gptini.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ChatService chatService;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * 메시지 전송
     * Client -> /pub/chat/rooms/{roomId}
     * Server -> /sub/chat/rooms/{roomId}
     */
    @MessageMapping("/chat/rooms/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload SendMessageRequest request,
            Principal principal
    ) {
        Long userId = extractUserId(principal);
        log.info("Message received: roomId={}, userId={}, type={}", roomId, userId, request.type());

        ChatMessageResponse response = chatMessageService.sendMessage(userId, roomId, request);

        // 해당 채팅방 구독자들에게 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + roomId, response);

        // 방 참여자들에게 ROOM_UPDATE 전송 (채팅 목록 실시간 업데이트용)
        sendRoomUpdateToParticipants(roomId, userId, response);
    }

    /**
     * 방 참여자들에게 ROOM_UPDATE 메시지 전송
     */
    private void sendRoomUpdateToParticipants(Long roomId, Long senderId, ChatMessageResponse message) {
        List<ChatRoomUserEntity> participants = chatRoomUserRepository.findByChatRoomId(roomId);

        for (ChatRoomUserEntity participant : participants) {
            Long participantId = participant.getUser().getId();

            // 각 참여자별 unreadCount 계산
            long unreadCount = Math.max(0, message.messageId() - participant.getLastReadMessageId());

            RoomUpdateMessage roomUpdate = RoomUpdateMessage.of(
                    roomId,
                    message.content(),
                    message.createdAt(),
                    message.senderId(),
                    message.senderNickname(),
                    unreadCount
            );

            // 각 유저의 개인 토픽으로 전송
            messagingTemplate.convertAndSend("/sub/users/" + participantId + "/rooms", roomUpdate);
        }
    }

    /**
     * 읽음 상태 업데이트
     * Client -> /pub/chat/rooms/{roomId}/read
     * Server -> /sub/chat/rooms/{roomId}/read
     */
    @MessageMapping("/chat/rooms/{roomId}/read")
    public void updateReadStatus(
            @DestinationVariable Long roomId,
            @Payload UpdateReadRequest request,
            Principal principal
    ) {
        Long userId = extractUserId(principal);
        log.info("Read status update: roomId={}, userId={}, messageId={}", roomId, userId, request.messageId());

        chatService.updateLastRead(userId, roomId, request.messageId());

        // 읽음 상태 변경 알림 (다른 사용자들의 unreadCount 업데이트용)
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + roomId + "/read",
                new ReadStatusUpdate(userId, request.messageId()));

        // 해당 유저에게 ROOM_UPDATE 전송 (unreadCount: 0)
        sendRoomUpdateAfterRead(roomId, userId);
    }

    /**
     * 읽음 처리 후 해당 유저에게 ROOM_UPDATE 전송
     */
    private void sendRoomUpdateAfterRead(Long roomId, Long userId) {
        chatService.getLatestMessageInfo(roomId).ifPresent(messageInfo -> {
            RoomUpdateMessage roomUpdate = RoomUpdateMessage.of(
                    roomId,
                    messageInfo.content(),
                    messageInfo.createdAt(),
                    messageInfo.senderId(),
                    messageInfo.senderNickname(),
                    0  // 방금 읽었으므로 0
            );

            messagingTemplate.convertAndSend("/sub/users/" + userId + "/rooms", roomUpdate);
        });
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            if (auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
                return userPrincipal.userId();
            }
        }
        throw new IllegalStateException("Unable to extract user ID from principal");
    }

    public record ReadStatusUpdate(Long userId, Long messageId) {}
}
