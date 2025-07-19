package com.nico.store.store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    // ✅ Welcome Email
    public void sendWelcomeEmail(String to, String username) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Welcome to Core7 Store!");
        helper.setText(buildWelcomeEmail(username), true);
        mailSender.send(message);
    }

    // ✅ Order Confirmation Email
    public void sendOrderConfirmationEmail(String to, String username, String orderId,
                                           List<String> orderItems, String orderTotal,
                                           String shippingAddress, String paymentMethod,
                                           String trackingUrl, String companyLogo) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Your Order #" + orderId + " - Core7 Store");

        String emailContent = buildOrderConfirmationEmail(username, orderId, orderItems, orderTotal,
                shippingAddress, paymentMethod, trackingUrl, companyLogo);

        helper.setText(emailContent, true);
        mailSender.send(message);
    }

    // ✅ Generate Welcome Email Content
    private String buildWelcomeEmail(String username) {
        return "<html>"
                + "<body style='font-family:Arial, sans-serif; background-color:#e8f5e9; padding:20px;'>"
                + "<div style='max-width:600px; background:white; padding:20px; border-radius:12px; box-shadow:0px 4px 20px rgba(0,0,0,0.15); text-align:center;'>"
                + "<h2 style='color:#2e7d32;'>Welcome to Core7 Store, " + username + "!</h2>"
                + "<p>We are thrilled to have you with us.</p>"
                + "<a href='https://localhost:8080' style='display:inline-block; padding:12px 25px; margin-top:15px; background:#2e7d32; color:white; font-size:16px; text-decoration:none; border-radius:8px;'>Shop Now</a>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    // ✅ Generate Order Confirmation Email Content (✅ FIXED)
    private String buildOrderConfirmationEmail(String username, String orderId, List<String> orderItems,
                                               String orderTotal, String shippingAddress,
                                               String paymentMethod, String trackingUrl, String companyLogo) {
        // ✅ Building order items table dynamically
        StringBuilder itemsHtml = new StringBuilder();
        for (String item : orderItems) {
            itemsHtml.append("<tr>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #ddd;'>" + item + "</td>")
                    .append("</tr>");
        }

        return "<html><body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>"
                + "<div style='max-width: 600px; background: white; padding: 20px; border-radius: 8px; "
                + "box-shadow: 0px 0px 10px rgba(0,0,0,0.1); margin: auto;'>"
                + "<div style='text-align: center;'>"
                + "<img src='" + companyLogo + "' alt='Core7 Store Logo' style='max-width: 150px; margin-bottom: 20px;'>"
                + "<h2 style='color: #2e7d32;'>Thank You for Your Order, " + username + "!</h2>"
                + "<p>Your Order ID: <b>" + orderId + "</b></p>"

                // ✅ Order Items Table
                + "<table style='width: 100%; border-collapse: collapse; margin-top: 15px; text-align: left;'>"
                + "<thead>"
                + "<tr style='background-color: #4caf50; color: white;'>"
                + "<th style='padding: 10px;'>Item</th>"
                + "</tr>"
                + "</thead>"
                + "<tbody>"
                + itemsHtml.toString()
                + "</tbody>"
                + "</table>"

                + "<p style='margin-top: 15px; font-size: 18px;'><b>Total: " + orderTotal + "</b></p>"
                + "<p><b>Shipping Address:</b> " + shippingAddress + "</p>"
                + "<p><b>Payment Method:</b> " + paymentMethod + "</p>"

                // ✅ Track Order Button
                + "<p style='text-align:center; margin-top: 20px;'>"
                + "<a href='" + trackingUrl + "' style='display:inline-block; padding:10px 20px; background:#2e7d32; color:white; text-decoration:none; border-radius:5px;'>Track Your Order</a>"
                + "</p>"

                + "<p style='margin-top: 15px; font-size: 14px;'>If you have any questions, contact our support.</p>"
                + "<p>Best Regards,<br><b>Core7 Store Team</b></p>"
                + "</div></div></body></html>";
    }
}
