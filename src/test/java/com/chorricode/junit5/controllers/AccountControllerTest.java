package com.chorricode.junit5.controllers;

import com.chorricode.junit5.Data;
import com.chorricode.junit5.dtos.TransferDTO;
import com.chorricode.junit5.models.Account;
import com.chorricode.junit5.services.AccountService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetAccountById() throws Exception {
        // GIVEN
        when(accountService.findById(1L)).thenReturn(Data.createAccount001().orElseThrow());

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON))
        // THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.personName").value("Andrés"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value("1000"));
        verify(accountService).findById(1L);
    }

    @Test
    void testAccountTransfer() throws Exception {
        // GIVEN
        TransferDTO transferDTO = new TransferDTO(1L, 2L, new BigDecimal("100"), 1L);


        final Map<String, Object> response = Map.of(
                "date", LocalDate.now().toString(),
                "status", "ok",
                "message", "Transfer ok",
                "transfer", transferDTO
        );

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferDTO)))

        // THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.date").value(LocalDate.now().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Transfer ok"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transfer.sourceAccountId").value(transferDTO.getSourceAccountId()))
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(response)));

    }

    @Test
    void testAccounFindAll() throws Exception {
        // GIVEN
        List<Account> accountList = Arrays.asList(
                Data.createAccount001().orElseThrow(),
                Data.createAccount002().orElseThrow());
        when(accountService.findAll()).thenReturn(accountList);

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts").contentType(MediaType.APPLICATION_JSON))
        // THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].personName").value("Andrés"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].balance").value("1000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].personName").value("John"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].balance").value("2000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(accountList)));
        verify(accountService).findAll();
    }

    @Test
    void testAccountSave() throws Exception {
        // GIVEN
        Account account = new Account(null, "Pepe", new BigDecimal("3000.1234"));
        when(accountService.save(any())).then(invocationOnMock -> {
            Account accountMockSaved = invocationOnMock.getArgument(0);
            accountMockSaved.setId(3L);
            return accountMockSaved;
        });

        // WHEN
        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
        // THEN
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.personName", Matchers.is("Pepe")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance", Matchers.is(3000.1234)));
        verify(accountService).save(any());

    }

}