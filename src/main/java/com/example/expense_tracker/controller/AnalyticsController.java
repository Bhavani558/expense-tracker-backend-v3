package com.example.expense_tracker.controller;

import com.example.expense_tracker.entity.Expense;
import com.example.expense_tracker.entity.User;
import com.example.expense_tracker.repository.ExpenseRepository;
import com.example.expense_tracker.repository.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.expense_tracker.dto.MonthlyTrendDTO;
import java.time.Month;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/expenses")
public class AnalyticsController {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public AnalyticsController(
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
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    @GetMapping("/summary")
    public Map<String, Double> getExpenseSummary() {

        User currentUser = getCurrentUser();

        List<Expense> expenses =
                expenseRepository.findByUser(currentUser);

        Map<String, Double> summary = new HashMap<>();

        double total = 0;
        double today = 0;
        double thisMonth = 0;

        LocalDate todayDate = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        for (Expense e : expenses) {

            if (e.getDate() == null)
                continue;

            total += e.getAmount();

            if (e.getDate().isEqual(todayDate)) {
                today += e.getAmount();
            }

            if (YearMonth.from(e.getDate())
                    .equals(currentMonth)) {

                thisMonth += e.getAmount();
            }
        }

        summary.put("total", total);
        summary.put("today", today);
        summary.put("thisMonth", thisMonth);

        return summary;
    }

    @GetMapping("/category-summary")
    public Map<String, Double> getCategorySummary() {

        User currentUser = getCurrentUser();

        List<Expense> expenses =
                expenseRepository.findByUser(currentUser);

        Map<String, Double> categorySummary =
                new HashMap<>();

        for (Expense expense : expenses) {

            String category =
                    expense.getCategory();

            double amount =
                    expense.getAmount();

            categorySummary.put(
                    category,
                    categorySummary.getOrDefault(
                            category,
                            0.0
                    ) + amount
            );
        }

        return categorySummary;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats() {

        User currentUser = getCurrentUser();

        List<Expense> expenses =
                expenseRepository.findByUser(currentUser);

        double totalExpense = 0;
        double highestExpense = 0;

        int transactionCount =
                expenses.size();

        for (Expense expense : expenses) {

            totalExpense +=
                    expense.getAmount();

            if (expense.getAmount()
                    > highestExpense) {

                highestExpense =
                        expense.getAmount();
            }
        }

        double averageExpense = 0;

        if (transactionCount > 0) {

            averageExpense =
                    totalExpense /
                            transactionCount;
        }

        Map<String, Object> dashboard =
                new HashMap<>();

        dashboard.put(
                "totalExpense",
                totalExpense
        );

        dashboard.put(
                "highestExpense",
                highestExpense
        );

        dashboard.put("averageExpense",
                averageExpense
        );

        dashboard.put(
                "transactionCount",
                transactionCount
        );

        return dashboard;
    }
    @GetMapping("/budget-check")
    public Map<String, Object> checkMonthlyBudget(
            @RequestParam Double budget) {

        User currentUser = getCurrentUser();

        List<Expense> expenses =
                expenseRepository.findByUser(currentUser);

        LocalDate now = LocalDate.now();
        YearMonth currentMonth =
                YearMonth.from(now);

        double spentThisMonth = 0;

        for (Expense expense : expenses) {

            if (expense.getDate() != null &&
                    YearMonth.from(expense.getDate())
                            .equals(currentMonth)) {

                spentThisMonth +=
                        expense.getAmount();
            }
        }

        Map<String, Object> response =
                new HashMap<>();

        response.put("budget", budget);
        response.put("spent", spentThisMonth);

        if (spentThisMonth > budget) {

            response.put(
                    "status",
                    "LIMIT_EXCEEDED"
            );

            response.put(
                    "overBy",
                    spentThisMonth - budget
            );

        } else {

            response.put(
                    "status",
                    "WITHIN_LIMIT"
            );

            response.put(
                    "remaining",
                    budget - spentThisMonth
            );
        }

        return response;
    }


    @GetMapping("/monthly-trend")
    public List<MonthlyTrendDTO> getMonthlyTrend() {

        User currentUser = getCurrentUser();

        List<Expense> expenses =
                expenseRepository.findByUser(currentUser);

        Map<Integer, Double> monthlyTotals =
                new HashMap<>();

        for (Expense expense : expenses) {

            if (expense.getDate() == null)
                continue;

            int month =
                    expense.getDate()
                            .getMonthValue();

            monthlyTotals.put(
                    month,
                    monthlyTotals.getOrDefault(
                            month,
                            0.0
                    ) + expense.getAmount()
            );
        }

        List<MonthlyTrendDTO> trend =
                new ArrayList<>();

        for (int month = 1; month <= 12; month++) {

            trend.add(
                    new MonthlyTrendDTO(
                            Month.of(month)
                                    .name()
                                    .substring(0, 3),
                            monthlyTotals.getOrDefault(
                                    month,
                                    0.0
                            )
                    )
            );
        }

        return trend;
    }
}