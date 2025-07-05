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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid; // 用於啟用 DTO 中的 @NotBlank, @Email 等驗證
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
      // 建議：為保持 JSON 格式一致性，這裡也可以返回一個帶有 message 的 JSON
      // Map<String, String> errorResponse = new HashMap<>();
      // errorResponse.put("message", "此電子郵件已被註冊。");
      // return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
      return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 返回 409 Conflict，不帶具體錯誤訊息，提高安全性
    } catch (IllegalArgumentException e) {
      // 捕獲其他如密碼不符合規則等驗證失敗（如果沒有被 @Valid 處理掉）
      System.err.println("Registration failed due to invalid argument: " + e.getMessage()); // 記錄錯誤
      // 同樣建議這裡返回 JSON
      // Map<String, String> errorResponse = new HashMap<>();
      // errorResponse.put("message", e.getMessage());
      // return ResponseEntity.badRequest().body(errorResponse);
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
  public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest,
                                                 HttpServletRequest request) { // 引入 HttpServletRequest
    try {
      String captchaId = null;
      if (request.getCookies() != null) {
        for (Cookie cookie : request.getCookies()) {
          if ("captchaId".equals(cookie.getName())) {
            captchaId = cookie.getValue();
            break;
          }
        }
      }

      // 將從 Cookie 獲取的 captchaId 設置到 LoginRequest DTO 中
      // 這是為了保持 AuthService 接口的簡潔性
      loginRequest.setCaptchaId(captchaId);

      LoginResponse response = authService.loginUser(loginRequest);
      return ResponseEntity.ok(response); // 200 OK
    } catch (BadCredentialsException e) {
      System.err.println("Login failed: " + e.getMessage());
      // 這邊已經返回 LoginResponse DTO，是 JSON 格式，保持不變
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse("Invalid email, password, or captcha.", null));
    }
  }

  /**
   * 處理忘記密碼請求
   * POST /api/auth/forgot-password
   * @param request 包含 email 的忘記密碼請求 DTO
   * @return 成功訊息 (無論郵箱是否存在，都返回相同訊息以避免洩露用戶信息)
   */
  @PostMapping("/forgot-password")
  public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    // 為了安全，無論郵箱是否存在，都返回相同的成功訊息，避免洩露用戶信息
    authService.createPasswordResetTokenForUser(request.getEmail());
    Map<String, String> response = new HashMap<>();
    response.put("message", "如果您的電子郵件存在，我們已發送重設密碼連結。"); // 將訊息放入 JSON 中
    return ResponseEntity.ok(response); // 返回 JSON 響應
  }

  /**
   * 處理重設密碼請求
   * POST /api/auth/reset-passwordo
   * @param request 包含 token 和 newPassword 的重設密碼請求 DTO
   * @return 成功訊息
   */
  @PostMapping("/reset-password")
  public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    try {
      authService.resetPassword(request.getToken(), request.getNewPassword());
      Map<String, String> response = new HashMap<>();
      response.put("message", "密碼已成功重設！請使用新密碼登入。"); // 將訊息放入 JSON 中
      return ResponseEntity.ok(response); // 返回 200 OK 和 JSON 響應
    } catch (InvalidTokenException e) {
      // 捕獲自定義的 InvalidTokenException (無效或過期 token)，返回 400 Bad Request
      System.err.println("Reset password failed: " + e.getMessage()); // 記錄錯誤
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("message", e.getMessage()); // 將錯誤訊息放入 JSON 中
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); // 返回 JSON 錯誤響應
    } catch (IllegalArgumentException e) {
      // 捕獲 IllegalArgumentException (例如密碼長度不夠)，返回 400 Bad Request
      System.err.println("Reset password failed: " + e.getMessage()); // 記錄錯誤
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("message", e.getMessage()); // 將錯誤訊息放入 JSON 中
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); // 返回 JSON 錯誤響應
    } catch (Exception e) {
      // 捕獲其他未知錯誤，返回 500 Internal Server Error
      System.err.println("An unexpected error occurred during password reset: " + e.getMessage()); // 記錄錯誤
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("message", "重設密碼時發生未知錯誤，請稍後再試。"); // 將錯誤訊息放入 JSON 中
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // 返回 JSON 錯誤響應
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


  /**
   * 獲取驗證碼圖片（Base64 編碼）
   * GET /api/auth/captcha
   *
   * @return ResponseEntity<String> 包含 Base64 編碼的驗證碼圖片數據（data:image/png;base64,...）
   * 以及一個與之關聯的唯一 ID (例如通過 Cookie 或響應頭傳遞)
   */
  @GetMapping("/captcha")
  public ResponseEntity<String> getCaptcha(HttpServletResponse response) { // 引入 HttpServletResponse
    try {
      // 調用 AuthService 來生成驗證碼和圖片
      // 這裡的邏輯需要您在 AuthService 中實現
      Map<String, String> captchaData = authService.generateCaptcha();
      String captchaId = captchaData.get("captchaId"); // 從服務層獲取驗證碼 ID
      String base64Image = captchaData.get("base64Image"); // 從服務層獲取 Base64 圖片

      // 將 captchaId 設置為 HttpOnly Cookie，這樣前端 JS 無法直接讀取，提高安全性
      // 確保您的前端在每次請求時都會自動帶上這個 Cookie
      ResponseCookie cookie = ResponseCookie.from("captchaId", captchaId)
              .httpOnly(true)       // HttpOnly: 防止客戶端腳本訪問 Cookie
              .secure(true)         // Secure: 僅在 HTTPS 連接上發送 (生產環境應為 true)、測試在HTTP上可先註解掉
              .path("/api/auth")    // Path: 限制 Cookie 僅對 /api/auth 路徑有效
              .maxAge(60 * 5)       // Max-Age: 設置 Cookie 過期時間 (例如 5 分鐘)
              .sameSite("Lax")      // SameSite: 防止 CSRF 攻擊，通常設置為 Lax 或 Strict，雖然瀏覽器預設也是 Lax，但在 localhost 跨端口的開發環境下，移除顯式設置有時會讓瀏覽器表現得更「寬鬆」
              .build();

      response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

      // 返回 Base64 編碼的圖片字符串
      return ResponseEntity.ok(base64Image); // 200 OK
    } catch (Exception e) {
      System.err.println("Error generating captcha: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate captcha.");
    }
  }

  /**
   * 檢查電子郵件是否已註冊的 API。
   * 該 API 在 Email 存在或不存在時都返回 200 OK，並在響應體中指示狀態。
   *
   * @param email 要檢查的電子郵件
   * @return ResponseEntity 包含 { "exists": true/false }
   */
  @GetMapping("/check-email")
  public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
    // 調用 UserService 檢查 Email 是否存在
    boolean exists = authService.isEmailAlreadyRegistered(email);

    Map<String, Boolean> response = new HashMap<>();
    response.put("exists", exists);

    // 返回 200 OK 和包含 exists 狀態的 JSON 響應
    return ResponseEntity.ok(response);
  }

}