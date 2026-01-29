package com.gptini.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FriendRequestRequest(
        @NotBlank(message = "친구 코드를 입력해주세요")
        @Size(min = 8, max = 8, message = "친구 코드는 8자리입니다")
        String friendCode
) {
}
