package com.oop.web_project.persistence;

import com.oop.web_project.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM CardBalance b WHERE b.card.account.id = :accountId AND b.currency = :currencyCode")
    Optional<BigDecimal> getBalanceForAccount(long accountId, String currencyCode);

    Optional<List<Card>> getAllByAccountId(long accountId);
}
