package com.chrishsu.taiwanDivineCha.repository;

import com.chrishsu.taiwanDivineCha.model.Cart;
import com.chrishsu.taiwanDivineCha.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUser(User user);
}
