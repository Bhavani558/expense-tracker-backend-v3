package com.example.expense_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String title;

    @Setter
    @Column(nullable = false)
    private double amount;

    @Setter
    private String description;

    @Setter
    @Column(nullable = false)
    private String category;

    @Setter
    @Column(nullable = false)
    private LocalDate date;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public Expense() {
    }

    public Expense(String title, double amount,
                   String description,
                   String category,
                   LocalDate date) {

        this.title = title;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public User getUser() {
        return user;
    }

}
