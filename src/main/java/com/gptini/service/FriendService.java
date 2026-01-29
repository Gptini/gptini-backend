package com.gptini.service;

import com.gptini.dto.response.FriendRequestResponse;
import com.gptini.dto.response.FriendResponse;
import com.gptini.dto.response.UserResponse;

import java.util.List;

public interface FriendService {

    // 친구 코드로 유저 검색
    UserResponse searchByFriendCode(String friendCode);

    // 친구 요청 보내기
    FriendRequestResponse sendFriendRequest(Long userId, String friendCode);

    // 받은 친구 요청 목록
    List<FriendRequestResponse> getReceivedRequests(Long userId);

    // 보낸 친구 요청 목록
    List<FriendRequestResponse> getSentRequests(Long userId);

    // 친구 요청 수락
    void acceptRequest(Long userId, Long requestId);

    // 친구 요청 거절
    void rejectRequest(Long userId, Long requestId);

    // 친구 목록
    List<FriendResponse> getFriends(Long userId);

    // 친구 삭제
    void deleteFriend(Long userId, Long friendId);
}
