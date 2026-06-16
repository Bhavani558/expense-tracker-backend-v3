package com.example.expense_tracker.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Setter
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Setter
    private String role = "USER";

    @OneToMany(mappedBy = "user")
    private List<Expense> expenses;

    public User() {
    }

}