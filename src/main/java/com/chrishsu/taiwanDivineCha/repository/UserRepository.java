package com.chrishsu.taiwanDivineCha.repository;

import com.chrishsu.taiwanDivineCha.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // 可選，但建議加上以明確其角色
public interface UserRepository extends JpaRepository<User, Integer> { // User Entity, Integer 主鍵類型

    // 根據 email 查找用戶，AuthServiceImpl 中有用到
    Optional<User> findByEmail(String email);

    // 根據 resetPasswordToken 查找用戶，AuthServiceImpl 中有用到
    Optional<User> findByResetPasswordToken(String resetPasswordToken);

    // 您可能還需要其他查詢方法，例如根據 email 和 password 查找（用於登入，但通常直接在Service層處理密碼驗證）
    // 或者判斷 email 是否已存在 (如果不在 service 層做 existsById)
    boolean existsByEmail(String email);

    Optional<User> findByEmailVerificationToken(String emailVerificationToken); //
}