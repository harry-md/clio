package com.harry.clio.dto.stats;

public enum RevenuePeriodType {
    MONTH(12, 1, "day", "Tháng"),
    QUARTER(4, 3, "month", "Quý");

    private final int maxPeriod;
    private final int monthsPerPeriod;
    private final String bucketUnit;
    private final String displayName;

    RevenuePeriodType(int maxPeriod, int monthsPerPeriod, String bucketUnit, String displayName) {
        this.maxPeriod = maxPeriod;
        this.monthsPerPeriod = monthsPerPeriod;
        this.bucketUnit = bucketUnit;
        this.displayName = displayName;
    }

    public int maxPeriod() {
        return maxPeriod;
    }

    public int monthsPerPeriod() {
        return monthsPerPeriod;
    }

    public String bucketUnit() {
        return bucketUnit;
    }

    public String displayName() {
        return displayName;
    }

    public int firstMonth(int period) {
        return (period - 1) * monthsPerPeriod + 1;
    }

    public boolean isMonth() {
        return this == MONTH;
    }
}
