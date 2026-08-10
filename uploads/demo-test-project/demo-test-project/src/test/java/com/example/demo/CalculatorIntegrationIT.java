package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * TEST D'INTEGRATION - nommage *IT.java
 * Execute par Failsafe, phase Maven "verify" -> commande : mvn verify -Dit.test=*IT,!*ApiIT
 * Demarre le contexte Spring complet (plus lent, teste l'integration des couches)
 */
@SpringBootTest
@AutoConfigureMockMvc
class CalculatorIntegrationIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testAdditionViaControleur() throws Exception {
        mockMvc.perform(get("/api/addition").param("a", "2").param("b", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}
