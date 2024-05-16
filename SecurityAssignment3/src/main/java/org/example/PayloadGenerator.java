package org.example;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        System.out.println("Parsing tx payload:");

        int offset = 0;

        //Get version
        byte[] version = new byte[4];
        System.arraycopy(txMessagePayload, 0, version, 0, 4);
        offset += 4;
        String versionString = convertByteArrayToHexString(version);
        System.out.println("Transaction data format version: " + versionString);

        //check if flag is present
        byte[] flag = new byte[2];
        System.arraycopy(txMessagePayload, 4, flag, 0, 2);
        boolean flagPresent = false;
        if (flag[0] == (byte) 0x00 && flag[1] == (byte) 0x01) {
            offset += 2;
            flagPresent = true;
            System.out.println("Witness data is present");
        }

        //Get txin count
        byte txInCountFirstByte = txMessagePayload[offset];
        int txInCountLength = getVarIntLength(txInCountFirstByte);
        byte[] txInCountBytes = new byte[txInCountLength];
        System.arraycopy(txMessagePayload, offset, txInCountBytes, 0, txInCountLength); //TODO: was 1
        long txInCount = getIntFromVarInt(txInCountBytes, txInCountLength);
        System.out.println("Number of transaction inputs: " + txInCount);
        offset += txInCountLength;

        //Parse txins
        for (int i = 0; i < txInCount; i++) {
            //Parse outpoint
            byte[] hash = new byte[32];
            byte[] index = new byte[4];
            System.arraycopy(txMessagePayload, offset, hash, 0, 32);
            offset += 32;
            System.arraycopy(txMessagePayload, offset, index, 0, 4);
            offset += 4;
            System.out.println("Hash of the previous transaction: " + convertByteArrayToHexString(hash));
            ByteBuffer byteBuffer = ByteBuffer.wrap(index).order(ByteOrder.LITTLE_ENDIAN);
            System.out.println("Index of the previous transaction: " + byteBuffer.getInt());

            //Get script length
            byte scriptLengthFirstByte = txMessagePayload[offset];
            int scriptLengthLength = getVarIntLength(scriptLengthFirstByte);
            byte[] scriptLengthBytes = new byte[scriptLengthLength]; //TODO:copy script, ok so you are not copying the script length
            System.arraycopy(txMessagePayload, offset, scriptLengthBytes, 0, scriptLengthLength); //TODO: verify, probably have to remove the copy from the method + print script proper
            long scriptLength = getIntFromVarInt(scriptLengthBytes, scriptLengthLength);
            System.out.println("Script length: " + scriptLength);
            byte[] scriptBytes = new byte[(int) scriptLength];
            offset += scriptLengthLength;
            System.arraycopy(txMessagePayload, offset, scriptBytes, 0, (int) scriptLength);
            byteBuffer = ByteBuffer.wrap(scriptBytes);
            System.out.println("Script: " + convertByteArrayToHexString(scriptBytes)); //new String(byteBuffer.array(), StandardCharsets.UTF_8).trim()); //use convertByteArrayToHexString instead
            offset += scriptBytes.length;
            byte[] sequence = new byte[4];
            System.arraycopy(txMessagePayload, offset, sequence, 0, 4);
            byteBuffer = ByteBuffer.wrap(sequence).order(ByteOrder.LITTLE_ENDIAN);
            System.out.println("Sequence: " + byteBuffer.getInt()); //TODO: should be hex?
            offset += 4;
        }

        //Get txout count
        byte txOutCountFirstByte = txMessagePayload[offset];
        int txOutCountLength = getVarIntLength(txOutCountFirstByte);
        byte[] txOutCountBytes = new byte[txOutCountLength];
        System.arraycopy(txMessagePayload, offset, txOutCountBytes, 0, txOutCountLength);
        long txOutCount = getIntFromVarInt(txOutCountBytes, txOutCountLength);
        System.out.println("Number of transaction outputs: " + txOutCount);
        offset += txOutCountLength;

//        byte txInCountFirstByte = txMessagePayload[offset];
//        int txInCountLength = getVarIntLength(txInCountFirstByte);
//        byte[] txInCountBytes = new byte[txInCountLength];
//        System.arraycopy(txMessagePayload, offset, txInCountBytes, 0, 1);
//        long txInCount = getIntFromVarInt(txInCountBytes, txInCountLength);
//        System.out.println("Number of transaction inputs: " + txInCount);
//        offset += txInCountLength;

        //Parse txouts
        for (int i = 0; i < txOutCount; i++) {
            //Transaction value
            byte[] transactionValueBytes = new byte[8];
            System.arraycopy(txMessagePayload, offset, transactionValueBytes, 0, 8);
            offset += 8;
            ByteBuffer byteBuffer = ByteBuffer.wrap(transactionValueBytes);//.order(ByteOrder.LITTLE_ENDIAN);
            long transactionValue = Long.reverseBytes(byteBuffer.getLong());//byteBuffer.getLong(); //=0
            System.out.println("Transaction value: " + transactionValue);

            //pk Script
            byte pkScriptLengthFirstByte = txMessagePayload[offset];
            int pkScriptLengthLength = getVarIntLength(pkScriptLengthFirstByte);
            byte[] pkScriptLengthBytes = new byte[pkScriptLengthLength]; //TODO: copy script
            System.arraycopy(txMessagePayload, offset, pkScriptLengthBytes, 0, pkScriptLengthLength); //TODO: verify this works
            long pkScriptLength = getIntFromVarInt(pkScriptLengthBytes, pkScriptLengthLength);
            System.out.println("Script length: " + pkScriptLength);
            offset += pkScriptLengthLength;
            byte[] pkScriptBytes = new byte[(int) pkScriptLength];
            System.arraycopy(txMessagePayload, offset, pkScriptBytes, 0, (int) pkScriptLength);
            offset += pkScriptBytes.length;
            byteBuffer = ByteBuffer.wrap(pkScriptBytes);
            System.out.println("Script: " + convertByteArrayToHexString(pkScriptBytes)); //new String(byteBuffer.array(), StandardCharsets.UTF_8).trim());
        }

        //Parse txWitnesses
        if (flagPresent) {
            System.out.println("Flag present"); //TODO: remove this line
            byte[] txWitnesses = new byte[4];
        }

        //lock_time
        byte[] lockTimeBytes = new byte[4];
        System.arraycopy(txMessagePayload, offset, lockTimeBytes, 0, 4);
        ByteBuffer byteBuffer = ByteBuffer.wrap(lockTimeBytes);
        int lockTime = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getInt();
        System.out.println("Lock time: " + lockTime);
    }

    private static long getIntFromVarInt(byte[] varInt, int varIntLength) {
        long txInCount = 0; //TODO: check if you need to copy stuff here, probably have to write some tests
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
