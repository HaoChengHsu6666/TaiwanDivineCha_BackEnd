package com.chrishsu.taiwanDivineCha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
  @NotBlank(message = "Token cannot be blank")
  private String token;

  @NotBlank(message = "New password cannot be blank")
  @Size(min = 6, message = "Password must be at least 6 characters long")
  private String newPassword;

  // Optional: 如果你想在後端進行雙重確認
  // private String confirmPassword;
}
