package org.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HelperMethods {

    public static long getIntFromVarInt(byte[] varInt, int varIntLength) {
        long txInCount = 0;
        if (varIntLength == 1) {
            txInCount = Byte.toUnsignedInt(varInt[0]);
        }
        else if (varIntLength == 3) {
            byte[] shortArray = new byte[2];
            System.arraycopy(varInt, 1, shortArray, 0, 2);
            ByteBuffer byteBuffer = ByteBuffer.wrap(shortArray).order(ByteOrder.LITTLE_ENDIAN);
            txInCount = byteBuffer.getShort();
        }
        else if (varIntLength == 5) {
            byte[] intArray = new byte[4];
            System.arraycopy(varInt, 1, intArray, 0, 4);
            ByteBuffer byteBuffer = ByteBuffer.wrap(intArray).order(ByteOrder.LITTLE_ENDIAN);
            txInCount = byteBuffer.getInt();
        }
        else if (varIntLength == 9) {
            byte[] longArray = new byte[8];
            System.arraycopy(varInt, 1, longArray, 0, 8);
            ByteBuffer byteBuffer = ByteBuffer.wrap(longArray).order(ByteOrder.LITTLE_ENDIAN);
            txInCount = byteBuffer.getLong();
        }
        return txInCount;
    }

    public static int getVarIntLength(byte b) {
        int length = Byte.toUnsignedInt(b);
        if (length < 0xFD) {
            return 1;
        }
        else if (length == 0xFD) {
            return 3;
        }
        else if (length == 0xFE) {
            return 5;
        }
        else {
            return 9;
        }
    }

    public static String convertByteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            String hex = Integer.toHexString(0xff & b);
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    public static byte[] calculateCheckSum(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        byte[] hash2 = digest.digest(hash);
        byte[] checkSum = new byte[4];
        System.arraycopy(hash2, 0, checkSum, 0, 4);
        return checkSum;
    }
}
