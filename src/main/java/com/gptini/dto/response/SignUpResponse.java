package com.gptini.dto.response;

public record SignUpResponse(
        UserResponse user,
        String accessToken,
        String refreshToken
) {}
