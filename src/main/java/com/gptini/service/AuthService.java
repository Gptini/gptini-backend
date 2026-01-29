package com.gptini.service;

import com.gptini.dto.request.LoginRequest;
import com.gptini.dto.request.RefreshTokenRequest;
import com.gptini.dto.request.SignUpRequest;
import com.gptini.dto.response.SignUpResponse;
import com.gptini.dto.response.TokenResponse;

public interface AuthService {

    SignUpResponse signUp(SignUpRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);
}
