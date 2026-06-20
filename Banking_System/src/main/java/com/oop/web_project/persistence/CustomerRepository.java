package com.oop.web_project.persistence;

import com.oop.web_project.entities.Customer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> getCustomerByEmail(String email);

    List<Customer> getCustomersByAccounts_Id(long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Customer> findWithLockById(long id);
}
