package com.example.expense_tracker.repository;
import com.example.expense_tracker.entity.Expense;
import com.example.expense_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.expense_tracker.entity.User;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUser(User user);
    List<Expense> findByDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );
    List<Expense> findByTitleContainingIgnoreCase(String title);
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    Double getTotalExpense();

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.date = :date")
    Double getTodayExpense(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE MONTH(e.date) = :month AND YEAR(e.date) = :year")
    Double getMonthlyExpense(@Param("month") int month, @Param("year") int year);

    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM Expense e GROUP BY e.category")
    List<Object[]> getExpenseByCategory();

    @Query("SELECT e FROM Expense e WHERE " +
            "(:category = 'All' OR e.category = :category) AND " +
            "(:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    List<Expense> filterExpenses(
            @Param("category") String category,
            @Param("title") String title


    );
    @Query("""
       SELECT e.category, SUM(e.amount)
       FROM Expense e
       WHERE e.user = :user
       GROUP BY e.category
       """)
    List<Object[]> getCategorySummary(
            @Param("user") User user
    );


}