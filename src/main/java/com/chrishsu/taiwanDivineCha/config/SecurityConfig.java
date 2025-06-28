package com.chrishsu.taiwanDivineCha.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections; // 確保引入 Collections

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // ... 其他 @Bean 定義，例如 UserDetailsService ...

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable()) // 禁用 CSRF
            .authorizeHttpRequests(auth -> auth

                    // --- 優先允許無需認證的 API 路徑 ---
                    .requestMatchers("/api/auth/register").permitAll()
                    .requestMatchers("/api/auth/check-email").permitAll()
                    .requestMatchers("/api/auth/captcha").permitAll()
                    .requestMatchers("/api/auth/forgot-password").permitAll()
                    .requestMatchers("/api/auth/reset-password").permitAll()
                    .requestMatchers("/api/auth/verify-email").permitAll()

                    // 也允許 Spring Boot 錯誤頁面，防止 403 for /error
                    .requestMatchers("/error").permitAll()

                    // 允許產品和分類相關 API 公開
                    .requestMatchers("/api/products/**").permitAll()
                    .requestMatchers("/api/categories/**").permitAll()

                    // --- 其他所有請求都需要認證 ---
                    .anyRequest().authenticated()
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource())); // 使用這個方法來配置 CORS

    return http.build();
  }

  // 定義 CORS 配置源的 Bean
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    // 從 application.properties 讀取 allowed-origins，或直接在這裡定義
    // 例如：configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://yourfrontend.com"));
    configuration.setAllowedOrigins(Collections.singletonList("http://localhost:4200")); // 允許特定來源
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 允許的 HTTP 方法
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept")); // 允許的請求頭
    configuration.setAllowCredentials(true); // 允許發送認證資訊（如 cookies, HTTP 認證或客戶端證書）
    configuration.setExposedHeaders(Arrays.asList("Authorization")); // 暴露給客戶端的響應頭

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // 將這個 CORS 配置應用於所有路徑
    return source;
  }
}