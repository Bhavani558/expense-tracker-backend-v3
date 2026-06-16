package com.example.expense_tracker.controller;
import com.example.expense_tracker.entity.Expense;
import com.example.expense_tracker.repository.ExpenseRepository;
import com.example.expense_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.expense_tracker.entity.User;
import java.util.LinkedHashMap;
import java.util.List;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;


@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public Expense addExpense(
            @RequestBody Expense expense) {

        User currentUser = getCurrentUser();

        expense.setUser(currentUser);

        return expenseRepository.save(expense);
    }

    @GetMapping
    public List<Expense> getAllExpenses() {

        User currentUser = getCurrentUser();

        return expenseRepository.findByUser(currentUser);
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        expenseRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public Expense updateExpense(@PathVariable Long id, @RequestBody Expense updatedExpense) {

        Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id " + id));

        existingExpense.setTitle(updatedExpense.getTitle());
        existingExpense.setAmount(updatedExpense.getAmount());
        existingExpense.setDescription(updatedExpense.getDescription());
        existingExpense.setCategory(updatedExpense.getCategory());
        existingExpense.setDate(updatedExpense.getDate());

        return expenseRepository.save(existingExpense);
    }
    @GetMapping("/summary-expense")
    public Map<String, Double> getExpenseSummary() {
        Map<String, Double> summary = new HashMap<>();

        User currentUser = getCurrentUser();

        List<Expense> expenses = expenseRepository.findByUser(currentUser);

        double total = 0;
        double today = 0;
        double thisMonth = 0;

        LocalDate todayDate = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        for (Expense e : expenses) {
            if (e.getDate() == null) continue; // ✅ only date check

            total += e.getAmount();

            if (e.getDate().isEqual(todayDate)) {
                today += e.getAmount();
            }

            if (YearMonth.from(e.getDate()).equals(currentMonth)) {
                thisMonth += e.getAmount();
            }
        }

        summary.put("total", total);
        summary.put("today", today);
        summary.put("thisMonth", thisMonth);

        return summary;
    }
    @GetMapping("/filter")
    public List<Expense> filterExpenses(
            @RequestParam(defaultValue = "All") String category,
            @RequestParam(required = false) String title) {

        return expenseRepository.filterExpenses(category, title);
    }
    @GetMapping("/test")
    public String testEndpoint() {
        return "Backend is live!";
    }

    private User getCurrentUser() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow();
    }

    @GetMapping("/filter-date")
    public List<Expense> filterByDate(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {

        User currentUser = getCurrentUser();

        return expenseRepository.findByUser(currentUser)
                .stream()
                .filter(expense ->
                        !expense.getDate().isBefore(start)
                                && !expense.getDate().isAfter(end))
                .toList();
    }

    @GetMapping("/search")
    public List<Expense> searchExpense(
            @RequestParam String keyword) {

        User currentUser = getCurrentUser();

        return expenseRepository.findByUser(currentUser)
                .stream()
                .filter(expense ->
                        expense.getTitle()
                                .toLowerCase()
                                .contains(
                                        keyword.toLowerCase()
                                ))
                .toList();
    }

    @GetMapping("/advanced-filter")
    public List<Expense> advancedFilter(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {

        User currentUser = getCurrentUser();

        return expenseRepository.findByUser(currentUser)
                .stream()
                .filter(expense ->
                        category == null ||
                                expense.getCategory()
                                        .equalsIgnoreCase(category))
                .filter(expense ->
                        keyword == null ||
                                expense.getTitle()
                                        .toLowerCase()
                                        .contains(
                                                keyword.toLowerCase()
                                        ))
                .filter(expense ->
                        start == null ||
                                !expense.getDate().isBefore(start))
                .filter(expense ->
                        end == null ||
                                !expense.getDate().isAfter(end))
                .toList();
    }

}


