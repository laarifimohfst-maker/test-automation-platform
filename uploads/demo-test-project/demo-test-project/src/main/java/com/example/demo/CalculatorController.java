package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping("/api/addition")
    public int addition(@RequestParam int a, @RequestParam int b) {
        return calculatorService.addition(a, b);
    }

    @GetMapping("/api/soustraction")
    public int soustraction(@RequestParam int a, @RequestParam int b) {
        return calculatorService.soustraction(a, b);
    }
}
