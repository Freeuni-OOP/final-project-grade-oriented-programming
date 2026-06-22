package com.oop.web_project;

import com.oop.web_project.utils.CardSecurityUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardSecurityUtilsTest {

    @Test
    void testMaskPanReturnsNullWhenPanIsNull() {
        String result = CardSecurityUtils.maskPan(null);

        assertNull(result);
    }

    @Test
    void testMaskPanReturnsAsIsWhenPanIsShorterThan16Digits() {
        String pan = "12345";

        String result = CardSecurityUtils.maskPan(pan);

        assertEquals(pan, result);
    }

    @Test
    void testMaskPanReturnsAsIsWhenPanIsLongerThan16Digits() {
        String pan = "12345678901234567";

        String result = CardSecurityUtils.maskPan(pan);

        assertEquals(pan, result);
    }

    @Test
    void testMaskPanReturnsAsIsWhenPanIsEmpty() {
        String pan = "";

        String result = CardSecurityUtils.maskPan(pan);

        assertEquals(pan, result);
    }

    @Test
    void testMaskPanReturnsMaskedValueWhenPanIs16Digits() {
        String pan = "4111111122223333";

        String result = CardSecurityUtils.maskPan(pan);

        assertEquals("411111******3333", result);
    }

    @Test
    void testGenerateTokenReturnsNullWhenPanIsNull() {
        String result = CardSecurityUtils.generateToken(null);

        assertNull(result);
    }

    @Test
    void testGenerateTokenReturnsNullWhenPanIsEmpty() {
        String result = CardSecurityUtils.generateToken("");

        assertNull(result);
    }

    @Test
    void testGenerateTokenReturns16CharacterTokenWhenPanIsValid() {
        String pan = "4111111122223333";

        String result = CardSecurityUtils.generateToken(pan);

        assertEquals(16, result.length());
    }

    @Test
    void testGenerateTokenPreservesFirstDigitAsRoutingId() {
        String pan = "5222222233334444";

        String result = CardSecurityUtils.generateToken(pan);

        assertEquals('5', result.charAt(0));
    }

    @Test
    void testGenerateTokenReturnsOnlyDigitsAfterRoutingId() {
        String pan = "4111111122223333";

        String result = CardSecurityUtils.generateToken(pan);

        assertTrue(result.substring(1).matches("\\d{15}"));
    }

    @Test
    void testGenerateTokenReturnsDifferentValuesOnRepeatedCalls() {
        String pan = "4111111122223333";

        String firstToken = CardSecurityUtils.generateToken(pan);
        String secondToken = CardSecurityUtils.generateToken(pan);

        assertNotEquals(firstToken, secondToken);
    }
}