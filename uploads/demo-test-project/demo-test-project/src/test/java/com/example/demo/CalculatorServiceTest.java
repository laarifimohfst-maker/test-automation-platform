package com.example.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * TEST UNITAIRE - nommage *Test.java
 * Execute par Surefire, phase Maven "test" -> commande : mvn test
 * Ne demarre PAS le contexte Spring (rapide, isole)
 */
class CalculatorServiceTest {

    private final CalculatorService calculatorService = new CalculatorService();

    @Test
    void testAddition() {
        assertEquals(4, calculatorService.addition(2, 2));
    }

    @Test
    void testSoustraction() {
        assertEquals(1, calculatorService.soustraction(3, 2));
    }
}
