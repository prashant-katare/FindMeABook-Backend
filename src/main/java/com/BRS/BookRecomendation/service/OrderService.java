package com.BRS.BookRecomendation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BRS.BookRecomendation.Entities.Book;
import com.BRS.BookRecomendation.Entities.Cart;
import com.BRS.BookRecomendation.Entities.Order;
import com.BRS.BookRecomendation.Entities.OrderItem;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.repository.BookRepository;
import com.BRS.BookRecomendation.repository.CartRepository;
import com.BRS.BookRecomendation.repository.OrderItemRepository;
import com.BRS.BookRecomendation.repository.OrderRepository;
import com.BRS.BookRecomendation.DTO.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Transactional
    public Order placeOrder(Long userId) {
        logger.info("Placing order for user: {}", userId);

        // Fetch User
        UserInfo user = userInfoService.getUserById(userId);
        if (user == null) {
            logger.error("User not found for user ID: {}", userId);
            throw new RuntimeException("User not found.");
        }
        logger.debug("Found user with ID: {}", user.getId());

        // Fetch Cart Items
        List<Cart> cartItems = cartService.getUserCart(userId);
        if (cartItems.isEmpty()) {
            logger.error("Cart is empty for user ID: {}", userId);
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }
        logger.debug("Found {} items in cart for user: {}", cartItems.size(), userId);

        // Check stock availability for all items BEFORE modifying anything
        for (Cart item : cartItems) {
            Book book = item.getBook();
            if (book.getStockQuantity() < item.getQuantity()) {
                logger.error("Insufficient stock for book: {} (stock: {}, requested: {})",
                        book.getTitle(), book.getStockQuantity(), item.getQuantity());
                throw new RuntimeException("Insufficient stock for book: " + book.getTitle());
            }
            logger.trace("Stock check passed for book: {} (available: {}, requested: {})",
                    book.getTitle(), book.getStockQuantity(), item.getQuantity());
        }
        logger.debug("Stock availability check passed for all items");

        // Create Order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Status.CONFIRMED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUsername(user.getUsername());
        
        List<OrderItem> orderItems = new ArrayList<>();
        
        for(Cart cart : cartItems) {     	
        	OrderItem item = new OrderItem();
        	Book book = bookRepository.getBookById(cart.getBook().getId());
        	
        	item.setBook(book);
        	item.setQuantity(cart.getQuantity());
        	item.setPrice(book.getPrice());
        	item.setImageUrl(book.getImageUrl());
        	item.setBookTitle(book.getTitle());
        	item.setOrder(order); 
        	
        	orderItems.add(item);
        }
        

//        // Create Order Items
//        List<OrderItem> orderItems = cartItems.stream()
//                .map(item -> {
//                    OrderItem orderItem = new OrderItem();
//                    orderItem.setBook(item.getBook());
//                    orderItem.setQuantity(item.getQuantity());
//                    orderItem.setPrice(item.getBook().getPrice());
//                    orderItem.setImageUrl(item.getBook().getImageUrl());
//                    orderItem.setOrder(order); // ✅ Associate orderItem with the order
//                    return orderItem;
//                })
//                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        // Calculate Total Price
        double totalPrice = orderItems.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        order.setTotalPrice(totalPrice);
        logger.debug("Order total price: ${}", totalPrice);

        // Update stock quantities **before saving order**
        for (Cart item : cartItems) {
            Book book = item.getBook();
            int newStock = book.getStockQuantity() - item.getQuantity();
            book.setStockQuantity(newStock);
            bookRepository.save(book);
            logger.debug("Updated stock for book: {} (new stock: {})", book.getTitle(), newStock);
        }
        logger.debug("Stock quantities updated for all books");

        // Save the Order (including its items)
        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with ID: {}", savedOrder.getId());

        // Clear the Cart
        cartRepository.deleteAll(cartItems);
        logger.debug("Cart cleared for user: {}", userId);

        logger.info("Order placement completed successfully for user: {}", userId);
        return savedOrder;
    }

    public List<Order> getOrdersByUserId(Long userId) {
        logger.info("Retrieving orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        logger.debug("Found {} orders for user: {}", orders.size(), userId);
        return orders;
    }

    public List<Order> getAllOrders() {
        logger.info("Retrieving all orders");
        List<Order> orders = orderRepository.findAll();
        logger.debug("Found {} total orders", orders.size());
        return orders;
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        logger.info("Cancelling order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Order not found");
                });
        logger.debug("Found order with ID: {} in status: {}", orderId, order.getStatus());

        // Only allow cancellation if order is in "Confirmed" status
        if (!"Confirmed".equals(order.getStatus())) {
            logger.error("Cannot cancel order in status: {}", order.getStatus());
            throw new RuntimeException("Cannot cancel order in status: " + order.getStatus());
        }

        // Update order status
        order.setStatus(Status.CANCELLED);
        logger.debug("Updated order status to Cancelled");

        // Restore stock quantities
        for (OrderItem item : order.getOrderItems()) {
            Book book = bookRepository.findById(item.getBook().getId())
                    .orElseThrow(() -> {
                        logger.error("Book not found with ID: {}", item.getBook().getId());
                        return new RuntimeException("Book not found: " + item.getBook().getId());
                    });

            int newStock = book.getStockQuantity() + item.getQuantity();
            book.setStockQuantity(newStock);
            bookRepository.save(book);
            logger.debug("Restored stock for book: {} (new stock: {})", book.getTitle(), newStock);
        }
        logger.debug("Stock quantities restored for all books in the order");

        Order savedOrder = orderRepository.save(order);
        logger.info("Order with ID: {} successfully cancelled", orderId);
        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        logger.info("Updating order status for order ID: {} to: {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Order not found");
                });
        logger.debug("Found order with ID: {} in current status: {}", orderId, order.getStatus());

        // Validate status
        if (!isValidStatus(status)) {
            logger.error("Invalid order status: {}", status);
            throw new RuntimeException("Invalid order status: " + status);
        }

        // If cancelling an order that was previously confirmed, restore stock
        if ("Cancelled".equals(status) && "Confirmed".equals(order.getStatus())) {
            logger.debug("Cancelling a confirmed order - restoring stock");
            for (OrderItem item : order.getOrderItems()) {
                Book book = bookRepository.findById(item.getBook().getId())
                        .orElseThrow(() -> {
                            logger.error("Book not found with ID: {}", item.getBook().getId());
                            return new RuntimeException("Book not found: " + item.getBook().getId());
                        });

                int newStock = book.getStockQuantity() + item.getQuantity();
                book.setStockQuantity(newStock);
                bookRepository.save(book);
                logger.debug("Restored stock for book: {} (new stock: {})", book.getTitle(), newStock);
            }
            logger.debug("Stock quantities restored for all books in the order");
        }

        order.setStatus(Status.valueOf(status));
        Order savedOrder = orderRepository.save(order);
        logger.info("Order status successfully updated to: {} for order ID: {}", status, orderId);
        return savedOrder;
    }

    private boolean isValidStatus(String status) {
        boolean isValid = List.of("Confirmed", "Processing", "Shipped", "Delivered", "Cancelled").contains(status);
        if (!isValid) {
            logger.warn("Attempted to set invalid order status: {}", status);
        }
        return isValid;
    }

    public void cancelAllOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        for (Order order : orders) {
            cancelOrder(order.getId());
        }
    }
}
