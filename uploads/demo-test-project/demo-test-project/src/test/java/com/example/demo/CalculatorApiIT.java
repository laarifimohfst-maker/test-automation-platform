package com.example.demo;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/*
 * TEST API - nommage *ApiIT.java
 * Execute par Failsafe, phase Maven "verify" -> commande : mvn verify -Dit.test=*ApiIT
 * Demarre un vrai serveur HTTP sur un port aleatoire, teste l'API comme un vrai client (Rest Assured)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CalculatorApiIT {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void testAdditionViaApi() {
        given()
                .queryParam("a", 10)
                .queryParam("b", 5)
            .when()
                .get("/api/addition")
            .then()
                .statusCode(200)
                .body(equalTo("15"));
    }
}
