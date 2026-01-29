package com.gptini.service;

import com.gptini.dto.response.FriendRequestResponse;
import com.gptini.dto.response.FriendResponse;
import com.gptini.dto.response.UserResponse;
import com.gptini.entity.FriendRequestEntity;
import com.gptini.entity.FriendRequestStatus;
import com.gptini.entity.FriendshipEntity;
import com.gptini.entity.UserEntity;
import com.gptini.exception.BusinessException;
import com.gptini.repository.FriendRequestRepository;
import com.gptini.repository.FriendshipRepository;
import com.gptini.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;

    @Override
    public UserResponse searchByFriendCode(String friendCode) {
        UserEntity user = userRepository.findByFriendCode(friendCode)
                .orElseThrow(() -> BusinessException.notFound("해당 코드의 사용자를 찾을 수 없습니다"));
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public FriendRequestResponse sendFriendRequest(Long userId, String friendCode) {
        UserEntity requester = getUser(userId);
        UserEntity receiver = userRepository.findByFriendCode(friendCode)
                .orElseThrow(() -> BusinessException.notFound("해당 코드의 사용자를 찾을 수 없습니다"));

        if (requester.getId().equals(receiver.getId())) {
            throw BusinessException.badRequest("자기 자신에게 친구 요청을 보낼 수 없습니다");
        }

        if (friendshipRepository.existsByUserAndFriend(requester, receiver)) {
            throw BusinessException.conflict("이미 친구인 사용자입니다");
        }

        if (friendRequestRepository.existsByRequesterAndReceiverAndStatus(
                requester, receiver, FriendRequestStatus.PENDING)) {
            throw BusinessException.conflict("이미 친구 요청을 보냈습니다");
        }

        // 상대방이 이미 요청을 보낸 경우 바로 수락 처리
        var existingRequest = friendRequestRepository.findByRequesterAndReceiver(receiver, requester);
        if (existingRequest.isPresent() && existingRequest.get().getStatus() == FriendRequestStatus.PENDING) {
            return acceptRequestInternal(existingRequest.get());
        }

        FriendRequestEntity request = FriendRequestEntity.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        return FriendRequestResponse.from(friendRequestRepository.save(request));
    }

    @Override
    public List<FriendRequestResponse> getReceivedRequests(Long userId) {
        UserEntity user = getUser(userId);
        return friendRequestRepository.findByReceiverAndStatus(user, FriendRequestStatus.PENDING)
                .stream()
                .map(FriendRequestResponse::from)
                .toList();
    }

    @Override
    public List<FriendRequestResponse> getSentRequests(Long userId) {
        UserEntity user = getUser(userId);
        return friendRequestRepository.findByRequesterAndStatus(user, FriendRequestStatus.PENDING)
                .stream()
                .map(FriendRequestResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void acceptRequest(Long userId, Long requestId) {
        FriendRequestEntity request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> BusinessException.notFound("친구 요청을 찾을 수 없습니다"));

        if (!request.getReceiver().getId().equals(userId)) {
            throw BusinessException.forbidden("해당 요청을 수락할 권한이 없습니다");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw BusinessException.badRequest("이미 처리된 요청입니다");
        }

        acceptRequestInternal(request);
    }

    private FriendRequestResponse acceptRequestInternal(FriendRequestEntity request) {
        request.accept();

        // 양방향 친구 관계 생성
        FriendshipEntity friendship1 = FriendshipEntity.create(request.getRequester(), request.getReceiver());
        FriendshipEntity friendship2 = FriendshipEntity.create(request.getReceiver(), request.getRequester());

        friendshipRepository.save(friendship1);
        friendshipRepository.save(friendship2);

        return FriendRequestResponse.from(request);
    }

    @Override
    @Transactional
    public void rejectRequest(Long userId, Long requestId) {
        FriendRequestEntity request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> BusinessException.notFound("친구 요청을 찾을 수 없습니다"));

        if (!request.getReceiver().getId().equals(userId)) {
            throw BusinessException.forbidden("해당 요청을 거절할 권한이 없습니다");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw BusinessException.badRequest("이미 처리된 요청입니다");
        }

        request.reject();
    }

    @Override
    public List<FriendResponse> getFriends(Long userId) {
        UserEntity user = getUser(userId);
        return friendshipRepository.findByUser(user)
                .stream()
                .map(FriendResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        UserEntity user = getUser(userId);
        UserEntity friend = getUser(friendId);

        // 양방향 삭제
        friendshipRepository.deleteByUserAndFriend(user, friend);
        friendshipRepository.deleteByUserAndFriend(friend, user);
    }

    private UserEntity getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));
    }
}
