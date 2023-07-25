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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerRestTemplateTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

    }

    private String uriCreate(String uri) {
        return "http://localhost:" + port + uri;
    }

    @Sql(scripts = {"classpath:Clean_DDBB.sql"})
    @Order(1)
    @Test
    void testAccountTransfer() throws JsonProcessingException {
        TransferDTO transferDTO = new TransferDTO(1L, 2L,new BigDecimal("100"), 1L);
        ResponseEntity<String> responseEntity = this.testRestTemplate.postForEntity(this.uriCreate("/api/accounts/transfer"), transferDTO, String.class);
        String json = responseEntity.getBody();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(json);
        assertTrue(json.contains("Transfer ok"));

        JsonNode jsonNode = this.objectMapper.readTree(json);
        assertEquals("Transfer ok", jsonNode.path("message").asText());
        assertEquals(LocalDate.now().toString(), jsonNode.path("date").asText());
        assertEquals("100", jsonNode.path("transfer").path("amount").asText());
        assertEquals(1L, jsonNode.path("transfer").path("sourceAccountId").asLong());
    }
    @Order(2)
    @Test
    void testGeAccountById() {
        Account accountExpected = new Account(1L, "Andrés", new BigDecimal("900.00"));
        ResponseEntity<Account> responseEntity = this.testRestTemplate.getForEntity(this.uriCreate("/api/accounts/1"), Account.class);
        Account account = responseEntity.getBody();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(account);
        assertEquals(1L, account.getId());
        assertEquals("Andrés", account.getPersonName());
        assertEquals("900.00", account.getBalance().toPlainString());
        assertEquals(accountExpected, account);
    }
    @Order(3)
    @Test
    void testGeAccountAll() throws JsonProcessingException {
        ResponseEntity<Account[]> responseEntity = this.testRestTemplate.getForEntity(this.uriCreate("/api/accounts"), Account[].class);
        List<Account> accountList = Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        assertEquals(1L, accountList.get(0).getId());
        assertEquals("Andrés", accountList.get(0).getPersonName());
        assertEquals("900.00", accountList.get(0).getBalance().toPlainString());
        assertEquals(2L, accountList.get(1).getId());
        assertEquals("John", accountList.get(1).getPersonName());
        assertEquals("2100.00", accountList.get(1).getBalance().toPlainString());

        JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(accountList));
        assertEquals(1L, jsonNode.get(0).path("id").asLong());
        assertEquals("Andrés", jsonNode.get(0).path("personName").asText());
        assertEquals("900.0", jsonNode.get(0).path("balance").asText());
        assertEquals(2L, jsonNode.get(1).path("id").asLong());
        assertEquals("John", jsonNode.get(1).path("personName").asText());
        assertEquals("2100.0", jsonNode.get(1).path("balance").asText());
    }

    @Order(4)
    @Test
    void testAccountSave() {
        Account accountForSave = new Account(null, "Pepe", new BigDecimal("3000"));

        ResponseEntity<Account> responseEntity = testRestTemplate.postForEntity(this.uriCreate("/api/accounts"), accountForSave, Account.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        Account accountSaved = responseEntity.getBody();
        assertNotNull(accountSaved);
        assertEquals(3L, accountSaved.getId());
        assertEquals(accountForSave.getPersonName(), accountSaved.getPersonName());
        assertEquals(accountForSave.getBalance(), accountSaved.getBalance());
    }

    @Order(5)
    @Test
    void testAccountDeleteById() {
        ResponseEntity<Account[]> accountsResponseEntity = this.testRestTemplate.getForEntity(this.uriCreate("/api/accounts"), Account[].class);
        List<Account> accountList = Arrays.asList(Objects.requireNonNull(accountsResponseEntity.getBody()));

        assertEquals(HttpStatus.OK, accountsResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, accountsResponseEntity.getHeaders().getContentType());
        assertEquals(3, accountList.size());

        //testRestTemplate.delete(this.uriCreate("/api/accounts/3"));
        ResponseEntity<Void> voidResponseEntity = testRestTemplate.exchange(this.uriCreate("/api/accounts/{id}"), HttpMethod.DELETE, null, Void.class, Map.of("id", 3L));
                assertEquals(HttpStatus.NO_CONTENT, voidResponseEntity.getStatusCode());
                assertFalse(voidResponseEntity.hasBody());

        accountsResponseEntity = this.testRestTemplate.getForEntity(this.uriCreate("/api/accounts"), Account[].class);
        accountList = Arrays.asList(Objects.requireNonNull(accountsResponseEntity.getBody()));
        assertEquals(2, accountList.size());

        ResponseEntity<Account> accountResponseEntity = this.testRestTemplate.getForEntity(this.uriCreate("/api/accounts/3"),Account.class);
        assertEquals(HttpStatus.NOT_FOUND, accountResponseEntity.getStatusCode());
        assertFalse(accountResponseEntity.hasBody());
    }
}