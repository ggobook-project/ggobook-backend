package com.untitled.ggobook.domain.enums;

// 2. 정지 기간
public enum SuspensionDuration {
    DAYS_3(3),
    DAYS_7(7),
    DAYS_14(14),
    DAYS_30(30),
    PERMANENT(9999);

    private final int days;

    SuspensionDuration(int days) { this.days = days; }

    public int getDays() { return days; }
}
