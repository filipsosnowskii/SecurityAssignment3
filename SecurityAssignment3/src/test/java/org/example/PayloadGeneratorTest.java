package org.example;


import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.example.PayloadGenerator.calculateCheckSum;

public class PayloadGeneratorTest {

    @Test
    public void testCheckSum() throws NoSuchAlgorithmException, CloneNotSupportedException {
        byte[] payload = new byte[]{0, 1, 17, 127, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 102, 65, 37, -6, 0, 0, 0, 0, 0, 0, 0, 1, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -64, -88, 56, 1, 32, -115, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, -1, -1, -64, -88, 56, 1, 32, -115, 0, 0, 0, 0, 52, 125, -82, -67, 0, 0, 0, 0, 1};
        byte[] expectedCheckSum = new byte[]{-67, -107, -76, -96};
        assertEquals(Arrays.toString(expectedCheckSum), Arrays.toString(calculateCheckSum(payload)));
    }
}
