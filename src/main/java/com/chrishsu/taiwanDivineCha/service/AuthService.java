package com.chrishsu.taiwanDivineCha.service;

import com.chrishsu.taiwanDivineCha.dto.*;

import java.util.Map;

public interface AuthService {

    // 註冊新用戶
    UserDto registerUser(RegisterRequest registerRequest); // 返回 UserDto 而不是 User 實體

    // 用戶登入
    LoginResponse loginUser(LoginRequest loginRequest);

    // 忘記密碼 - 生成 token 並發送郵件
    void createPasswordResetTokenForUser(String email);

    // 重設密碼 - 驗證 token 並更新密碼
    void resetPassword(String token, String newPassword);

    // 驗證重設密碼 token 是否有效且未過期 (可選，用於前端檢查 token 有效性)
    boolean validateResetPasswordToken(String token);

    Map<String, String> generateCaptcha();

    boolean validateCaptcha(String captchaId, String userInputCaptcha);

    boolean isEmailAlreadyRegistered(String email);

    // 獲取用戶個人資料
    UserProfileDto getUserProfile(String email);

    // 更新用戶個人資料
    UserProfileDto updateUserProfile(String email, UserProfileDto userProfileDto);
}
