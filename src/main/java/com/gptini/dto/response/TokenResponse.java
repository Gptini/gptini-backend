package com.gptini.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
