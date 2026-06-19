package com.oop.web_project.persistence;

import com.oop.web_project.entities.CardBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;


public interface CardBalanceRepository extends JpaRepository<CardBalance, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CardBalance> findByCardIdAndCurrencyCode(long cardId, String currencyCode);

    List<CardBalance> findAllByCardId(long cardId);
}
