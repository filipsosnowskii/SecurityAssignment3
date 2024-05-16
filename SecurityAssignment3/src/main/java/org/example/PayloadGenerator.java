package org.example;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class PayloadGenerator {

    public static byte[] generateVersionPayload() throws NoSuchAlgorithmException, IOException {

        //Header
        byte[] magicNumber = new byte[]{(byte) 0xF9, (byte) 0xBE, (byte) 0xB4, (byte) 0xD9};
        byte[] versionCommand = new byte[]{(byte) 0x76, (byte) 0x65, (byte) 0x72, (byte) 0x73, (byte) 0x69, (byte) 0x6F,
                (byte) 0x6E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00}; //76657273696F6E0000000000 - "version" in hex format with padding

        //Payload
        int version = 70016;
        long services = 1;
        long timestamp = System.currentTimeMillis() / 1000L;
        InetAddress receiverAddress = InetAddress.getLocalHost();
        InetAddress addrFrom = receiverAddress;
        byte[] addressPadding = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF};
        Random random = new Random();
        long nonce = random.nextLong(Integer.MAX_VALUE);;
        String userAgent = "/Satoshi:27.0.0/"; //Ignoring this as it's not needed, can be uncommented down below
        int startHeight = 0;
        boolean relay = true;

        //Write Payload
        ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(payloadStream);
        outputStream.writeInt(Integer.reverseBytes(version));
        outputStream.writeLong(Long.reverseBytes(services));
        outputStream.writeLong(Long.reverseBytes(timestamp));
        outputStream.writeLong(Long.reverseBytes(services));
        outputStream.write(addressPadding);
        outputStream.write(receiverAddress.getAddress());
        outputStream.writeShort(Short.reverseBytes((short) 8333));
        outputStream.writeLong(Long.reverseBytes(services));
        outputStream.write(addressPadding);
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
        out.write(magicNumber);
        out.write(versionCommand);
        out.writeInt(Integer.reverseBytes(payload.length));
        out.write(calculateCheckSum(payload));
        out.write(payload);

        return versionMessageStream.toByteArray();
    }

    public static byte[] generateGetDataMessage(byte[] payload) throws NoSuchAlgorithmException, IOException {

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

    public static void parseTxMessagePayload(byte[] txMessagePayload) {
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
        System.arraycopy(txMessagePayload, offset, txInCountBytes, 0, txInCountLength);
        long txInCount = getIntFromVarInt(txInCountBytes, txInCountLength);
        System.out.println("Number of transaction inputs: " + txInCount);
        offset += txInCountLength;

        //Parse txins
        for (int i = 0; i < txInCount; i++) {
            offset = parseTxIn(txMessagePayload, offset);
        }

        //Get txout count
        byte txOutCountFirstByte = txMessagePayload[offset];
        int txOutCountLength = getVarIntLength(txOutCountFirstByte);
        byte[] txOutCountBytes = new byte[txOutCountLength];
        System.arraycopy(txMessagePayload, offset, txOutCountBytes, 0, txOutCountLength);
        long txOutCount = getIntFromVarInt(txOutCountBytes, txOutCountLength);
        System.out.println("Number of transaction outputs: " + txOutCount);
        offset += txOutCountLength;

        //Parse txouts
        for (int i = 0; i < txOutCount; i++) {
            offset = parseTxOut(txMessagePayload, offset);
        }

        //Parse txWitnesses - data structure found at
        //https://bitcoin.stackexchange.com/questions/68924/complete-definition-of-tx-witness-data-structure-used-in-tx-data-structure
        //Haven't encountered any non-empty witness lists in my testing
        if (flagPresent) {

            System.out.println("Witnesses present");

            //Get witness component count
            byte txWitnessFirstByte = txMessagePayload[offset];
            int txWitnessLength = getVarIntLength(txWitnessFirstByte);
            byte[] txWitnessBytes = new byte[txWitnessLength];
            System.arraycopy(txMessagePayload, offset, txWitnessBytes, 0, txWitnessLength);
            long txWitnessCount = getIntFromVarInt(txWitnessBytes, txWitnessLength);
            System.out.println("Number of witness components: " + txWitnessCount);
            offset += txWitnessLength;

            //Go through witness components, just get length to add to offset and ignore it
            for (int i = 0; i < txWitnessCount; i++) {
                byte witnessComponentFirstByte = txMessagePayload[offset];
                int witnessComponentLengthLength = getVarIntLength(witnessComponentFirstByte);
                byte[] witnessComponentBytes = new byte[witnessComponentLengthLength];
                System.arraycopy(txMessagePayload, offset, witnessComponentBytes, 0, witnessComponentLengthLength);
                long witnessComponentLength = getIntFromVarInt(witnessComponentBytes, witnessComponentLengthLength);
                offset += witnessComponentLengthLength;
                offset += (int) witnessComponentLength;
            }
        }

        //lock_time
        byte[] lockTimeBytes = new byte[4];
        System.arraycopy(txMessagePayload, offset, lockTimeBytes, 0, 4);
        ByteBuffer byteBuffer = ByteBuffer.wrap(lockTimeBytes);
        int lockTime = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getInt();
        System.out.println("Lock time: " + lockTime);
    }

    private static int parseTxOut(byte[] txMessagePayload, int offset) {
        //Transaction value
        byte[] transactionValueBytes = new byte[8];
        System.arraycopy(txMessagePayload, offset, transactionValueBytes, 0, 8);
        offset += 8;
        ByteBuffer byteBuffer = ByteBuffer.wrap(transactionValueBytes).order(ByteOrder.LITTLE_ENDIAN);
        long transactionValue = byteBuffer.getLong();
        System.out.println("Transaction value: " + transactionValue);

        //pk Script
        byte pkScriptLengthFirstByte = txMessagePayload[offset];
        int pkScriptLengthLength = getVarIntLength(pkScriptLengthFirstByte);
        byte[] pkScriptLengthBytes = new byte[pkScriptLengthLength];
        System.arraycopy(txMessagePayload, offset, pkScriptLengthBytes, 0, pkScriptLengthLength);
        long pkScriptLength = getIntFromVarInt(pkScriptLengthBytes, pkScriptLengthLength);
        System.out.println("Script length: " + pkScriptLength);
        offset += pkScriptLengthLength;
        byte[] pkScriptBytes = new byte[(int) pkScriptLength];
        System.arraycopy(txMessagePayload, offset, pkScriptBytes, 0, (int) pkScriptLength);
        offset += pkScriptBytes.length;
        System.out.println("Script: " + convertByteArrayToHexString(pkScriptBytes));
        return offset;
    }

    private static int parseTxIn(byte[] txMessagePayload, int offset) {
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
        byte[] scriptLengthBytes = new byte[scriptLengthLength];
        System.arraycopy(txMessagePayload, offset, scriptLengthBytes, 0, scriptLengthLength);
        long scriptLength = getIntFromVarInt(scriptLengthBytes, scriptLengthLength);
        System.out.println("Script length: " + scriptLength);
        byte[] scriptBytes = new byte[(int) scriptLength];
        offset += scriptLengthLength;
        System.arraycopy(txMessagePayload, offset, scriptBytes, 0, (int) scriptLength);
        System.out.println("Script: " + convertByteArrayToHexString(scriptBytes));
        offset += scriptBytes.length;
        byte[] sequence = new byte[4];
        System.arraycopy(txMessagePayload, offset, sequence, 0, 4);
        System.out.println("Sequence: " + convertByteArrayToHexString(sequence));
        offset += 4;
        return offset;
    }

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
