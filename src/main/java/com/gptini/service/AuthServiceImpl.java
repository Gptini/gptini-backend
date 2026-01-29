package com.gptini.service;

import com.gptini.auth.JwtUtil;
import com.gptini.dto.request.LoginRequest;
import com.gptini.dto.request.RefreshTokenRequest;
import com.gptini.dto.request.SignUpRequest;
import com.gptini.dto.response.SignUpResponse;
import com.gptini.dto.response.TokenResponse;
import com.gptini.dto.response.UserResponse;
import com.gptini.entity.UserEntity;
import com.gptini.exception.BusinessException;
import com.gptini.repository.UserRepository;
import com.gptini.util.FriendCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw BusinessException.conflict("이미 사용 중인 이메일입니다");
        }

        String friendCode = generateUniqueFriendCode();

        UserEntity user = UserEntity.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .friendCode(friendCode)
                .build();

        UserEntity savedUser = userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(savedUser.getId());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getId());

        return new SignUpResponse(
                UserResponse.from(savedUser),
                accessToken,
                refreshToken
        );
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> BusinessException.unauthorized("이메일 또는 비밀번호가 일치하지 않습니다"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw BusinessException.unauthorized("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new TokenResponse(accessToken, refreshToken);
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtUtil.validateToken(request.refreshToken())) {
            throw BusinessException.unauthorized("유효하지 않은 토큰입니다");
        }

        Long userId = jwtUtil.getUserIdFromToken(request.refreshToken());

        if (!userRepository.existsById(userId)) {
            throw BusinessException.unauthorized("존재하지 않는 사용자입니다");
        }

        String accessToken = jwtUtil.generateAccessToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        return new TokenResponse(accessToken, refreshToken);
    }

    private String generateUniqueFriendCode() {
        String code;
        int attempts = 0;
        do {
            code = FriendCodeGenerator.generate();
            attempts++;
            if (attempts > 100) {
                throw BusinessException.internalError("친구 코드 생성에 실패했습니다");
            }
        } while (userRepository.existsByFriendCode(code));
        return code;
    }
}
