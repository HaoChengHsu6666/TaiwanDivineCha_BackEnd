package com.chrishsu.taiwanDivineCha.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    // --- 新增的驗證碼相關字段 ---
    @NotBlank(message = "Captcha is required") // 確保用戶輸入了驗證碼
    private String captcha;

    // captchaId 可以沒有 @NotBlank，因為它可能通過 HttpOnly Cookie 傳遞
    // 如果您希望前端也明確發送此 ID (例如，為了調試或特定場景)，則可以加 @NotBlank
    private String captchaId;
}