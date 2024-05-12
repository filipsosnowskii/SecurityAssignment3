package org.example;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Array;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;

public class PayloadGenerator {

    public static byte[] generateVersionPayload() throws NoSuchAlgorithmException, IOException, CloneNotSupportedException {
        //Header
        String magicNumber = "F9BEB4D9";
        String versionCommand = "76657273696F6E0000000000"; //"version" in hex format with padding
        //Payload
        int version = 70016;
        long services = 1;
        long timestamp = System.currentTimeMillis() / 1000L;
        InetAddress receiverAddress = InetAddress.getLocalHost();
        InetAddress addrFrom = receiverAddress;
        Random random = new Random();
        Long nonce = random.nextLong(Integer.MAX_VALUE);;
        String userAgent = "/Satoshi:27.0.0/"; //"";
        int startHeight = 0; //843192
        boolean relay = false;

        //Write Payload
        ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(payloadStream);
        outputStream.writeInt(version);
        outputStream.writeLong(services);
        outputStream.writeLong(timestamp);
        outputStream.writeLong(services);
        outputStream.write(HexFormat.of().parseHex("00000000000000000000FFFF"));
        outputStream.write(receiverAddress.getAddress());
        outputStream.writeShort(8333);
        outputStream.writeLong(services);
        outputStream.write(HexFormat.of().parseHex("00000000000000000000FFFF"));
        outputStream.write(addrFrom.getAddress());
        outputStream.writeShort(8333);
        outputStream.writeLong(nonce);
//        outputStream.writeByte(userAgent.length());
//        outputStream.writeBytes(userAgent);
        outputStream.write(HexFormat.of().parseHex("00")); //userAgent set to 0
        outputStream.writeInt(startHeight);
        outputStream.writeBoolean(relay);

        byte[] payload = payloadStream.toByteArray();

        //Write header and add payload for full version message
        ByteArrayOutputStream versionMessageStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(versionMessageStream);
        out.write(new byte[]{(byte) 0xF9, (byte) 0xBE, (byte) 0xB4, (byte) 0xD9});
        out.write(HexFormat.of().parseHex(versionCommand));//writeBytes("version");write("version");
        out.writeInt(payload.length);//Integer.reverseBytes(payload.length));
        out.write(calculateCheckSum(payload));
        out.write(payload);
        return versionMessageStream.toByteArray();
    }

    public void parseInvMessagePayload(byte[] invMessagePayload) {
        //Determine var_int length
        int storageLength = 1;
        String firstByteInHex = String.format("%02x", Byte.parseByte(String.valueOf(invMessagePayload[0])));
        if (firstByteInHex.startsWith("FD")) {
            storageLength = 3;
        }
        if (firstByteInHex.startsWith("FE")) {
            storageLength = 5;
        }
        if (firstByteInHex.startsWith("FF")) {
            storageLength = 9;
        }
        byte[] numberOfInvEntriesBytes = new byte[storageLength];
        for (int i = 1; i < storageLength; i++) {
            numberOfInvEntriesBytes[i-1] = invMessagePayload[i];
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(numberOfInvEntriesBytes);
        long numberOfInvEntries = 0;
        if (storageLength == 3) {
            numberOfInvEntries = byteBuffer.getShort();
        }
        if (storageLength == 5) {
            numberOfInvEntries = byteBuffer.getInt();
        }
        if (storageLength == 9) {
            numberOfInvEntries = byteBuffer.getLong();
        }
        System.out.println("Number of of inventory entries: " + numberOfInvEntries);
        //Parse Inv vectors
        for (int i = storageLength; i < numberOfInvEntries; i+=36) {
            if (i>=invMessagePayload.length) {
                System.out.println("End of payload reached.");
                System.out.println("-------------------------");
            }
            byte[] type = new byte[4];
            byte[] hash = new byte[32];
            System.arraycopy(invMessagePayload, i, type, 0, 4);
            System.arraycopy(invMessagePayload, i+4, hash, 0, 32);
            byteBuffer = ByteBuffer.wrap(type);
            long typeInt = byteBuffer.getLong();
            byteBuffer = ByteBuffer.wrap(hash);
            String hashString = byteBuffer.toString();
            System.out.println("Next event:");
            System.out.println("Object type: " + typeInt);
            System.out.println("Hash of the object : " + hashString);
            //TODO: ask for block info
        }
    }
    
    public static byte[] calculateCheckSum(byte[] bytes) throws NoSuchAlgorithmException, CloneNotSupportedException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        byte[] hash2 = digest.digest(hash);
        byte[] checkSum = new byte[4];
        System.arraycopy(hash2, 0, checkSum, 0, 4);
        return checkSum;
    }
}
