package com.gptini.controller.friend;

import com.gptini.auth.UserPrincipal;
import com.gptini.dto.request.FriendRequestRequest;
import com.gptini.dto.response.ApiResponse;
import com.gptini.dto.response.FriendRequestResponse;
import com.gptini.dto.response.FriendResponse;
import com.gptini.dto.response.UserResponse;
import com.gptini.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    // 친구 코드로 유저 검색
    @GetMapping("/users/search")
    public ResponseEntity<ApiResponse<UserResponse>> searchUser(
            @RequestParam(required = false) String code) {
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("친구 코드를 입력해주세요"));
        }
        UserResponse response = friendService.searchByFriendCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 친구 요청 보내기
    @PostMapping("/friend-requests")
    public ResponseEntity<ApiResponse<FriendRequestResponse>> sendRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FriendRequestRequest request) {
        FriendRequestResponse response = friendService.sendFriendRequest(principal.userId(), request.friendCode());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 받은 친구 요청 목록
    @GetMapping("/friend-requests")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getReceivedRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<FriendRequestResponse> response = friendService.getReceivedRequests(principal.userId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 보낸 친구 요청 목록
    @GetMapping("/friend-requests/sent")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getSentRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<FriendRequestResponse> response = friendService.getSentRequests(principal.userId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 친구 요청 수락
    @PutMapping("/friend-requests/{requestId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId) {
        friendService.acceptRequest(principal.userId(), requestId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 친구 요청 거절
    @PutMapping("/friend-requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId) {
        friendService.rejectRequest(principal.userId(), requestId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 친구 목록
    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFriends(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<FriendResponse> response = friendService.getFriends(principal.userId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 친구 삭제
    @DeleteMapping("/friends/{friendId}")
    public ResponseEntity<ApiResponse<Void>> deleteFriend(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long friendId) {
        friendService.deleteFriend(principal.userId(), friendId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
