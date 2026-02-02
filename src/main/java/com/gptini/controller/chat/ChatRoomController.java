package com.gptini.controller.chat;

import com.gptini.auth.UserPrincipal;
import com.gptini.dto.request.CreateChatRoomRequest;
import com.gptini.dto.request.UpdateReadRequest;
import com.gptini.dto.response.ApiResponse;
import com.gptini.dto.response.ChatMessageResponse;
import com.gptini.dto.response.ChatRoomListResponse;
import com.gptini.dto.response.ChatRoomResponse;
import com.gptini.dto.response.ChatRoomUserResponse;
import com.gptini.dto.response.UserResponse;
import com.gptini.service.ChatMessageService;
import com.gptini.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;
    private final ChatMessageService chatMessageService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateChatRoomRequest request
    ) {
        ChatRoomResponse response = chatService.createRoom(principal.userId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("채팅방이 생성되었습니다", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomListResponse>>> getChatRooms(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<ChatRoomListResponse> response = chatService.getChatRooms(principal.userId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long roomId
    ) {
        ChatRoomResponse response = chatService.getChatRoom(principal.userId(), roomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long roomId
    ) {
        chatService.leaveRoom(principal.userId(), roomId);
        return ResponseEntity.ok(ApiResponse.success("채팅방을 나갔습니다", null));
    }

    @GetMapping("/{roomId}/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getRoomUsers(@PathVariable Long roomId) {
        List<UserResponse> response = chatService.getRoomUsers(roomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{roomId}/participants")
    public ResponseEntity<ApiResponse<List<ChatRoomUserResponse>>> getParticipants(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long roomId
    ) {
        List<ChatRoomUserResponse> response = chatService.getParticipants(principal.userId(), roomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "50") int size
    ) {
        List<ChatMessageResponse> response = chatMessageService.getMessages(
                principal.userId(), roomId, beforeId, size
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{roomId}/read")
    public ResponseEntity<ApiResponse<Void>> updateLastRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateReadRequest request
    ) {
        chatService.updateLastRead(principal.userId(), roomId, request.messageId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
