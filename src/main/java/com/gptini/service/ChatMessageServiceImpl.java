package com.gptini.service;

import com.gptini.dto.request.SendMessageRequest;
import com.gptini.dto.response.ChatMessageResponse;
import com.gptini.entity.*;
import com.gptini.exception.BusinessException;
import com.gptini.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageServiceImpl implements ChatMessageService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMessageCounterRepository counterRepository;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long userId, Long roomId, SendMessageRequest request) {
        UserEntity sender = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        ChatRoomEntity chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> BusinessException.notFound("채팅방을 찾을 수 없습니다"));

        // 참여자 확인
        ChatRoomUserEntity chatRoomUser = chatRoomUserRepository.findByUserAndChatRoom(sender, chatRoom)
                .orElseThrow(() -> BusinessException.forbidden("채팅방에 참여하지 않았습니다"));

        // 메시지 카운터 증가 (비관적 락)
        ChatRoomMessageCounterEntity counter = counterRepository.findByRoomIdWithLock(roomId)
                .orElseThrow(() -> BusinessException.notFound("채팅방 카운터를 찾을 수 없습니다"));

        Long messageId = counter.getNextMessageId();

        // 메시지 저장
        ChatMessageEntity message = ChatMessageEntity.builder()
                .messageId(messageId)
                .roomId(roomId)
                .sender(sender)
                .type(request.type())
                .content(request.content())
                .fileUrl(request.fileUrl())
                .fileName(request.fileName())
                .build();

        chatMessageRepository.save(message);

        // 발신자는 읽음 처리
        chatRoomUser.updateLastReadMessageId(messageId);

        // 안 읽은 사람 수 계산 (본인 제외)
        int unreadCount = calculateUnreadCount(roomId, messageId);

        return ChatMessageResponse.from(message, unreadCount);
    }

    @Override
    public List<ChatMessageResponse> getMessages(Long userId, Long roomId, Long beforeId, int size) {
        // 참여자 확인
        if (!chatRoomUserRepository.existsByUserIdAndChatRoomId(userId, roomId)) {
            throw BusinessException.forbidden("채팅방에 참여하지 않았습니다");
        }

        PageRequest pageable = PageRequest.of(0, size);

        List<ChatMessageEntity> messages;
        if (beforeId == null) {
            messages = chatMessageRepository.findByRoomIdOrderByMessageIdDesc(roomId, pageable);
        } else {
            messages = chatMessageRepository.findByRoomIdAndMessageIdLessThan(roomId, beforeId, pageable);
        }

        // 역순으로 정렬 (오래된 순)
        List<ChatMessageEntity> reversed = new ArrayList<>(messages);
        java.util.Collections.reverse(reversed);

        return reversed.stream()
                .map(m -> ChatMessageResponse.from(m, calculateUnreadCount(roomId, m.getMessageId(), userId)))
                .toList();
    }

    @Override
    public int calculateUnreadCount(Long roomId, Long messageId) {
        return calculateUnreadCount(roomId, messageId, null);
    }

    /**
     * 안 읽은 사람 수 계산 (특정 유저 제외 가능)
     */
    public int calculateUnreadCount(Long roomId, Long messageId, Long excludeUserId) {
        List<ChatRoomUserEntity> users = chatRoomUserRepository.findByChatRoomId(roomId);

        int unreadCount = 0;
        for (ChatRoomUserEntity user : users) {
            // 제외할 유저는 건너뜀 (조회하는 본인)
            if (excludeUserId != null && user.getUser().getId().equals(excludeUserId)) {
                continue;
            }
            if (user.getLastReadMessageId() < messageId) {
                unreadCount++;
            }
        }

        return unreadCount;
    }
}
