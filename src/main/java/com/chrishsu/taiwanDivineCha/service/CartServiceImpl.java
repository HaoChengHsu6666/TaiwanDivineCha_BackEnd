package com.chrishsu.taiwanDivineCha.service;

import com.chrishsu.taiwanDivineCha.dto.AddToCartRequest;
import com.chrishsu.taiwanDivineCha.dto.CartDto;
import com.chrishsu.taiwanDivineCha.dto.CartItemDto;
import com.chrishsu.taiwanDivineCha.dto.UpdateCartItemRequest;
import com.chrishsu.taiwanDivineCha.model.Cart;
import com.chrishsu.taiwanDivineCha.model.CartItem;
import com.chrishsu.taiwanDivineCha.model.Product;
import com.chrishsu.taiwanDivineCha.model.User;
import com.chrishsu.taiwanDivineCha.repository.CartItemRepository;
import com.chrishsu.taiwanDivineCha.repository.CartRepository;
import com.chrishsu.taiwanDivineCha.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    @Transactional
    public void addToCart(User user, AddToCartRequest addToCartRequest) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(addToCartRequest.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (product.getStock() < addToCartRequest.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock");
        }

        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProductAndWeight(cart, product, addToCartRequest.getWeight());

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + addToCartRequest.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock for updated quantity");
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem newCartItem = new CartItem();
            newCartItem.setCart(cart);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(addToCartRequest.getQuantity());
            newCartItem.setWeight(addToCartRequest.getWeight());
            cartItemRepository.save(newCartItem);
        }
    }

    @Override
    @Transactional
    public void updateCartItem(User user, UpdateCartItemRequest updateCartItemRequest) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(updateCartItemRequest.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProductAndWeight(cart, product, updateCartItemRequest.getWeight());

        if (existingCartItem.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found");
        }

        CartItem cartItem = existingCartItem.get();
        int newQuantity = updateCartItemRequest.getQuantity();

        if (newQuantity == 0) {
            cartItemRepository.delete(cartItem);
        } else {
            if (product.getStock() < newQuantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock for updated quantity");
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        }
    }

    @Override
    @Transactional
    public void removeCartItem(User user, String productId, Integer weight) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        cartItemRepository.deleteByCartAndProductAndWeight(cart, product, weight);
    }

    @Override
    public CartDto getCart(User user) {
        Cart cart = getOrCreateCart(user);
        List<CartItem> cartItems = cart.getCartItems();

        List<CartItemDto> cartItemDtos = cartItems.stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            CartItemDto dto = new CartItemDto();

            double priceMultiplier = 1.0;
            if (cartItem.getWeight() == 300) { // 半斤
                priceMultiplier = 0.5;
            } else if (cartItem.getWeight() == 150) { // 四兩
                priceMultiplier = 0.25;
            }

            int adjustedPrice = (int) Math.round(product.getPrice() * priceMultiplier);

            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setImageUrl(product.getImageUrl());
            dto.setCategory(product.getCategory());
            dto.setPrice(adjustedPrice);
            dto.setStock(product.getStock());
            dto.setQuantity(cartItem.getQuantity());
            dto.setWeight(cartItem.getWeight());
            dto.setSubtotal(adjustedPrice * cartItem.getQuantity());
            return dto;
        }).collect(Collectors.toList());

        CartDto cartDto = new CartDto();
        cartDto.setCartId(cart.getCartId());
        cartDto.setUserId(user.getUserId());
        cartDto.setCartItems(cartItemDtos);
        cartDto.setTotalAmount(cartItemDtos.stream().mapToInt(CartItemDto::getSubtotal).sum());

        return cartDto;
    }
}