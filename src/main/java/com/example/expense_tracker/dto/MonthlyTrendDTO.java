package com.example.expense_tracker.dto;


public class MonthlyTrendDTO {

    private String month;
    private Double amount;

    public MonthlyTrendDTO(
            String month,
            Double amount) {

        this.month = month;
        this.amount = amount;
    }

    public String getMonth() {
        return month;
    }

    public Double getAmount() {
        return amount;
    }
}