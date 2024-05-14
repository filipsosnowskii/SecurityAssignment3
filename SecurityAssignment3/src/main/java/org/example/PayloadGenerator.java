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
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Array;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;

import static org.example.Connector.convertByteArrayToHexString;

public class PayloadGenerator {

    public static byte[] generateVersionPayload() throws NoSuchAlgorithmException, IOException, CloneNotSupportedException {
        //Header
        String magicNumber = "F9BEB4D9";
//        String versionCommand = "76657273696F6E0000000000"; //"version" in hex format with padding
        byte[] versionCommand = new byte[]{(byte) 0x76, (byte) 0x65, (byte) 0x72, (byte) 0x73, (byte) 0x69, (byte) 0x6F,
                (byte) 0x6E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00}; //76657273696F6E0000000000
        //Payload
        int version = 70016;
        long services = 1;
        long timestamp = System.currentTimeMillis() / 1000L;
        InetAddress receiverAddress = InetAddress.getLocalHost();
        InetAddress addrFrom = receiverAddress;
        byte[] addressPadding = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF};
        Random random = new Random();
        Long nonce = random.nextLong(Integer.MAX_VALUE);;
        String userAgent = "/Satoshi:27.0.0/"; //"";
        int startHeight = 0; //843192
        boolean relay = true;

        //Write Payload
        ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(payloadStream);
        outputStream.writeInt(Integer.reverseBytes(version));
        outputStream.writeLong(Long.reverseBytes(services));
        outputStream.writeLong(Long.reverseBytes(timestamp));
        outputStream.writeLong(Long.reverseBytes(services));
        outputStream.write(addressPadding); //writeBytes
        outputStream.write(receiverAddress.getAddress());
        outputStream.writeShort(Short.reverseBytes((short) 8333));
        outputStream.writeLong(Long.reverseBytes(services));
        outputStream.write(addressPadding); //writeBytes
        outputStream.write(addrFrom.getAddress());
        outputStream.writeShort(Short.reverseBytes((short) 88333));
        outputStream.writeLong(Long.reverseBytes(nonce));
//        outputStream.writeByte(userAgent.length());
//        outputStream.writeBytes(userAgent);
        outputStream.write(new byte[]{(byte) 0x00}); //userAgent set to 0
        outputStream.writeInt(Integer.reverseBytes(startHeight));
        outputStream.writeBoolean(relay);

        byte[] payload = payloadStream.toByteArray();

        //Write header and add payload for full version message
        ByteArrayOutputStream versionMessageStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(versionMessageStream);
        out.write(new byte[]{(byte) 0xF9, (byte) 0xBE, (byte) 0xB4, (byte) 0xD9});
        out.write(versionCommand); //HexFormat.of().parseHex(versionCommand));//writeBytes("version");write("version");
        out.writeInt(Integer.reverseBytes(payload.length));
        out.write(calculateCheckSum(payload));
        out.write(payload);
        return versionMessageStream.toByteArray();
    }

    public static void parseInvMessagePayload(byte[] invMessagePayload) throws InterruptedException {
//        //Determine var_int length
        int storageLength = 1;
////        String firstByteInHex = String.format("%02x", Byte.parseByte(String.valueOf(invMessagePayload[0])));
//        String firstByteInHex = Integer.toHexString(0xff & invMessagePayload[0]);
//        if (firstByteInHex.startsWith("FD")) {
//            storageLength = 3;
//        }
//        if (firstByteInHex.startsWith("FE")) {
//            storageLength = 5;
//        }
//        if (firstByteInHex.startsWith("FF")) {
//            storageLength = 9;
//        }
//        byte[] numberOfInvEntriesBytes = new byte[storageLength];
//        for (int i = 1; i < storageLength; i++) {
//            numberOfInvEntriesBytes[i-1] = invMessagePayload[i];
//        }
//        ByteBuffer byteBuffer = ByteBuffer.wrap(numberOfInvEntriesBytes);
        long numberOfInvEntries = invMessagePayload.length-1;//0;
//        if (storageLength == 3) {
//            numberOfInvEntries = byteBuffer.getShort();
//        }
//        if (storageLength == 5) {
//            numberOfInvEntries = byteBuffer.getInt();
//        }
//        if (storageLength == 9) {
//            numberOfInvEntries = byteBuffer.getLong();
//        }
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
            ByteBuffer byteBuffer = ByteBuffer.wrap(type).order(ByteOrder.LITTLE_ENDIAN);
            int typeInt = byteBuffer.getInt();
            byteBuffer = ByteBuffer.wrap(hash);
            String hashString = convertByteArrayToHexString(hash); //new String(byteBuffer.array(), StandardCharsets.UTF_8).trim();
            System.out.println("Next event:");
            System.out.println("Object type: " + typeInt);
            System.out.println("Hash of the object : " + hashString);
            Thread.sleep(100);
            //TODO: ask for block info
        }
    }

    public static byte[] generateGetDataMessage(byte[] payload) throws NoSuchAlgorithmException, IOException, CloneNotSupportedException {

        byte[] getDataCommand = new byte[]{(byte) 0x67, (byte) 0x65, (byte) 0x74, (byte) 0x64, (byte) 0x61, (byte) 0x74,
                (byte) 0x61, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

        ByteArrayOutputStream getdataMessageStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(getdataMessageStream);
        out.write(new byte[]{(byte) 0xF9, (byte) 0xBE, (byte) 0xB4, (byte) 0xD9});
        out.write(getDataCommand);
        out.writeInt(Integer.reverseBytes(payload.length));
        out.write(calculateCheckSum(payload));
        out.write(payload);

        return getdataMessageStream.toByteArray();
    }

    public static void parseTxMessagePayload(byte[] txMessagePayload) throws NoSuchAlgorithmException, IOException, CloneNotSupportedException {
        System.out.println("Parsing tx message:");
        byte[] version = new byte[4];
        byte[] flag = new byte[2];
        byte[] txInCount = new byte[1];
        byte[] txIn = new byte[0]; //parse count
        byte[] txOutCount = new byte[1];
        byte[] txOut = new byte[0]; //parse count
        byte[] txWitnesses = new byte[0];
        byte[] lockTime = new byte[4];

        System.arraycopy(txMessagePayload, 0, version, 0, 4);
        String versionString = convertByteArrayToHexString(version); //TODO: change this
        System.out.println("Version: " + versionString);
        System.arraycopy(txMessagePayload, 4, flag, 0, 2);
        int offset = 0;
        if (flag[0] == (byte) 0x00 && flag[1] == (byte) 0x01) {
            offset = 2;
        }
        byte varIntLength = txMessagePayload[4+offset];
        System.out.println("VAR LENGTH: " + getVarIntLength(varIntLength) + "-------------------------");
//        System.arraycopy(txMessagePayload, 4+offset, txInCount, 0, 1);
    }

    public static int getVarIntLength(byte b) {
        int length = Byte.toUnsignedInt(b);
        //TODO: is this better?
        //Integer.toHexString(0xff & b);
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

    public static void parseOutPoint() {
        //TODO
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
