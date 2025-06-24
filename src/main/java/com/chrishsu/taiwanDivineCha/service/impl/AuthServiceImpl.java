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
import org.springframework.data.redis.core.StringRedisTemplate; // 引入 RedisTemplate
import java.time.Duration; // 用於設置 Redis 鍵的過期時間

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO; // 確保導入這個

@Service
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final EmailService emailService; // 假設您有 EmailService
  private final PasswordEncoder passwordEncoder; // 假設您有配置 PasswordEncoder
  private final StringRedisTemplate redisTemplate; // 注入 RedisTemplate

  @Value("${app.frontend.resetPasswordUrl}")
  private String frontendResetPasswordUrl;

  // 驗證碼相關常量
  private static final int CAPTCHA_WIDTH = 120;
  private static final int CAPTCHA_HEIGHT = 40;
  private static final int CAPTCHA_LENGTH = 4; // 驗證碼長度
  // 驗證碼字符集，排除容易混淆的 '0', 'O', '1', 'I', 'l'
  private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final long CAPTCHA_EXPIRATION_MINUTES = 5; // 驗證碼有效期 5 分鐘

  public AuthServiceImpl(UserRepository userRepository, EmailService emailService,
                         PasswordEncoder passwordEncoder, StringRedisTemplate redisTemplate) { // 注入 RedisTemplate
    this.userRepository = userRepository;
    this.emailService = emailService;
    this.passwordEncoder = passwordEncoder;
    this.redisTemplate = redisTemplate; // 初始化 RedisTemplate
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
   * @param loginRequest 包含 email, password, captcha 和 captchaId 的登入請求
   * @return 登入響應 (例如 JWT token 或用戶基本信息)
   */
  @Override
  @Transactional(readOnly = true) // 讀取操作，設置為只讀事務
  public LoginResponse loginUser(LoginRequest loginRequest) {
    // 1. 驗證驗證碼
    String captchaId = loginRequest.getCaptchaId();
    String userInputCaptcha = loginRequest.getCaptcha();

    if (captchaId == null || userInputCaptcha == null || !validateCaptcha(captchaId, userInputCaptcha)) {
      // 驗證碼錯誤或過期，拋出 BadCredentialsException
      throw new BadCredentialsException("Invalid captcha.");
    }

    // 2. 驗證用戶名和密碼
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

  // --- 驗證碼相關方法 ---

  /**
   * 生成驗證碼圖片（Base64 編碼）並儲存驗證碼到 Redis
   * @return Map 包含 "captchaId" (唯一標識符) 和 "base64Image" (圖片數據)
   */
  @Override
  public Map<String, String> generateCaptcha() {
    // 1. 生成隨機驗證碼字符串
    String captchaText = generateRandomCaptchaText(CAPTCHA_LENGTH);

    // 2. 生成唯一的驗證碼 ID
    String captchaId = UUID.randomUUID().toString();

    // 3. 將驗證碼存儲到 Redis，設置過期時間
    // Key: "captcha:id" Value: captchaText
    redisTemplate.opsForValue().set("captcha:" + captchaId, captchaText, Duration.ofMinutes(CAPTCHA_EXPIRATION_MINUTES));

    // 4. 將驗證碼字符串繪製成圖片並轉換為 Base64
    String base64Image = generateCaptchaImage(captchaText);

    Map<String, String> result = new HashMap<>();
    result.put("captchaId", captchaId);
    result.put("base64Image", base64Image);
    return result;
  }

  /**
   * 驗證用戶輸入的驗證碼
   * @param captchaId 儲存在 Redis 中的驗證碼 ID
   * @param userInputCaptcha 用戶輸入的驗證碼
   * @return true 如果驗證碼匹配且未過期，false 否則
   */
  @Override
  public boolean validateCaptcha(String captchaId, String userInputCaptcha) {
    // 獲取儲存的驗證碼
    String storedCaptcha = redisTemplate.opsForValue().get("captcha:" + captchaId);

    if (storedCaptcha == null) {
      // 驗證碼不存在或已過期
      System.out.println("Captcha ID not found or expired: " + captchaId);
      return false;
    }

    // 驗證通過後從 Redis 中刪除驗證碼，防止重用
    redisTemplate.delete("captcha:" + captchaId);
    System.out.println("Captcha removed from Redis: " + captchaId);


    // 比較時忽略大小寫，提高用戶體驗
    boolean isValid = storedCaptcha.equalsIgnoreCase(userInputCaptcha);
    if (!isValid) {
      System.out.println("Captcha mismatch for ID " + captchaId + ": Stored='" + storedCaptcha + "', Input='" + userInputCaptcha + "'");
    }
    return isValid;
  }

  // 輔助方法：生成隨機驗證碼字符串
  private String generateRandomCaptchaText(int length) {
    StringBuilder captcha = new StringBuilder();
    Random rnd = new Random();
    for (int i = 0; i < length; i++) {
      captcha.append(CAPTCHA_CHARS.charAt(rnd.nextInt(CAPTCHA_CHARS.length())));
    }
    System.out.println("Generated captcha text: " + captcha.toString());
    return captcha.toString();
  }

  // 輔助方法：將驗證碼字符串繪製成圖片並 Base64 編碼
  private String generateCaptchaImage(String captchaText) {
    BufferedImage image = new BufferedImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    Random rand = new Random();

    // 填充背景
    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT);

    // 設置字體
    g2d.setFont(new Font("Arial", Font.BOLD, 30));

    // 繪製扭曲的驗證碼文字
    for (int i = 0; i < captchaText.length(); i++) {
      g2d.setColor(new Color(rand.nextInt(200), rand.nextInt(200), rand.nextInt(200))); // 隨機顏色
      // 簡單的扭曲效果，可以更複雜
      g2d.drawString(String.valueOf(captchaText.charAt(i)), (i * 25) + 5, rand.nextInt(10) + 30);
    }

    // 繪製干擾線
    for (int i = 0; i < 5; i++) {
      g2d.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 100));
      g2d.drawLine(rand.nextInt(CAPTCHA_WIDTH), rand.nextInt(CAPTCHA_HEIGHT),
              rand.nextInt(CAPTCHA_WIDTH), rand.nextInt(CAPTCHA_HEIGHT));
    }

    g2d.dispose();

    // 將圖片轉換為 Base64 編碼的 PNG 字符串
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ImageIO.write(image, "png", baos);
      String base64String = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
      // System.out.println("Generated Base64 Image: " + base64String.substring(0, Math.min(base64String.length(), 100)) + "..."); // 打印部分 Base64 字符串
      return base64String;
    } catch (IOException e) {
      System.err.println("Error converting captcha image to Base64: " + e.getMessage());
      // 拋出一個運行時異常，讓 Spring 處理，或者返回一個錯誤標識
      throw new RuntimeException("Failed to generate captcha image.", e);
    }
  }
}