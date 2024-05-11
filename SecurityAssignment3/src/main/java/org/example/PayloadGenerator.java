package org.example;

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

public class PayloadGenerator {

    public static String generateVersionPayload() throws NoSuchAlgorithmException, UnknownHostException, CloneNotSupportedException {
        StringBuilder stringBuilder = new StringBuilder();
        //Header
        String magicNumber = "F9BEB4D9";
        String versionCommand = "76657273696F6E0000000000";
        //Payload
        String version = "0001117F"; //70015 in hex
        String services = "0100000000000000";
        long timestamp = System.currentTimeMillis() / 1000L;
        //Find ipv4 address
        StringBuilder ipString = new StringBuilder();
        ipString.append("00000000000000000000FFFF");
        String ipv4Address = InetAddress.getLocalHost().getHostAddress();
        //convert address to hex and provide last 4 bytes
        ipString.append(String.format("%02x", new BigInteger(1, ipv4Address.getBytes())).toUpperCase().substring(ipString.length()-8));
        String bitcoinPort = "208D"; //8333 in hex
        //Receiver address
        String receiverAddress = "0100000000000000" + ipString + bitcoinPort;
//        byte[] addrRecv = HexFormat.of().parseHex(receiverAddress);
        //Calculate payload length
        StringBuilder payload = new StringBuilder();
        payload.append(version);
        payload.append(services);
        payload.append(Long.toHexString(timestamp).toUpperCase());
        payload.append(receiverAddress);
        int payloadLength = payload.length();
        StringBuilder payloadLengthCommand = new StringBuilder();
        for (int i = 0; i < 8-Integer.toHexString(payloadLength).length(); i++) {
            payloadLengthCommand.append("0");
        }
        payloadLengthCommand.append(Integer.toHexString(payloadLength));
//        String payloadLengthCommand = Integer.toHexString(payloadLength);
        //Version header
        stringBuilder.append(magicNumber);
        stringBuilder.append(versionCommand);
        stringBuilder.append(payloadLengthCommand);
        stringBuilder.append(calculateCheckSum(payload.toString()));
//        stringBuilder.append("54DED412");//calculateCheckSum(stringBuilder.toString()));
        //Version payload
        stringBuilder.append(payload);
        return stringBuilder.toString();
    }

    public static String generateVerackPayload() {
        //TODO: Write as bytes not string
        //TODO: Prefix with 0x? no
        StringBuilder stringBuilder = new StringBuilder();
        //Header
//        String magicNumber = /*0x*/ "0xF9 0xBE 0xB4 0xD9";
//        String verackCommand = "0x76 0x65 0x72 0x61 0x63 0x6B 0x00 0x00 0x00 0x00 0x00 0x00";
//        String payLoadLengthCommand = "0x00 0x00 0x00 0x00";
//        String checksumCommand = "0x5D 0xF6 0xE0 0xE2";
        String magicNumber = /*0x*/ "F9BEB4D9";
        String verackCommand = "76657261636B000000000000";
        String payLoadLengthCommand = "00000000";
        String checksumCommand = "5afcd6be".toUpperCase(); //"5DF6E0E2";
        //Message
//        String versionMessage = "62 EA 00 00";
        stringBuilder.append(magicNumber);
        stringBuilder.append(verackCommand);
        stringBuilder.append(payLoadLengthCommand);
        stringBuilder.append(checksumCommand);
        return stringBuilder.toString();
    }

    public static String calculateCheckSum(String string) throws NoSuchAlgorithmException, CloneNotSupportedException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(HexFormat.of().parseHex(string));
        byte[] hash2 = digest.digest(hash);
        StringBuilder checkSum = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            checkSum.append(Integer.toHexString(hash2[i] & 0xFF).toUpperCase());
//            checkSum.append(String.format("%02x", hash2[i] & 0xFF).toUpperCase());
        }
        return checkSum.toString();
    }

//    public List<String> generateVersionPayload() {
//        List<String> magicNumber = Arrays.asList("0x","F9","BE","B4","D9");
//        76 65 72 73 69 6F 6E 00 00 00 00 00
//    }

//    public static byte[] convertStringToByteArray(String s, int b) {
//        String[] splitString = s.split(" ");
//        byte[] payload = new byte[splitString.length/b]; //TODO: or 2? 2
//        for (int i = 0; i < splitString.length; i++) {
////            payload[i] = (byte) Integer.parseInt(splitString[i]);
//            payload[i] = (byte) HexFormat.of().parseHex(splitString[i]);
//        }
//        return payload;
//    }

}
