package com.chorricode.junit5.controllers;

import com.chorricode.junit5.dtos.TransferDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Order(1)
    @Test
    void testAccountTransfer() throws JsonProcessingException {
        TransferDTO transferDTO = new TransferDTO(2L, 1L,new BigDecimal("100"), 1L);
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
        assertEquals(2L, jsonNode.path("transfer").path("sourceAccountId").asLong());
    }
}