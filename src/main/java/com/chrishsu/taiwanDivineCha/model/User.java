package com.chrishsu.taiwanDivineCha.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data // Lombok 註解，自動生成 getter, setter, equals, hashCode, toString
@Entity
@Table(name = "users") // 對應資料庫表名
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自動增長的主鍵
    private Integer userId;

    //  @JsonProperty("e_mail")// 加入此段，此參數在回傳至前端的(responsebody)時會有自訂義key名稱
    @Column(unique = true, nullable = false)
    private String email;

    // Email 驗證相關欄位
    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expiry")
    private LocalDateTime emailVerificationTokenExpiry;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false; // 預設為未驗證

    @JsonIgnore //此參數在回傳至前端的(responsebody)時會被忽略(隱藏)
    @Column
    private String password;

    @Column(name = "reset_password_token", unique = true) // 確保 token 是唯一的
    private String resetPasswordToken;

    @Column(name = "reset_password_expires")
    private LocalDateTime resetPasswordExpires;

    @CreatedDate
    @Column(name = "create_date", nullable = false, updatable = false) // updatable = false 確保創建後不會被修改
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;

    @Column(name = "name")
    private String name;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "birth")
    private java.time.LocalDate birth; // 使用 java.time.LocalDate 對應 SQL 的 DATE 類型

    @Column(name = "is_profile_completed", nullable = false, columnDefinition = "boolean default false")
    private Boolean isProfileCompleted = false; // 預設為 false

    // 您可能需要手動添加 @PrePersist 和 @PreUpdate 如果不啟用 Spring Data JPA Auditing
    // 或者確保您的 @EnableJpaAuditing 配置正確 (通常在主應用類或配置類中)
    // 如果沒有啟用 auditing，那麼 @CreatedDate 和 @LastModifiedDate 不會自動工作
    // 可以參考 Product 類中的 @PrePersist/@PreUpdate 方式來手動實現
    @PrePersist
    protected void onCreate() {
        if (this.createdDate == null) { // 避免重複設置，如果 auditing 已設置則會自動設置
            this.createdDate = LocalDateTime.now();
        }
        if (this.lastModifiedDate == null) {
            this.lastModifiedDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastModifiedDate = LocalDateTime.now();
    }

    // --- UserDetails Methods ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For now, we'll return a simple "USER" role.
        // You can expand this later to support multiple roles.
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        // JWT's subject will be the user's email
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // You can use the isEmailVerified flag to enable/disable users
        return isEmailVerified;
    }
}
