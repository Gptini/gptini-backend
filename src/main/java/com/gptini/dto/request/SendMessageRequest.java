package com.gptini.dto.request;

import com.gptini.enums.MessageType;

public record SendMessageRequest(
        MessageType type,
        String content,
        String fileUrl,
        String fileName
) {}
