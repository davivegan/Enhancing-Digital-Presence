package com.nico.store.store.controller;

import com.nico.store.store.service.impl.RazorpayService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import com.razorpay.RazorpayException;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class RazorpayController {

    @Autowired
    private RazorpayService razorpayService;

    @PostMapping("/create-order")
    public ResponseEntity<String> createOrder(@RequestParam double amount) {
        try {
            return ResponseEntity.ok(razorpayService.createOrder(amount));
        } catch (RazorpayException e) {
            return ResponseEntity.badRequest().body("Error creating order");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody Map<String, String> data) {
        boolean isValid = razorpayService.verifySignature(
                data.get("razorpay_order_id"),
                data.get("razorpay_payment_id"),
                data.get("razorpay_signature")
        );

        if (isValid) {
            return ResponseEntity.ok("Payment Successful");
        } else {
            return ResponseEntity.badRequest().body("Payment Verification Failed");
        }
    }
}