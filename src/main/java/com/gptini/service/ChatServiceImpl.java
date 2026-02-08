package com.gptini.service;

import com.gptini.dto.request.CreateChatRoomRequest;
import com.gptini.dto.response.ChatRoomListResponse;
import com.gptini.dto.response.ChatRoomResponse;
import com.gptini.dto.response.ChatRoomUserResponse;
import com.gptini.dto.response.UserResponse;
import com.gptini.entity.*;
import com.gptini.enums.ChatRoomType;
import com.gptini.enums.MessageType;
import com.gptini.exception.BusinessException;
import com.gptini.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMessageCounterRepository counterRepository;

    @Override
    @Transactional
    public ChatRoomResponse createRoom(Long userId, CreateChatRoomRequest request) {
        UserEntity creator = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        // 참여자 목록에 생성자 추가
        Set<Long> userIds = new HashSet<>(request.userIds());
        userIds.add(userId);

        List<UserEntity> users = userRepository.findByIdIn(new ArrayList<>(userIds));
        if (users.size() != userIds.size()) {
            throw BusinessException.badRequest("존재하지 않는 사용자가 포함되어 있습니다");
        }

        // 채팅방 타입 결정
        ChatRoomType type = users.size() == 2 ? ChatRoomType.PRIVATE : ChatRoomType.GROUP;

        // 1:1 채팅방인 경우 기존 채팅방 확인
        if (type == ChatRoomType.PRIVATE) {
            List<Long> userIdList = new ArrayList<>(userIds);
            log.info("Checking existing private room for users: {} and {}", userIdList.get(0), userIdList.get(1));

            Optional<ChatRoomEntity> existingRoom = chatRoomRepository.findPrivateRoomByUsers(
                    ChatRoomType.PRIVATE,
                    userIdList.get(0),
                    userIdList.get(1)
            );

            log.info("Existing room found: {}", existingRoom.isPresent());

            if (existingRoom.isPresent()) {
                log.info("Returning existing room: {}", existingRoom.get().getId());
                return ChatRoomResponse.from(existingRoom.get(), true);
            }
        }

        // 채팅방 생성
        ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                .name(request.name())
                .type(type)
                .build();

        ChatRoomEntity savedRoom = chatRoomRepository.save(chatRoom);

        // 메시지 카운터 초기화
        ChatRoomMessageCounterEntity counter = ChatRoomMessageCounterEntity.builder()
                .chatRoom(savedRoom)
                .build();
        counterRepository.save(counter);

        // 참여자 추가
        for (UserEntity user : users) {
            ChatRoomUserEntity chatRoomUser = ChatRoomUserEntity.builder()
                    .chatRoom(savedRoom)
                    .user(user)
                    .build();
            chatRoomUserRepository.save(chatRoomUser);
        }

        return ChatRoomResponse.from(savedRoom);
    }

    @Override
    public List<ChatRoomListResponse> getChatRooms(Long userId) {
        List<ChatRoomUserEntity> chatRoomUsers = chatRoomUserRepository.findByUserIdWithChatRoom(userId);

        if (chatRoomUsers.isEmpty()) {
            return Collections.emptyList();
        }

        // 채팅방 ID 목록
        List<Long> roomIds = chatRoomUsers.stream()
                .map(cru -> cru.getChatRoom().getId())
                .toList();

        // 메시지 카운터 조회
        Map<Long, Long> lastMessageIdMap = counterRepository.findByRoomIdIn(roomIds).stream()
                .collect(Collectors.toMap(
                        ChatRoomMessageCounterEntity::getRoomId,
                        ChatRoomMessageCounterEntity::getLastMessageId
                ));

        // 사용자별 lastReadMessageId 맵
        Map<Long, Long> lastReadMap = chatRoomUsers.stream()
                .collect(Collectors.toMap(
                        cru -> cru.getChatRoom().getId(),
                        ChatRoomUserEntity::getLastReadMessageId
                ));

        // 채팅방 목록 생성
        List<ChatRoomListResponse> responses = new ArrayList<>();

        for (ChatRoomUserEntity cru : chatRoomUsers) {
            ChatRoomEntity room = cru.getChatRoom();
            Long roomId = room.getId();

            // 안 읽은 메시지 수 계산
            Long lastMessageId = lastMessageIdMap.getOrDefault(roomId, 0L);
            Long lastReadId = lastReadMap.getOrDefault(roomId, 0L);
            long unreadCount = Math.max(0, lastMessageId - lastReadId);

            // 마지막 메시지 조회
            Optional<ChatMessageEntity> lastMessage = chatMessageRepository.findLatestByRoomId(roomId);

            responses.add(new ChatRoomListResponse(
                    roomId,
                    room.getName(),
                    room.getType(),
                    room.getUsers().size(),
                    lastMessage.map(m -> getMessagePreview(m.getType(), m.getContent())).orElse(null),
                    lastMessage.map(m -> m.getSender().getNickname()).orElse(null),
                    lastMessage.map(ChatMessageEntity::getCreatedAt).orElse(room.getCreatedAt()),
                    unreadCount
            ));
        }

        // 마지막 메시지 시간순 정렬 (최신순)
        responses.sort((a, b) -> b.lastMessageTime().compareTo(a.lastMessageTime()));

        return responses;
    }

    @Override
    public ChatRoomResponse getChatRoom(Long userId, Long roomId) {
        ChatRoomEntity chatRoom = chatRoomRepository.findByIdWithUsers(roomId)
                .orElseThrow(() -> BusinessException.notFound("채팅방을 찾을 수 없습니다"));

        // 참여자인지 확인
        if (!chatRoomUserRepository.existsByUserIdAndChatRoomId(userId, roomId)) {
            throw BusinessException.forbidden("채팅방에 참여하지 않았습니다");
        }

        return ChatRoomResponse.from(chatRoom);
    }

    @Override
    @Transactional
    public void leaveRoom(Long userId, Long roomId) {
        if (!chatRoomUserRepository.existsByUserIdAndChatRoomId(userId, roomId)) {
            throw BusinessException.badRequest("채팅방에 참여하지 않았습니다");
        }

        chatRoomUserRepository.deleteByUserIdAndChatRoomId(userId, roomId);
    }

    @Override
    public List<UserResponse> getRoomUsers(Long roomId) {
        List<ChatRoomUserEntity> users = chatRoomUserRepository.findByChatRoomId(roomId);

        return users.stream()
                .map(cru -> UserResponse.from(cru.getUser()))
                .toList();
    }

    @Override
    public List<ChatRoomUserResponse> getParticipants(Long userId, Long roomId) {
        // 참여자인지 확인
        if (!chatRoomUserRepository.existsByUserIdAndChatRoomId(userId, roomId)) {
            throw BusinessException.forbidden("채팅방에 참여하지 않았습니다");
        }

        List<ChatRoomUserEntity> users = chatRoomUserRepository.findByChatRoomId(roomId);

        return users.stream()
                .map(ChatRoomUserResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void updateLastRead(Long userId, Long roomId, Long messageId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        ChatRoomEntity chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> BusinessException.notFound("채팅방을 찾을 수 없습니다"));

        ChatRoomUserEntity chatRoomUser = chatRoomUserRepository.findByUserAndChatRoom(user, chatRoom)
                .orElseThrow(() -> BusinessException.forbidden("채팅방에 참여하지 않았습니다"));

        chatRoomUser.updateLastReadMessageId(messageId);
    }

    @Override
    public Optional<LatestMessageInfo> getLatestMessageInfo(Long roomId) {
        return chatMessageRepository.findLatestByRoomId(roomId)
                .map(msg -> new LatestMessageInfo(
                        getMessagePreview(msg.getType(), msg.getContent()),
                        msg.getCreatedAt(),
                        msg.getSender().getId(),
                        msg.getSender().getNickname()
                ));
    }

    private String getMessagePreview(MessageType type, String content) {
        if (type == MessageType.TEXT) return content;
        if (type == MessageType.GIF) return "(GIF)";
        if (type == MessageType.IMAGE) return "(이미지)";
        return "(파일)";
    }
}
