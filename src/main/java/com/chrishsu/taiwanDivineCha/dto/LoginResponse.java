package com.chrishsu.taiwanDivineCha.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String email;
    private Boolean isProfileCompleted; // 新增欄位
    // 實際應用中，這裡會包含 JWT Token
    // private String jwtToken;
}