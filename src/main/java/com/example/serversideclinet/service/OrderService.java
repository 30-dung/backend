package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.OrderDTO;
import com.example.serversideclinet.dto.OrderItemDTO;
import com.example.serversideclinet.model.Cart;
import com.example.serversideclinet.model.Order;
import com.example.serversideclinet.model.OrderItem;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.repository.CartRepository;
import com.example.serversideclinet.repository.OrderItemRepository;
import com.example.serversideclinet.repository.OrderRepository;
import com.example.serversideclinet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public OrderDTO createOrder(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("cart not found"));
        if(cart.getItems().isEmpty()){
            throw new RuntimeException("cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum());
        for (var cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            order.getItems().add(orderItem);
        }

        orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);

        return convertToDTO(order);
    }

    private OrderDTO convertToDTO(Order order){
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getUserId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setItems(order.getItems().stream().map(item -> {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductName(item.getProduct().getProductName());
            itemDTO.setPrice(item.getPrice());
            itemDTO.setQuantity(item.getQuantity());
            return itemDTO;
        }).collect(Collectors.toList()));
        return dto;
    }
}
