package com.gptini.service;

import com.gptini.dto.response.UserResponse;

public interface UserService {

    UserResponse getUser(Long userId);

    UserResponse getMe(Long userId);

    void withdraw(Long userId);
}
