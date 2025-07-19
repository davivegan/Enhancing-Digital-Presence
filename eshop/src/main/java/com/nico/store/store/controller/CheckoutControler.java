package com.nico.store.store.controller;

import com.nico.store.store.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nico.store.store.domain.Address;
import com.nico.store.store.domain.Order;
import com.nico.store.store.domain.Payment;
import com.nico.store.store.domain.Shipping;
import com.nico.store.store.domain.ShoppingCart;
import com.nico.store.store.domain.User;
import com.nico.store.store.service.OrderService;
import com.nico.store.store.service.ShoppingCartService;

import javax.mail.MessagingException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CheckoutControler {
	@Autowired
	private ShoppingCartService shoppingCartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private MailService emailService;


	@RequestMapping("/checkout")
	public String checkout( @RequestParam(value="missingRequiredField", required=false) boolean missingRequiredField,
							Model model, Authentication authentication) {		
		User user = (User) authentication.getPrincipal();	
		ShoppingCart shoppingCart = shoppingCartService.getShoppingCart(user);
		if(shoppingCart.isEmpty()) {
			model.addAttribute("emptyCart", true);
			return "redirect:/shopping-cart/cart";
		}						
		model.addAttribute("cartItemList", shoppingCart.getCartItems());
		model.addAttribute("shoppingCart", shoppingCart);
		if(missingRequiredField) {
			model.addAttribute("missingRequiredField", true);
		}		
		return "checkout";		
	}
	
//	@RequestMapping(value = "/checkout", method = RequestMethod.POST)
//	public String placeOrder(@ModelAttribute("shipping") Shipping shipping,
//							@ModelAttribute("address") Address address,
//							@ModelAttribute("payment") Payment payment,
//							RedirectAttributes redirectAttributes, Authentication authentication) {
//		User user = (User) authentication.getPrincipal();
//		ShoppingCart shoppingCart = shoppingCartService.getShoppingCart(user);
//		if (!shoppingCart.isEmpty()) {
//			shipping.setAddress(address);
//			Order order = orderService.createOrder(shoppingCart, shipping, payment, user);
//			redirectAttributes.addFlashAttribute("order", order);
//		}
//		return "redirect:/order-submitted";
//	}




	// New for mail sending the order details


	@RequestMapping(value = "/checkout", method = RequestMethod.POST)
	public String placeOrder(@ModelAttribute("shipping") Shipping shipping,
							 @ModelAttribute("address") Address address,
							 @ModelAttribute("payment") Payment payment,
							 RedirectAttributes redirectAttributes, Authentication authentication) {

		User user = (User) authentication.getPrincipal();
		ShoppingCart shoppingCart = shoppingCartService.getShoppingCart(user);

		if (!shoppingCart.isEmpty()) {
			shipping.setAddress(address);
			Order order = orderService.createOrder(shoppingCart, shipping, payment, user);

			// Get order details for email (now includes quantity)
			List<String> orderItems = shoppingCart.getCartItems().stream()
					.map(item -> item.getQty() + " x " + item.getArticle().getTitle() + " - ₹" + item.getArticle().getPrice())
					.collect(Collectors.toList());

			String orderTotal = "₹" + shoppingCart.getGrandTotal();
			String shippingAddress = address.getStreetAddress() + " " + address.getCity() +" " + address.getCountry() +" " + address.getZipCode();
			String paymentMethod = payment.getType();
			String trackingUrl = "https://core7.com/track/" + order.getId();
			String companyLogo = "https://www.core7.co.uk/wp-content/uploads/2017/11/Logo-7-Orange-768x522.png"; // Corrected logo URL

			try {
				// Send order confirmation email
				emailService.sendOrderConfirmationEmail(user.getEmail(), user.getUsername(),
						String.valueOf(order.getId()), orderItems,
						orderTotal, shippingAddress, paymentMethod,
						trackingUrl, companyLogo);
				System.out.println("Order confirmation email sent successfully.");
			} catch (MessagingException e) {
				e.printStackTrace();
				System.err.println("Failed to send order confirmation email: " + e.getMessage());
				redirectAttributes.addFlashAttribute("emailError", "Order placed, but email failed to send.");
			}

			redirectAttributes.addFlashAttribute("order", order);
		}

		return "redirect:/order-submitted";
	}



	@RequestMapping(value = "/order-submitted", method = RequestMethod.GET)
	public String orderSubmitted(Model model) {
		Order order = (Order) model.asMap().get("order");
		if (order == null) {
			return "redirect:/";
		}
		model.addAttribute("order", order);
		return "orderSubmitted";	
	}

}
