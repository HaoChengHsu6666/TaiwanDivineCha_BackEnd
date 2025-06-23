package com.chrishsu.taiwanDivineCha.controller;

import com.chrishsu.taiwanDivineCha.dto.UserDto; // 引入用於返回用戶信息的 DTO
import com.chrishsu.taiwanDivineCha.dto.ForgotPasswordRequest;
import com.chrishsu.taiwanDivineCha.dto.LoginRequest;
import com.chrishsu.taiwanDivineCha.dto.LoginResponse; // 引入登入響應 DTO
import com.chrishsu.taiwanDivineCha.dto.RegisterRequest; // 引入註冊請求 DTO
import com.chrishsu.taiwanDivineCha.dto.ResetPasswordRequest;

import com.chrishsu.taiwanDivineCha.exception.InvalidTokenException; // 自定義異常
import com.chrishsu.taiwanDivineCha.exception.UserAlreadyExistsException; // 自定義異常
import com.chrishsu.taiwanDivineCha.exception.BadCredentialsException; // 自定義異常

import com.chrishsu.taiwanDivineCha.service.AuthService;
import jakarta.validation.Valid; // 用於啟用 DTO 中的 @NotBlank, @Email 等驗證
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth") // 所有認證相關的 API 都以 /api/auth 開頭
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*", allowCredentials = "true")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * 用戶註冊
   * POST /api/auth/register
   * @param registerRequest 註冊請求 DTO
   * @return 註冊成功的用戶信息 (UserDto)
   */
  @PostMapping("/register")
  public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
    try {
      UserDto registeredUser = authService.registerUser(registerRequest);
      return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser); // 201 Created
    } catch (UserAlreadyExistsException e) {
      // 捕獲自定義的 UserAlreadyExistsException，返回 409 Conflict
      System.err.println("Registration failed: " + e.getMessage()); // 記錄錯誤
      return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 返回 409 Conflict，不帶具體錯誤訊息，提高安全性
    } catch (IllegalArgumentException e) {
      // 捕獲其他如密碼不符合規則等驗證失敗（如果沒有被 @Valid 處理掉）
      System.err.println("Registration failed due to invalid argument: " + e.getMessage()); // 記錄錯誤
      return ResponseEntity.badRequest().build(); // 400 Bad Request
    }
  }

  /**
   * 用戶登入
   * POST /api/auth/login
   * @param loginRequest 登入請求 DTO
   * @return 登入響應 (LoginResponse，實際可能包含 JWT Token)
   */
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
    try {
      LoginResponse response = authService.loginUser(loginRequest);
      return ResponseEntity.ok(response); // 200 OK
    } catch (BadCredentialsException e) {
      // 捕獲自定義的 BadCredentialsException，返回 401 Unauthorized
      System.err.println("Login failed: " + e.getMessage()); // 記錄錯誤
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse("Invalid email or password.", null)); // 統一錯誤訊息，不暴露具體原因
    }
  }

  /**
   * 處理忘記密碼請求
   * POST /api/auth/forgot-password
   * @param request 包含 email 的忘記密碼請求 DTO
   * @return 成功訊息 (無論郵箱是否存在，都返回相同訊息以避免洩露用戶信息)
   */
  @PostMapping("/forgot-password")
  public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    // 為了安全，無論郵箱是否存在，都返回相同的成功訊息，避免洩露用戶信息
    authService.createPasswordResetTokenForUser(request.getEmail());
    return ResponseEntity.ok("如果您的電子郵件存在，我們已發送重設密碼連結。"); // 返回 String
  }

  /**
   * 處理重設密碼請求
   * POST /api/auth/reset-password
   * @param request 包含 token 和 newPassword 的重設密碼請求 DTO
   * @return 成功訊息
   */
  @PostMapping("/reset-password")
  public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    try {
      authService.resetPassword(request.getToken(), request.getNewPassword());
      return ResponseEntity.ok("密碼已成功重設！請使用新密碼登入。"); // 返回 String
    } catch (InvalidTokenException e) {
      // 捕獲自定義的 InvalidTokenException (無效或過期 token)，返回 400 Bad Request
      System.err.println("Reset password failed: " + e.getMessage()); // 記錄錯誤
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (IllegalArgumentException e) {
      // 捕獲 IllegalArgumentException (例如密碼長度不夠)，返回 400 Bad Request
      System.err.println("Reset password failed: " + e.getMessage()); // 記錄錯誤
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      // 捕獲其他未知錯誤，返回 500 Internal Server Error
      System.err.println("An unexpected error occurred during password reset: " + e.getMessage()); // 記錄錯誤
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("重設密碼時發生未知錯誤，請稍後再試。");
    }
  }

  /**
   * 驗證重設密碼 token 是否有效
   * GET /api/auth/validate-reset-token?token={token}
   * @param token 重設密碼令牌
   * @return true 如果有效，false 如果無效或過期
   */
  @GetMapping("/validate-reset-token")
  public ResponseEntity<Boolean> validateResetToken(@RequestParam String token) {
    boolean isValid = authService.validateResetPasswordToken(token);
    return ResponseEntity.ok(isValid); // 200 OK, body is true or false
  }

  // 全局處理 DTO 驗證失敗的異常
  // 當 @Valid 註解的 DTO 驗證失敗時，會拋出 MethodArgumentNotValidException
  @ResponseStatus(HttpStatus.BAD_REQUEST) // 設定響應狀態碼為 400 Bad Request
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = "";
      String errorMessage = error.getDefaultMessage();

      // 如果是 FieldError，可以獲取字段名
      if (error instanceof org.springframework.validation.FieldError) {
        fieldName = ((org.springframework.validation.FieldError) error).getField();
      } else {
        // 對於非字段特定的錯誤（如類級別的驗證），只返回預設訊息
        fieldName = error.getObjectName(); // 通常是 DTO 的類名
      }
      errors.put(fieldName, errorMessage);
    });
    System.err.println("Validation errors: " + errors); // 記錄驗證錯誤
    return errors;
  }
}