package com.chrishsu.taiwanDivineCha.repository;

import com.chrishsu.taiwanDivineCha.model.Cart;
import com.chrishsu.taiwanDivineCha.model.CartItem;
import com.chrishsu.taiwanDivineCha.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartAndProductAndWeight(Cart cart, Product product, Integer weight);
    void deleteByCartAndProductAndWeight(Cart cart, Product product, Integer weight);
    List<CartItem> findByCartAndProduct(Cart cart, Product product);
}