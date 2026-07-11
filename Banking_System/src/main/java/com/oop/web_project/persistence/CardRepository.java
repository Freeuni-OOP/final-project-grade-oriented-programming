package com.oop.web_project.persistence;

import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    @Query("""
    SELECT DISTINCT c FROM Card c
    LEFT JOIN FETCH c.balances
    WHERE c.id = :id
""")
    Optional<Card> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT SUM(b.amount) FROM CardBalance b WHERE b.card.account.id = :accountId AND b.currency.code = :currencyCode")
    Optional<BigDecimal> getBalanceForAccount(@Param("accountId")long accountId,
                                              @Param("currencyCode") String currencyCode);

    List<Card> getAllByAccountId(long accountId);

    boolean existsByPanToken(String panToken);

    boolean existsByPanMasked(String panMasked);
  
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Card> findWithLockById(long id);


    boolean existsByIdAndIsActiveTrue(long cardId);
}
