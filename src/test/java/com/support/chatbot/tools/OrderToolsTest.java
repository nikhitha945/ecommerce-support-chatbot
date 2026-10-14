package com.support.chatbot.tools;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class OrderToolsTest {
    private final OrderTools orderTools = new OrderTools();

    @Test
    void knownOrderReturnsStatusAndTracking() {
        assertThat(orderTools.getOrderStatus("BC-1001")).contains("SHIPPED");
    }

    @Test
    void unknownOrderReturnsNotFound() {
        assertThat(orderTools.getOrderStatus("BC-9999")).contains("BC-9999").containsIgnoringCase("no order");
    }
}
