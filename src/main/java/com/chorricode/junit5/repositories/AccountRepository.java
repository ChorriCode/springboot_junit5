package com.chorricode.junit5.repositories;


import com.chorricode.junit5.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    //@Query("SELECT a FROM Account a WHERE a.person_name = ?1")
    //Optional<Account> findByPersonNameQuery(String personName);

    Optional<Account> findByPersonName(String personName);

    /*List<Account> findAll();
    Account findById(Long id);
    void save(Account account);*/

}
