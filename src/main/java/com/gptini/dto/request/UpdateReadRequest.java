package com.gptini.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateReadRequest(
        @NotNull(message = "메시지 ID는 필수입니다")
        Long messageId
) {}
