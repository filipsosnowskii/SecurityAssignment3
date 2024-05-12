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

//    public static String generateVersionPayload() throws NoSuchAlgorithmException, UnknownHostException, CloneNotSupportedException {
//        StringBuilder stringBuilder = new StringBuilder();
//        //Header
//        String magicNumber = "F9BEB4D9";
//        String versionCommand = "76657273696F6E0000000000";
//        //Payload
//        String version = "0001117F"; //70015 in hex
//        String services = "0100000000000000";
//        long timestamp = System.currentTimeMillis() / 1000L;
//        //Find ipv4 address
//        StringBuilder ipString = new StringBuilder();
//        ipString.append("00000000000000000000FFFF");
//        String ipv4Address = InetAddress.getLocalHost().getHostAddress();
//        //convert address to hex and provide last 4 bytes
//        ipString.append(String.format("%02x", new BigInteger(1, ipv4Address.getBytes())).toUpperCase().substring(ipString.length()-8));
//        String bitcoinPort = "208D"; //8333 in hex
//        //Receiver address
//        String receiverAddress = "0100000000000000" + ipString + bitcoinPort;
//        String addrFrom = receiverAddress;
//        String nonce = generateNonce();
//        String userAgent = "000";
//        String startHeight = "0000";
//        String relay = "1";
////        byte[] addrRecv = HexFormat.of().parseHex(receiverAddress);
//        //Calculate payload length
//        StringBuilder payload = new StringBuilder();
//        payload.append(version);
//        payload.append(services);
//        payload.append(Long.toHexString(timestamp).toUpperCase());
//        payload.append(receiverAddress);
//
//        payload.append(addrFrom);
//        payload.append(nonce);
//        payload.append(userAgent);
//        payload.append(startHeight);
//        payload.append(relay);
//        int payloadLength = payload.length();
//        StringBuilder payloadLengthCommand = new StringBuilder();
//        for (int i = 0; i < 8-Integer.toHexString(payloadLength).length(); i++) {
//            payloadLengthCommand.append("0");
//        }
//        payloadLengthCommand.append(Integer.toHexString(payloadLength));
////        String payloadLengthCommand = Integer.toHexString(payloadLength);
//        //Version header
//        stringBuilder.append(magicNumber);
//        stringBuilder.append(versionCommand);
//        stringBuilder.append(payloadLengthCommand);
//        stringBuilder.append(calculateCheckSum(payload.toString()));
////        stringBuilder.append("54DED412");//calculateCheckSum(stringBuilder.toString()));
//        //Version payload
//        stringBuilder.append(payload);
//        return stringBuilder.toString();
//    }

    public static byte[] generateVersionPayload() throws NoSuchAlgorithmException, IOException, CloneNotSupportedException {
        //Header
        String magicNumber = "F9BEB4D9";
        String versionCommand = "76657273696F6E0000000000"; //"version" in hex format with padding
        //Payload
        int version = 70015;
        long services = 1;
        long timestamp = System.currentTimeMillis() / 1000L;
        //Set receiver address
//        StringBuilder ipString = new StringBuilder();
//        ipString.append("00000000000000000000FFFF");
//        String ipv4Address = InetAddress.getLocalHost().getHostAddress();
//        ipString.append(String.format("%02x", new BigInteger(1, ipv4Address.getBytes())).toUpperCase().substring(ipString.length()-8)); //convert address to hex and provide last 4 bytes
//        int bitcoinPort = 8333;
//        String recvAddr = "0100000000000000" + ipString + bitcoinPort;
        //Receiver address
        InetAddress receiverAddress = InetAddress.getLocalHost();
//        byte[] receiverAddress = new byte[26];
//        receiverAddress = HexFormat.of().parseHex(recvAddr);
//        byte[] addrFrom = receiverAddress;
        InetAddress addrFrom = receiverAddress;
        Random random = new Random();
        Long nonce = random.nextLong(Integer.MAX_VALUE);;
        String userAgent = "";
        int startHeight = 0;
        boolean relay = true;

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

    public static byte[] calculateCheckSum(byte[] bytes) throws NoSuchAlgorithmException, CloneNotSupportedException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        byte[] hash2 = digest.digest(hash);
        byte[] checkSum = new byte[4];
        System.arraycopy(hash2, 0, checkSum, 0, 4);
        return checkSum;
    }
}
