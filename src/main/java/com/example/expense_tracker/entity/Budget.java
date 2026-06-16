package com.example.expense_tracker.entity;
import jakarta.persistence.*;
import lombok.Setter;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private Double monthlyBudget;

    @Setter
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Budget() {
    }

    public Long getId() {
        return id;
    }

    public Double getMonthlyBudget() {
        return monthlyBudget;
    }

    public User getUser() {
        return user;
    }

}