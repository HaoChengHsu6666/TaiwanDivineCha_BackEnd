package com.chrishsu.taiwanDivineCha.service.impl;

import com.chrishsu.taiwanDivineCha.model.User;
import com.chrishsu.taiwanDivineCha.service.AuthService; // 引入介面
import com.chrishsu.taiwanDivineCha.service.EmailService;
import com.chrishsu.taiwanDivineCha.repository.UserRepository;
import com.chrishsu.taiwanDivineCha.dto.UserDto; // 用於返回用戶信息
import com.chrishsu.taiwanDivineCha.dto.LoginRequest;
import com.chrishsu.taiwanDivineCha.dto.LoginResponse;
import com.chrishsu.taiwanDivineCha.dto.RegisterRequest;
import com.chrishsu.taiwanDivineCha.exception.InvalidTokenException; // 自定義異常
import com.chrishsu.taiwanDivineCha.exception.UserAlreadyExistsException; // 自定義異常
import com.chrishsu.taiwanDivineCha.exception.BadCredentialsException; // 自定義異常，或使用 Spring Security 內建的

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final EmailService emailService; // 假設您有 EmailService
  private final PasswordEncoder passwordEncoder; // 假設您有配置 PasswordEncoder

  @Value("${app.frontend.resetPasswordUrl}")
  private String frontendResetPasswordUrl;

  public AuthServiceImpl(UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.emailService = emailService;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * 註冊新用戶
   * @param registerRequest 包含 email 和 password 的註冊請求
   * @return 註冊成功的用戶信息 DTO
   */
  @Override
  @Transactional
  public UserDto registerUser(RegisterRequest registerRequest) {
    if (userRepository.existsByEmail(registerRequest.getEmail())) {
      throw new UserAlreadyExistsException("Email is already registered: " + registerRequest.getEmail());
    }

    User newUser = new User();
    newUser.setEmail(registerRequest.getEmail());
    // 對密碼進行加密
    newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
    // createdDate 和 lastModifiedDate 將由 @PrePersist 自動設置

    User savedUser = userRepository.save(newUser);
    return new UserDto(savedUser.getUserId(), savedUser.getEmail()); // 返回 DTO
  }

  /**
   * 用戶登入
   * @param loginRequest 包含 email 和 password 的登入請求
   * @return 登入響應 (例如 JWT token 或用戶基本信息)
   */
  @Override
  @Transactional(readOnly = true) // 讀取操作，設置為只讀事務
  public LoginResponse loginUser(LoginRequest loginRequest) {
    User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Email or password incorrect.")); // 統一錯誤訊息，避免洩露用戶是否存在

    // 驗證密碼
    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Email or password incorrect.");
    }

    // 如果是 JWT 認證，這裡會生成 JWT token
    // 假設這裡返回一個簡單的成功響應
    return new LoginResponse("Login successful!", user.getEmail());
    // 實際應用中，這裡會返回 JWT Token
  }


  /**
   * 忘記密碼請求 - 生成 token 並發送郵件
   * @param email 用戶郵箱
   */
  @Override
  @Transactional
  public void createPasswordResetTokenForUser(String email) {
    Optional<User> userOptional = userRepository.findByEmail(email);

    if (userOptional.isEmpty()) {
      // 為了安全，即使找不到用戶，也返回成功訊息，避免洩露用戶是否存在
      System.out.println("Attempted forgot password for non-existent email: " + email);
      return; // 不拋出異常，假裝成功
    }

    User user = userOptional.get();
    String token = UUID.randomUUID().toString(); // 生成 UUID 作為令牌

    user.setResetPasswordToken(token);
    user.setResetPasswordExpires(LocalDateTime.now().plusHours(1)); // 設定令牌有效期，例如 1 小時
    // lastModifiedDate 將由 @PreUpdate 自動設置
    userRepository.save(user);

    // 構建重設連結
    String resetLink = frontendResetPasswordUrl + token;
    emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
  }

  /**
   * 重設密碼 - 驗證 token 並更新密碼
   * @param token 重設密碼令牌
   * @param newPassword 新密碼
   */
  @Override
  @Transactional
  public void resetPassword(String token, String newPassword) {
    Optional<User> userOptional = userRepository.findByResetPasswordToken(token);

    if (userOptional.isEmpty()) {
      throw new InvalidTokenException("Invalid or missing reset password token.");
    }

    User user = userOptional.get();

    // 檢查令牌是否過期
    if (user.getResetPasswordExpires() == null || user.getResetPasswordExpires().isBefore(LocalDateTime.now())) {
      // 使過期令牌失效，避免再次嘗試
      user.setResetPasswordToken(null);
      user.setResetPasswordExpires(null);
      userRepository.save(user); // 保存更新以使令牌失效
      throw new InvalidTokenException("Reset password token has expired. Please request a new one.");
    }

    // 密碼驗證邏輯通常在 DTO 的 @Valid 註解中處理，但這裡做一個簡單的服務層檢查
    if (newPassword == null || newPassword.length() < 6) {
      throw new IllegalArgumentException("Password must be at least 6 characters long.");
    }

    // 哈希新密碼並更新
    user.setPassword(passwordEncoder.encode(newPassword));

    // 使令牌失效 (一次性使用)
    user.setResetPasswordToken(null);
    user.setResetPasswordExpires(null);
    // lastModifiedDate 將由 @PreUpdate 自動設置
    userRepository.save(user);
  }

  /**
   * 驗證重設密碼 token 是否有效且未過期
   * @param token 重設密碼令牌
   * @return 如果 token 有效且未過期則返回 true，否則返回 false
   */
  @Override
  @Transactional(readOnly = true)
  public boolean validateResetPasswordToken(String token) {
    if (token == null || token.trim().isEmpty()) {
      return false;
    }
    return userRepository.findByResetPasswordToken(token)
            .map(user -> user.getResetPasswordExpires() != null && user.getResetPasswordExpires().isAfter(LocalDateTime.now()))
            .orElse(false);
  }
}