package com.chorricode.junit5.controllers;

import com.chorricode.junit5.dtos.TransferDTO;
import com.chorricode.junit5.models.Account;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerWebTestClientTest {
    @Autowired
    protected WebTestClient webTestClient;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Order(1)
    @Test
    void testAccountTransfer() throws JsonProcessingException {
        // GIVEN
        TransferDTO transferDTO = new TransferDTO(1L, 2L,new BigDecimal("100"), 1L);
        final Map<String, Object> responseMap = Map.of(
                "date", LocalDate.now().toString(),
                "status", "ok",
                "message", "Transfer ok",
                "transfer", transferDTO
        );
        //WHEN
        webTestClient.post().uri("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transferDTO)
                .exchange()
        // THEN
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(response -> {
                    try {
                        JsonNode jsonResponse = objectMapper.readTree(response.getResponseBody());
                        assertEquals("Transfer ok", jsonResponse.path("message").asText());
                        assertEquals(1L, jsonResponse.path("transfer").path("sourceAccountId").asLong());
                        assertEquals(LocalDate.now().toString(), jsonResponse.path("date").asText());
                        assertEquals("100", jsonResponse.path("transfer").path("amount").asText());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.message").value(Matchers.is("Transfer ok"))
                .jsonPath("$.message").value(message -> assertEquals("Transfer ok", message))
                .jsonPath("$.message").isEqualTo("Transfer ok")
                .jsonPath("$.transfer.sourceAccountId").isEqualTo(transferDTO.getSourceAccountId())
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString())
                .json(objectMapper.writeValueAsString(responseMap));
    }

    @Order(2)
    @Test
    void testGeAccountById() throws JsonProcessingException {
        Account accountExpected = new Account(1L, "Andrés", new BigDecimal("900"));
        webTestClient.get().uri("/api/accounts/1").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.personName").isEqualTo("Andrés")
                .jsonPath("$.balance").isEqualTo(900)
                .json(objectMapper.writeValueAsString(accountExpected));
    }
    @Order(3)
    @Test
    void testGetAccountByIdOther() {
        webTestClient.get().uri("/api/accounts/2").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Account.class)
                .consumeWith(response -> {
                    Account account = response.getResponseBody();
                    assertNotNull(account);
                    assertEquals("John", account.getPersonName());
                    assertEquals("2100.00", account.getBalance().toPlainString());
                });
    }

    @Order(4)
    @Test
    void testGeAccountAll() {
        webTestClient.get().uri("/api/accounts").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].personName").isEqualTo("Andrés")
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].balance").isEqualTo(900)
                .jsonPath("$[1].personName").isEqualTo("John")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].balance").isEqualTo(2100)
                .jsonPath("$").isArray()
                .jsonPath("$").value(Matchers.hasSize(2));
    }

    @Order(5)
    @Test
    void testGeAccountAllOther() {
        webTestClient.get().uri("/api/accounts").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Account.class)
                .consumeWith(response -> {
                    List<Account> accountList = response.getResponseBody();
                    assertNotNull(accountList);
                    assertEquals(2, accountList.size());
                    assertEquals(1L, accountList.get(0).getId());
                    assertEquals("Andrés", accountList.get(0).getPersonName());
                    assertEquals("900.0", accountList.get(0).getBalance().toPlainString());
                    assertEquals(2L, accountList.get(1).getId());
                    assertEquals("John", accountList.get(1).getPersonName());
                    assertEquals("2100.0", accountList.get(1).getBalance().toPlainString());
                })
                .hasSize(2)
                .value(Matchers.hasSize(2));
    }

    @Order(6)
    @Test
    void testAccountSave() {
        Account accountForSave = new Account(null, "Pepe", new BigDecimal("3000"));
        webTestClient.post().uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(accountForSave)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(3)
                .jsonPath("$.personName").isEqualTo("Pepe")
                .jsonPath("$.personName").value(Matchers.is("Pepe"))
                .jsonPath("$.balance").isEqualTo(3000);
    }

    @Order(7)
    @Test
    void testAccountSaveOther() {
        Account accountForSave = new Account(null, "Pepita", new BigDecimal("3500"));
        webTestClient.post().uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(accountForSave)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Account.class)
                .consumeWith(response -> {
                    Account account = response.getResponseBody();
                    assertNotNull(account);
                    assertEquals(4L, account.getId());
                    assertEquals("Pepita", account.getPersonName());
                    assertEquals("3500", account.getBalance().toPlainString());
                });
    }
}