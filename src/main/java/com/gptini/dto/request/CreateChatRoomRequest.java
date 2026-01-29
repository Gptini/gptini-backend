package com.gptini.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateChatRoomRequest(
        @NotBlank(message = "채팅방 이름은 필수입니다")
        @Size(max = 50, message = "채팅방 이름은 50자 이하여야 합니다")
        String name,

        @NotEmpty(message = "참여자는 최소 1명 이상이어야 합니다")
        List<Long> userIds
) {}
