package com.gptini.service;

import com.gptini.dto.response.UserResponse;
import com.gptini.entity.UserEntity;
import com.gptini.exception.BusinessException;
import com.gptini.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    public UserResponse getUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        return UserResponse.from(user);
    }

    @Override
    public UserResponse getMe(Long userId) {
        return getUser(userId);
    }

    @Override
    @Transactional
    public void withdraw(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        // 친구 요청 삭제
        friendRequestRepository.deleteByRequesterOrReceiver(user, user);

        // 친구 관계 삭제
        friendshipRepository.deleteByUserOrFriend(user, user);

        // 채팅 메시지 삭제
        chatMessageRepository.deleteBySenderId(userId);

        // 채팅방 참여 삭제
        chatRoomUserRepository.deleteByUserId(userId);

        // 사용자 삭제
        userRepository.delete(user);
    }
}
