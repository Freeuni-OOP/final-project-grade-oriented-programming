package com.oop.web_project.aspect.activity;

import com.oop.web_project.annotations.ActivityCheckRequired;
import com.oop.web_project.entities.CheckActivityTarget;
import com.oop.web_project.exceptions.accountExceptions.AccountIsNotActiveException;
import com.oop.web_project.exceptions.cardExceptions.CardIsNotActiveException;
import com.oop.web_project.exceptions.customerExceptions.CustomerIsNotActiveException;
import com.oop.web_project.persistence.AccountRepository;
import com.oop.web_project.persistence.CardRepository;
import com.oop.web_project.persistence.CustomerRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.AnnotationException;
import org.springframework.stereotype.Component;


@Component
@Aspect
public class ActivityCheckAspect {

    private final CustomerRepository customerRepository;
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;

    public ActivityCheckAspect(CustomerRepository customerRepository, CardRepository cardRepository,
                               AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
    }

    @Before(value = "@annotation(activityCheckRequired) && args(id, ..)")
    public void checkEntityActivity(JoinPoint jp,
                                      long id,
                                      ActivityCheckRequired activityCheckRequired) {

        CheckActivityTarget checkActivityTarget = activityCheckRequired.checkActivityTarget();


        switch (checkActivityTarget) {
            case ACCOUNT -> {
                if (!accountRepository.existsByIdAndIsActiveTrue(id)) {
                    throw new AccountIsNotActiveException("This account is deactivated, access is forbidden!");
                }
            }
            case CARD -> {
                if (!cardRepository.existsByIdAndIsActiveTrue(id)) {
                    throw new CardIsNotActiveException("This card is deactivated, access is forbidden!");
                }
            }
            case CUSTOMER -> {
                if (!customerRepository.existsByIdAndIsActiveTrue(id)) {
                    throw new CustomerIsNotActiveException("This customer is deactivated, access is forbidden!");
                }
            }
            case null -> throw new IllegalArgumentException("Target cannot be null");
            default -> throw new AnnotationException("annotation requires first parameter to be long type!");
        }

    }

}
