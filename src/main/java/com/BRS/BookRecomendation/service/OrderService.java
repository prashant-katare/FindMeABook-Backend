package com.BRS.BookRecomendation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.BRS.BookRecomendation.Entities.Cart;
import com.BRS.BookRecomendation.Entities.Order;
import com.BRS.BookRecomendation.Entities.OrderItem;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserBookService UserBookService;

    public Order placeOrder(String username) {
        UserInfo user = userInfoService.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found.");
        }

        List<Cart> cartItems = UserBookService.getUserCart(username);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        List<OrderItem> orderItems = cartItems.stream()
                .<OrderItem>map(item -> OrderItem.builder()
                        .bookId(item.getBook().getId())
                        .bookTitle(item.getBook().getTitle())
                        .quantity(item.getQuantity())
                        .price(item.getBook().getPrice())
                        .imageUrl(item.getBook().getImageUrl())
                        .build())
                .collect(Collectors.toList());

        Order order = Order.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .orderItems(orderItems)
                .totalPrice(orderItems.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum())
                .status("Confirmed")
                .build();

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUsername(String username) {
        return orderRepository.findByUsername(username);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
