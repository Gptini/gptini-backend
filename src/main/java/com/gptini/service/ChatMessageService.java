package com.gptini.service;

import com.gptini.dto.request.SendMessageRequest;
import com.gptini.dto.response.ChatMessageResponse;

import java.util.List;

public interface ChatMessageService {

    ChatMessageResponse sendMessage(Long userId, Long roomId, SendMessageRequest request);

    List<ChatMessageResponse> getMessages(Long userId, Long roomId, Long beforeId, int size);
}
