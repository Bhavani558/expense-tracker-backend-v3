package com.example.expense_tracker.controller;

import com.example.expense_tracker.dto.BudgetRequest;
import com.example.expense_tracker.entity.Budget;
import com.example.expense_tracker.entity.Expense;
import com.example.expense_tracker.entity.User;
import com.example.expense_tracker.repository.BudgetRepository;
import com.example.expense_tracker.repository.ExpenseRepository;
import com.example.expense_tracker.repository.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budget")
@CrossOrigin(origins = "*")
public class BudgetController {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public BudgetController(
            BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository,
            UserRepository userRepository) {

        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    @PostMapping
    public Budget saveBudget(
            @RequestBody BudgetRequest request) {

        User user = getCurrentUser();

        Budget budget =
                budgetRepository
                        .findByUser(user)
                        .orElse(new Budget());

        budget.setUser(user);
        budget.setMonthlyBudget(
                request.getMonthlyBudget());

        return budgetRepository.save(budget);
    }

    @GetMapping("/status")
    public Map<String, Object> getBudgetStatus() {

        User user = getCurrentUser();

        Budget budget =
                budgetRepository
                        .findByUser(user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Budget not set"));

        List<Expense> expenses =
                expenseRepository.findByUser(user);

        double spent = 0;

        YearMonth currentMonth =
                YearMonth.now();

        for (Expense expense : expenses) {

            if (expense.getDate() != null &&
                    YearMonth.from(
                                    expense.getDate())
                            .equals(currentMonth)) {

                spent += expense.getAmount();
            }
        }

        double remaining =
                budget.getMonthlyBudget()
                        - spent;

        double percentageUsed =
                (spent /
                        budget.getMonthlyBudget())
                        * 100;

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "budget",
                budget.getMonthlyBudget());

        response.put(
                "spent",
                spent);

        response.put(
                "remaining",
                remaining);

        response.put(
                "percentageUsed",
                Math.round(
                        percentageUsed));

        if (percentageUsed >= 100) {

            response.put(
                    "alert",
                    "Budget exceeded");
        }
        else if (percentageUsed >= 80) {

            response.put(
                    "alert",
                    "Warning: 80% budget used");
        }
        else {

            response.put(
                    "alert",
                    "Budget is healthy");
        }

        return response;
    }


}