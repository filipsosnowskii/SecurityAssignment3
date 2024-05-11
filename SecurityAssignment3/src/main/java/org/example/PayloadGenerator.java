package org.example;

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

    public static String generateVersionPayload() throws NoSuchAlgorithmException, UnknownHostException {
        StringBuilder stringBuilder = new StringBuilder();
        //Header
        String magicNumber = "F9BEB4D9";
        String versionCommand = "76657273696F6E0000000000";
        String payLoadLengthCommand = "00000000";//"64 00 00 00";
//        String checksumCommand = "358d4932";
        //Payload
//        String versionMessage = "62 EA 00 00";
        //Convert int to byte array https://stackoverflow.com/questions/2183240/java-integer-to-byte-array
        byte[] version =  ByteBuffer.allocate(4).putInt(70015).array(); //70015 is the most recent version https://developer.bitcoin.org/reference/p2p_networking.html
        byte[] services =  HexFormat.of().parseHex("0100000000000000");
        byte[] timestamp = ByteBuffer.allocate(8).putLong(System.currentTimeMillis() / 1000L).array();
        //Find ipv4 address
        String ipv4Address = InetAddress.getLocalHost().getHostAddress(); //TODO:convert address to hex
        String bitcoinPort = "208D"; //8333 in hex
        byte[] addrRecv = HexFormat.of().parseHex("0100000000000000" + ipv4Address + bitcoinPort);
        stringBuilder.append(magicNumber);
        stringBuilder.append(versionCommand);
        stringBuilder.append(payLoadLengthCommand);
        stringBuilder.append("54DED412");//calculateCheckSum(stringBuilder.toString())); TODO: fix checksum
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

    private static String calculateCheckSum(String string) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(string.getBytes());
        byte[] hash2 = digest.digest(hash);
        StringBuilder checkSum = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            checkSum.append(Integer.toHexString(hash2[i] & 0xFF).toUpperCase());
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
