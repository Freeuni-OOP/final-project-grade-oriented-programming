package com.oop.web_project.persistence;

import com.oop.web_project.entities.Customer;
import com.oop.web_project.entities.Role;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> getCustomerByEmail(String email);

    List<Customer> getCustomersByAccounts_Id(long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Customer> findWithLockById(long id);

    boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Account a " +
            "JOIN a.customers c " +
            "JOIN a.cards card " +
            "WHERE c.email = :email AND card.id = :cardId")
    boolean customerWithEmailOwnsCard(@Param("email") String email, @Param("cardId") Long cardId);

    boolean existsByEmailAndAccountsId(String email, Long accountId);

    boolean existsByEmailAndId(String email, Long customerId);

    boolean existsCustomerByAccounts_Id(long accountId);

    boolean existsByEmailAndRole(String email, Role role);

    boolean existsByIdAndIsActiveTrue(long customerId);
}
