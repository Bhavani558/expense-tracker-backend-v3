package com.example.expense_tracker.controller;
import com.example.expense_tracker.entity.Expense;
import com.example.expense_tracker.entity.User;
import com.example.expense_tracker.repository.ExpenseRepository;
import com.example.expense_tracker.repository.UserRepository;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ReportController(
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

    @GetMapping("/pdf")
    public void generatePdfReport(
            HttpServletResponse response)
            throws IOException, DocumentException {

        response.setContentType(
                "application/pdf"
        );

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=expense-report.pdf"
        );

        User currentUser =
                getCurrentUser();

        List<Expense> expenses =
                expenseRepository.findByUser(currentUser);

        Document document =
                new Document();

        PdfWriter.getInstance(
                document,
                response.getOutputStream()
        );

        document.open();

        Font titleFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        18
                );

        Paragraph title =
                new Paragraph(
                        "Expense Report",
                        titleFont
                );

        title.setAlignment(
                Element.ALIGN_CENTER
        );

        document.add(title);

        document.add(
                new Paragraph(" ")
        );

        document.add(
                new Paragraph(
                        "User: "
                                + currentUser.getEmail()
                )
        );

        document.add(
                new Paragraph(" ")
        );

        PdfPTable table =
                new PdfPTable(4);

        table.setWidthPercentage(100);

        table.addCell("Title");
        table.addCell("Amount");
        table.addCell("Category");
        table.addCell("Date");

        double totalExpense = 0;

        for (Expense expense : expenses) {

            table.addCell(
                    expense.getTitle()
            );

            table.addCell(
                    String.valueOf(
                            expense.getAmount()
                    )
            );

            table.addCell(
                    expense.getCategory()
            );

            table.addCell(
                    String.valueOf(
                            expense.getDate()
                    )
            );

            totalExpense +=
                    expense.getAmount();
        }

        document.add(table);

        document.add(
                new Paragraph(" ")
        );

        document.add(
                new Paragraph(
                        "Total Expense: ₹"
                                + totalExpense
                )
        );

        document.close();
    }
}
