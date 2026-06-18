package com.oop.web_project.persistence;

import com.oop.web_project.entities.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT SUM(b.amount) FROM CardBalance b WHERE b.card.account.id = :accountId AND b.currency.code = :currencyCode")
    Optional<BigDecimal> getBalanceForAccount(long accountId, String currencyCode);

    List<Card> getAllByAccountId(long accountId);
}
