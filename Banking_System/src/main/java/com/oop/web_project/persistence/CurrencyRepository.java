package com.oop.web_project.persistence;

import com.oop.web_project.entities.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Currency findCurrencyByCode(String code);
}
