package com.example.expense_tracker.controller;

import com.example.expense_tracker.entity.Expense;
import com.example.expense_tracker.entity.User;
import com.example.expense_tracker.repository.ExpenseRepository;
import com.example.expense_tracker.repository.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/insights")
@CrossOrigin(origins = "*")
public class InsightsController {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public InsightsController(
            ExpenseRepository expenseRepository,
            UserRepository userRepository) {

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
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );
    }

    @GetMapping
    public Map<String, Object> getInsights() {

        User currentUser = getCurrentUser();

        List<Expense> expenses =
                expenseRepository.findByUser(currentUser);

        Map<String, Object> insights =
                new HashMap<>();

        if (expenses.isEmpty()) {

            insights.put(
                    "message",
                    "No expenses found"
            );

            return insights;
        }

        double total = 0;

        Expense highestExpense =
                expenses.get(0);

        Map<String, Double> categoryTotals =
                new HashMap<>();

        for (Expense expense : expenses) {

            total += expense.getAmount();

            if (expense.getAmount() >
                    highestExpense.getAmount()) {

                highestExpense = expense;
            }

            categoryTotals.put(
                    expense.getCategory(),
                    categoryTotals.getOrDefault(
                            expense.getCategory(),
                            0.0
                    ) + expense.getAmount()
            );
        }

        double average =
                total / expenses.size();

        String topCategory = "";

        double maxCategoryAmount = 0;

        for (Map.Entry<String, Double> entry :
                categoryTotals.entrySet()) {

            if (entry.getValue() >
                    maxCategoryAmount) {

                maxCategoryAmount =
                        entry.getValue();

                topCategory =
                        entry.getKey();
            }
        }

        String suggestion;

        switch (topCategory.toLowerCase()) {

            case "shopping":
                suggestion =
                        "Most spending is on Shopping. Consider reducing discretionary purchases.";
                break;

            case "food":
                suggestion =
                        "Food expenses are high. Consider meal planning.";
                break;

            case "transport":
                suggestion =
                        "Transport costs are high. Consider shared travel options.";
                break;

            default:
                suggestion =
                        "Your spending looks balanced.";
        }

        insights.put(
                "highestExpense",
                highestExpense.getAmount()
        );

        insights.put(
                "highestExpenseTitle",
                highestExpense.getTitle()
        );

        insights.put(
                "averageExpense",
                Math.round(
                        average * 100.0
                ) / 100.0
        );

        insights.put(
                "topCategory",
                topCategory
        );

        insights.put(
                "totalTransactions",
                expenses.size()
        );

        insights.put(
                "suggestion",
                suggestion
        );

        return insights;
    }
}