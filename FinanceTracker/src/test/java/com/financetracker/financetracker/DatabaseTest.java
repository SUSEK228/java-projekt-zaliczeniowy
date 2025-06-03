package com.financetracker.financetracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {

    @BeforeEach
    void setUp() {
        Database.setLimit(100.0, "Dzienny");
        Database.injectTransactionList(List.of(
                new Transaction("Test", -90.0, LocalDate.now().toString(), "Jedzenie")
        ));
    }

    @Test
    void testLimitExceededTrue() {
        Transaction t = new Transaction("Nowy", -20.0, LocalDate.now().toString(), "Inne");
        assertTrue(Database.isLimitExceeded(t));
    }

    @Test
    void testLimitExceededFalse() {
        Transaction t = new Transaction("Tani", -5.0, LocalDate.now().toString(), "Transport");
        assertFalse(Database.isLimitExceeded(t));
    }
}
