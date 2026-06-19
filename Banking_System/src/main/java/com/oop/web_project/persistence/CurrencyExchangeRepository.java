package com.oop.web_project.persistence;

import com.oop.web_project.entities.Currency;
import com.oop.web_project.entities.CurrencyExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyExchangeRepository extends JpaRepository<CurrencyExchange, Long> {

    @Query("SELECT ce FROM CurrencyExchange ce " +
            "WHERE ce.from.code = :fromCode " +
            "AND ce.to.code = :toCode " +
            "ORDER BY ce.timeStamp DESC LIMIT 1")
    Optional<CurrencyExchange> findByCurrencyCodes(@Param("fromCode") String currencyCodeFrom,
                                                   @Param("toCode") String currencyCodeTo);
}
