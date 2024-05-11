package org.example;


import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;
import static org.example.PayloadGenerator.calculateCheckSum;

public class PayloadGeneratorTest {

    @Test
    public void testCheckSum() throws NoSuchAlgorithmException, CloneNotSupportedException {
        String payload = "0001117F0100000000000000663FF77F010000000000000000000000000000000000FFFF35362E31208D";
        String expectedCheckSum = "D5115180";
        assertEquals(expectedCheckSum, calculateCheckSum(payload));
    }
}
