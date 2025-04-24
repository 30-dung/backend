package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.CartDTO;
import com.example.serversideclinet.dto.CartItemDTO;
import com.example.serversideclinet.model.Cart;
import com.example.serversideclinet.model.CartItem;
import com.example.serversideclinet.model.Product;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.repository.CartItemRepository;
import com.example.serversideclinet.repository.CartRepository;
import com.example.serversideclinet.repository.ProductRepository;
import com.example.serversideclinet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public CartDTO getCart(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
        return convertToDTO(cart);
    }

    public  CartDTO addToCart(Integer userId, Long productId, Integer quantity){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
        if(existingItem.isPresent()){
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity()  + quantity);
            cartItemRepository.save(item);
        }else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }
        return convertToDTO(cartRepository.save(cart));
    }

    public CartDTO removeFromCart(Integer userId, Long itemId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return convertToDTO(cartRepository.save(cart));
    }

    private CartDTO convertToDTO(Cart cart){
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getUserId());
        dto.setItems(cart.getItems().stream().map(item -> {
            CartItemDTO itemDTO = new CartItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductName(item.getProduct().getProductName());
            itemDTO.setPrice(item.getProduct().getPrice());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setImageUrl(item.getProduct().getImages().stream()
                    .filter(img -> img.getIsPrimary())
                    .findFirst()
                    .map(img -> img.getImageUrl())
                    .orElse(null));
            return itemDTO;
        }).collect(Collectors.toList()));
        dto.setTotalPrice(dto.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum());
        return dto;
    }
}
