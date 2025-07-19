package com.nico.store.store.controller;

import com.nico.store.store.domain.Order;
import com.nico.store.store.domain.User;
import com.nico.store.store.repository.OrderRepository;
import com.nico.store.store.service.OrderService;
import com.nico.store.store.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderRepository orderRepository;

    // Show all orders for admin
    @GetMapping
    public String listAllOrders(Model model) {
        List<Order> orders = orderService.findAllOrders();
        model.addAttribute("orders", orders);
        return "admin/manage-orders";  // Renders the Thymeleaf template
    }

    // Show a specific order for a user
    @GetMapping("/{userId}")
    public String listUserOrders(@PathVariable Long userId, Model model) {
        User user = userService.findById(userId);
        if (user == null) {
            return "redirect:/admin/orders";
        }
        List<Order> userOrders = orderService.findByUser(user);
        model.addAttribute("orders", userOrders);
        model.addAttribute("user", user);
        return "admin/user-orders";  // New Thymeleaf template for user-specific orders
    }

    // Update order for a specific user
    @PostMapping("/update/{orderId}")
    public String updateUserOrder(@PathVariable Long orderId,
                                  @RequestParam("status") String status,
                                  @RequestParam("shippingDate") String shippingDate) {
        Order order = orderService.findOrderWithDetails(orderId);
        if (order != null) {
            order.setOrderStatus(status);
            order.setShippingDate(java.sql.Date.valueOf(shippingDate));
            orderRepository.save(order);
        }
        return "redirect:/admin/orders";
    }



}
