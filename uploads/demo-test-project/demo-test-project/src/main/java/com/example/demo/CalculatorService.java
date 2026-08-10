package com.example.demo;

import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    public int addition(int a, int b) {
        return a + b;
    }

    public int soustraction(int a, int b) {
        return a - b;
    }
}
