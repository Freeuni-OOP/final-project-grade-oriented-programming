package com.oop.web_project.persistence;

import com.oop.web_project.entities.Account;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    @Query("SELECT DISTINCT a FROM Account a " +
            "LEFT JOIN FETCH a.transactions " +
            "LEFT JOIN FETCH a.cards " +
            "LEFT JOIN FETCH a.customers c " +
            "LEFT JOIN FETCH c.accounts " +
            "WHERE a.id = :id")
    Optional<Account> findByIdWithDetails(@Param("id") Long id);

    List<Account> findAllByCustomersEmail(String email);

    List<Account> findAllByCustomersId(long id);

    Optional<Account> findByCardsId(long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findWithLockById(long id);

    boolean existsByIdAndIsActiveTrue(long accountId);

}
