package com.oop.web_project;

import com.oop.web_project.dto.responses.AccountSummaryResponse;
import com.oop.web_project.dto.responses.CardBalanceResponse;
import com.oop.web_project.dto.responses.CardResponse;
import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Card;
import com.oop.web_project.entities.CardBalance;
import com.oop.web_project.exceptionHandler.GlobalExceptionHandler;
import com.oop.web_project.mapping.AccountApiMapper;
import com.oop.web_project.mapping.AccountSummaryApiMapper;
import com.oop.web_project.mapping.CardApiMapper;
import com.oop.web_project.mapping.CardBalanceApiMapper;
import com.oop.web_project.restController.CardRestController;
import com.oop.web_project.services.AccountService;
import com.oop.web_project.services.CardService;
import com.oop.web_project.services.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, CardRestControllerTest.ValidatorConfig.class})
class CardRestControllerTest {

    @TestConfiguration
    static class ValidatorConfig {
        @Bean
        public LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CardApiMapper cardApiMapper;

    @MockitoBean
    private AccountApiMapper accountApiMapper;

    @MockitoBean
    private AccountSummaryApiMapper accountSummaryApiMapper;

    @MockitoBean
    private CardBalanceApiMapper cardBalanceApiMapper;

    @MockitoBean
    private JWTService jwtService;

    @Test
    void testGetCardByIdReturnsOk() throws Exception {
        Card card = mock(Card.class);
        CardResponse cardResponse = mock(CardResponse.class);

        when(cardService.selectCardById(1L)).thenReturn(card);
        when(cardApiMapper.toCardResponse(card)).thenReturn(cardResponse);

        mockMvc.perform(get("/api/card/1"))
                .andExpect(status().isOk());

        verify(cardService).selectCardById(1L);
        verify(cardApiMapper).toCardResponse(card);
    }

    @Test
    void testGetCardAccountReturnsOk() throws Exception {
        Account account = mock(Account.class);
        AccountSummaryResponse summaryResponse = mock(AccountSummaryResponse.class);

        when(accountService.selectAccountByCardId(1L)).thenReturn(account);
        when(accountSummaryApiMapper.toAccountSummaryResponse(account)).thenReturn(summaryResponse);

        mockMvc.perform(get("/api/card/1/account"))
                .andExpect(status().isOk());

        verify(accountService).selectAccountByCardId(1L);
        verify(accountSummaryApiMapper).toAccountSummaryResponse(account);
    }

    @Test
    void testGetCardBalancesReturnsOk() throws Exception {
        Card card = mock(Card.class);
        CardBalance cardBalance = mock(CardBalance.class);
        CardBalanceResponse balanceResponse = mock(CardBalanceResponse.class);

        when(cardService.selectCardById(1L)).thenReturn(card);
        when(cardService.selectCardBalances(1L)).thenReturn(List.of(cardBalance));
        when(cardBalanceApiMapper.toCardBalanceResponse(cardBalance)).thenReturn(balanceResponse);

        mockMvc.perform(get("/api/card/1/balances"))
                .andExpect(status().isOk());

        verify(cardService).selectCardById(1L);
        verify(cardService).selectCardBalances(1L);
        verify(cardBalanceApiMapper).toCardBalanceResponse(cardBalance);
    }

    @Test
    void testGetCardBalancesReturnsEmptyListWhenNoBalances() throws Exception {
        when(cardService.selectCardById(1L)).thenReturn(mock(Card.class));
        when(cardService.selectCardBalances(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/card/1/balances"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testGetCardExpirationReturnsTrueWhenExpired() throws Exception {
        when(cardService.checkCardExpiration(1L)).thenReturn(true);

        mockMvc.perform(get("/api/card/1/expiration"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(cardService).checkCardExpiration(1L);
    }

    @Test
    void testGetCardExpirationReturnsFalseWhenNotExpired() throws Exception {
        when(cardService.checkCardExpiration(1L)).thenReturn(false);

        mockMvc.perform(get("/api/card/1/expiration"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(cardService).checkCardExpiration(1L);
    }

    @Test
    void testDepositMoneyToCardReturnsOk() throws Exception {
        String body = """
                {
                  "amountToDeposit": 500.0,
                  "currencyCode": "USD"
                }
                """;

        mockMvc.perform(post("/api/card/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Money has been successfully deposited to card!"));

        verify(cardService).depositMoney(1L, BigDecimal.valueOf(500.00), "USD");
    }

    @Test
    void testTransferMoneyReturnsOk() throws Exception {
        String body = """
                {
                  "senderCardId": 1,
                  "receiverCardId": 2,
                  "amount": 300.0,
                  "currencyCode": "USD"
                }
                """;

        mockMvc.perform(post("/api/card/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Money have been successfully transferred!"));

        verify(cardService).transferMoney(1L, 2L, BigDecimal.valueOf(300.00), "USD");
    }

    @Test
    void testExchangeCurrencyReturnsOk() throws Exception {
        String body = """
                {
                  "amount": 100.0,
                  "fromCurrencyCode": "USD",
                  "toCurrencyCode": "EUR"
                }
                """;

        mockMvc.perform(post("/api/card/1/exchange-currency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Currency transfer was successful!"));

        verify(cardService).changeCurrency(1L, BigDecimal.valueOf(100.00), "USD", "EUR");
    }

    @Test
    void testAddCurrencyToCardReturnsOk() throws Exception {
        Card card = mock(Card.class);
        CardResponse cardResponse = mock(CardResponse.class);

        when(cardService.selectCardById(1L)).thenReturn(card);
        when(cardApiMapper.toCardResponse(card)).thenReturn(cardResponse);

        mockMvc.perform(patch("/api/card/1/currencies/EUR"))
                .andExpect(status().isOk());

        verify(cardService).addCurrencyToCard(1L, "EUR");
        verify(cardService).selectCardById(1L);
        verify(cardApiMapper).toCardResponse(card);
    }

    @Test
    void testActivateCardReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/card/1/activate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Card has been successfully activated!"));

        verify(cardService).activateCard(1L);
    }

    @Test
    void testDeactivateCardReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/card/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Card has been successfully deactivated!"));

        verify(cardService).deactivateCard(1L);
    }

    @Test
    void testDeleteCardReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/card/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Card has been successfully deleted!"));

        verify(cardService).deleteCard(1L);
    }
}