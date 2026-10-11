package com.support.chatbot.tools;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OrderTools {
    public record OrderInfo(String orderId, String status, String trackingNumber,
                            String estimatedDelivery, List<String> items) {}

    private final Map<String, OrderInfo> orders = Map.of(
            "BC-1001", new OrderInfo("BC-1001", "SHIPPED", "1Z999888777",
                    "Oct 14", List.of("Ceramic pour-over set")),
            "BC-1002", new OrderInfo("BC-1002", "PROCESSING", null,
                    "Oct 16", List.of("Linen throw blanket", "Soy candle")),
            "BC-1003", new OrderInfo("BC-1003", "DELIVERED", "1Z999888999",
                    "Oct 8", List.of("Walnut cutting board"))
    );

    @Tool(description = "Look up a BrightCart order by its order ID (format BC-XXXX). "
            + "Returns the order status, tracking number, estimated delivery date, and items. "
            + "Use this whenever the customer asks about a specific order.")
    public String getOrderStatus(String orderId) {
        OrderInfo order = orders.get(orderId.toUpperCase().trim());
        if (order == null) {
            return "No order found with ID " + orderId
                    + ". Order IDs start with BC- and are in the confirmation email.";
        }
        return "Order %s is %s. Items: %s. Estimated delivery: %s. Tracking: %s."
                .formatted(order.orderId(), order.status(),
                        String.join(", ", order.items()), order.estimatedDelivery(),
                        order.trackingNumber() == null ? "not assigned yet" : order.trackingNumber());
    }
}
