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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
        chatRoomUserRepository.findByUserAndChatRoom(sender, chatRoom)
                .orElseThrow(() -> BusinessException.forbidden("채팅방에 참여하지 않았습니다"));

        // 메시지 카운터 증가 (비관적 락)
        ChatRoomMessageCounterEntity counter = counterRepository.findByRoomIdWithLock(roomId)
                .orElseThrow(() -> BusinessException.notFound("채팅방 카운터를 찾을 수 없습니다"));

        Long messageId = counter.getNextMessageId();

        // 메시지 저장 (시간은 서버 수신 시점 기준)
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        ChatMessageEntity message = ChatMessageEntity.builder()
                .messageId(messageId)
                .roomId(roomId)
                .sender(sender)
                .type(request.type())
                .content(request.content())
                .fileUrl(request.fileUrl())
                .fileName(request.fileName())
                .createdAt(now)
                .build();

        chatMessageRepository.save(message);

        return ChatMessageResponse.from(message);
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
                .map(ChatMessageResponse::from)
                .toList();
    }
}
