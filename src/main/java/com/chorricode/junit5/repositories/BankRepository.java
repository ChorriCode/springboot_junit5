package com.chorricode.junit5.repositories;

import com.chorricode.junit5.models.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, Long> {
    /*List<Bank> findAll();
    Bank findById(Long id);
    void save(Bank bank);*/
}
