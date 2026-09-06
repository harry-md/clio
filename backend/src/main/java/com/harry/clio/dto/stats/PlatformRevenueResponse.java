package com.harry.clio.dto.stats;

import java.math.BigDecimal;
import java.util.List;

public record PlatformRevenueResponse(
        String time,
        int period,
        int year,
        String label,
        BigDecimal totalRevenue,
        BigDecimal bookRevenue,
        BigDecimal subscriptionRevenue,
        List<RevenuePoint> points,
        List<TopBookRevenue> topBooks) {

    public record RevenuePoint(
            String label, BigDecimal bookRevenue, BigDecimal subscriptionRevenue) {

        public BigDecimal total() {
            return bookRevenue.add(subscriptionRevenue);
        }
    }

    public record TopBookRevenue(Integer bookId, String title, Long sales, BigDecimal revenue) {}
}
