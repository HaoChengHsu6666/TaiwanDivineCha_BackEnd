package com.chrishsu.taiwanDivineCha.service.impl;

import com.chrishsu.taiwanDivineCha.model.User;
import com.chrishsu.taiwanDivineCha.service.AuthService; // 引入介面
import com.chrishsu.taiwanDivineCha.service.EmailService;
import com.chrishsu.taiwanDivineCha.repository.UserRepository;
import com.chrishsu.taiwanDivineCha.dto.UserDto; // 用於返回用戶信息
import com.chrishsu.taiwanDivineCha.dto.LoginRequest;
import com.chrishsu.taiwanDivineCha.dto.LoginResponse;
import com.chrishsu.taiwanDivineCha.dto.RegisterRequest;
import com.chrishsu.taiwanDivineCha.dto.UserProfileDto; // 引入 UserProfileDto
import com.chrishsu.taiwanDivineCha.exception.InvalidTokenException; // 自定義異常
import com.chrishsu.taiwanDivineCha.exception.UserAlreadyExistsException; // 自定義異常
import com.chrishsu.taiwanDivineCha.exception.BadCredentialsException; // 自定義異常，或使用 Spring Security 內建的

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // 引入 UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate; // 引入 RedisTemplate
import java.time.Duration; // 用於設置 Redis 鍵的過期時間

import java.time.LocalDateTime;
import java.time.LocalDate; // 引入 LocalDate
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
   * @param registerRequest 包含 email 的註冊請求 (密碼將在後續設定)
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

    // --- 核心修改：生成 Email 驗證 token 並設置帳號為未驗證 ---
    String emailVerificationToken = UUID.randomUUID().toString();
    // 驗證連結 24 小時有效
    LocalDateTime emailVerificationTokenExpiry = LocalDateTime.now().plusHours(24);

    newUser.setEmailVerificationToken(emailVerificationToken);
    newUser.setEmailVerificationTokenExpiry(emailVerificationTokenExpiry);
    newUser.setIsEmailVerified(false); // 新註冊用戶預設為未驗證

    User savedUser = userRepository.save(newUser); // 保存包含驗證 token 的用戶信息

    // --- 核心修改：發送 Email 驗證郵件 ---
    String verificationLink = frontendResetPasswordUrl + emailVerificationToken;
    emailService.sendEmailVerificationAndSetPasswordEmail(savedUser.getEmail(), verificationLink); // 您需要在 EmailService 中實現此方法

    return new UserDto(savedUser.getUserId(), savedUser.getEmail());
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

    // 核心修改：登入時檢查 Email 是否已驗證
    if (user.getIsEmailVerified() == null || !user.getIsEmailVerified()) {
      throw new BadCredentialsException("Please verify your email address before logging in.");
    }

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

    // --- 核心修改：只有已驗證的 Email 才能請求重設密碼 ---
    if (user.getIsEmailVerified() == null || !user.getIsEmailVerified()) {
      System.out.println("Forgot password requested for unverified email: " + email);
      return; // 返回成功訊息，但實際上不發送郵件
    }

    String token = UUID.randomUUID().toString(); // 生成 UUID 作為令牌
    user.setResetPasswordToken(token);
    user.setResetPasswordExpires(LocalDateTime.now().plusMinutes(10)); // 設定令牌有效期， 10分鐘
    userRepository.save(user);

    // 構建重設連結 (這個連結現在用於「忘記密碼」和「首次設定密碼」)
    String resetLink = frontendResetPasswordUrl + token;
    emailService.sendResetPasswordEmail(user.getEmail(), resetLink);

  }

  /**
   * 重設密碼 - 驗證 token 並更新密碼
   * 此方法也用於新用戶初次設定密碼
   * @param token 重設密碼令牌
   * @param newPassword 新密碼
   */
  @Override
  @Transactional
  public void resetPassword(String token, String newPassword) {
    System.out.println("Received token in resetPassword: " + token); // Log the token received

    // 嘗試查找 email_verification_token
    Optional<User> userByEmailVerificationToken = userRepository.findByEmailVerificationToken(token);
    System.out.println("userByEmailVerificationToken.isPresent(): " + userByEmailVerificationToken.isPresent());

    // 嘗試查找 reset_password_token
    Optional<User> userByResetPasswordToken = userRepository.findByResetPasswordToken(token);
    System.out.println("userByResetPasswordToken.isPresent(): " + userByResetPasswordToken.isPresent());

    User user;

    if (userByEmailVerificationToken.isPresent()) {
      user = userByEmailVerificationToken.get();
      System.out.println("Found user by email verification token: " + user.getEmail());

      // 檢查 Email 驗證 Token 是否過期
      if (user.getEmailVerificationTokenExpiry() == null || user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
        throw new InvalidTokenException("Email verification token is expired or invalid.");
      }
      // 驗證成功後，標記 Email 為已驗證
      user.setIsEmailVerified(true);
      // 清空 email_verification_token
      user.setEmailVerificationToken(null);
      user.setEmailVerificationTokenExpiry(null);

    } else if (userByResetPasswordToken.isPresent()) {
      user = userByResetPasswordToken.get();
      System.out.println("Found user by reset password token: " + user.getEmail());

      // 檢查重設密碼 Token 是否過期
      if (user.getResetPasswordExpires() == null || user.getResetPasswordExpires().isBefore(LocalDateTime.now())) {
        throw new InvalidTokenException("Reset password token is expired or invalid.");
      }
      // 清空 reset_password_token
      user.setResetPasswordToken(null);
      user.setResetPasswordExpires(null);

    } else {
      System.err.println("No user found for token: " + token); // Log which token failed
      // 如果兩種 Token 都找不到對應的用戶，則拋出無效 Token 異常
      throw new InvalidTokenException("Invalid token provided.");
    }

    // 驗證新密碼的複雜度 (這裡可以重複前端的驗證邏輯，作為二次驗證)
    // 這個檢查應在確定用戶存在且 Token 有效之後進行
    if (newPassword == null || newPassword.length() < 8 ||
            !newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
      throw new IllegalArgumentException("Password does not meet complexity requirements.");
    }

    // 加密新密碼並保存用戶信息
    user.setPassword(passwordEncoder.encode(newPassword));
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

  /**
   * 檢查電子郵件是否已在數據庫中註冊。
   *
   * @param email 要檢查的電子郵件
   * @return 如果 Email 已存在則返回 true，否則返回 false
   */
  public boolean isEmailAlreadyRegistered(String email) {
    // 調用 UserRepository 檢查是否存在該 Email 的用戶
    // 假設 UserRepository 有一個 findByEmail(String email) 方法，
    // 並且如果找到則返回 User 實例，否則返回 null 或 Optional.empty()
    return userRepository.findByEmail(email).isPresent();
  }

  /**
   * 獲取用戶個人資料。
   * @param email 用戶郵箱
   * @return 用戶個人資料 DTO
   * @throws UsernameNotFoundException 如果找不到用戶
   */
  @Override
  @Transactional(readOnly = true)
  public UserProfileDto getUserProfile(String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

    UserProfileDto userProfileDto = new UserProfileDto();
    userProfileDto.setEmail(user.getEmail());
    userProfileDto.setName(user.getName());
    userProfileDto.setMobile(user.getMobile());
    userProfileDto.setBirth(user.getBirth());
    userProfileDto.setIsProfileCompleted(user.getIsProfileCompleted());
    return userProfileDto;
  }

  /**
   * 更新用戶個人資料。
   * @param email 用戶郵箱
   * @param userProfileDto 包含更新資料的 DTO
   * @return 更新後的用戶個人資料 DTO
   * @throws UsernameNotFoundException 如果找不到用戶
   */
  @Override
  @Transactional
  public UserProfileDto updateUserProfile(String email, UserProfileDto userProfileDto) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

    user.setName(userProfileDto.getName());
    user.setMobile(userProfileDto.getMobile());
    user.setBirth(userProfileDto.getBirth());

    // 如果姓名不為空，則將 isProfileCompleted 設置為 true
    if (userProfileDto.getName() != null && !userProfileDto.getName().trim().isEmpty()) {
        user.setIsProfileCompleted(true);
    } else {
        user.setIsProfileCompleted(false); // 如果姓名為空，則設置為 false
    }

    User updatedUser = userRepository.save(user);

    UserProfileDto updatedUserProfileDto = new UserProfileDto();
    updatedUserProfileDto.setEmail(updatedUser.getEmail());
    updatedUserProfileDto.setName(updatedUser.getName());
    updatedUserProfileDto.setMobile(updatedUser.getMobile());
    updatedUserProfileDto.setBirth(updatedUser.getBirth());
    updatedUserProfileDto.setIsProfileCompleted(updatedUser.getIsProfileCompleted());
    return updatedUserProfileDto;
  }
}
