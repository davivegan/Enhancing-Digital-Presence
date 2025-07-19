package com.nico.store.store.service.impl;
import com.razorpay.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {
    private static final String KEY_ID = "rzp_test_KTDMU61nkndJog";
    private static final String KEY_SECRET = "kwWPUSbVz8CpQShZ5HId9bjF";

    public String createOrder(double amount) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(KEY_ID, KEY_SECRET);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) (amount * 100)); // Amount in paisa
        orderRequest.put("currency", "INR");
        orderRequest.put("payment_capture", 1);

        Order order = client.Orders.create(orderRequest);
        return order.toString();
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String expectedSignature = Utils.getHash(orderId + "|" + paymentId, KEY_SECRET);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}