package org.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static org.example.HelperMethods.*;
import static org.example.HelperMethods.convertByteArrayToHexString;

public class MessageParser {

    public static void parseAndReadHeader(byte[] headerBytes, Header header) {

        byte[] magicNumberBytes = new byte[4];
        byte[] commandBytes = new byte[12];
        byte[] payloadLengthBytes = new byte[4];
        byte[] checkSumVerBytes = new byte[4];

        System.arraycopy(headerBytes, 0, magicNumberBytes, 0, 4);
        System.arraycopy(headerBytes, 4, commandBytes, 0, 12);
        System.arraycopy(headerBytes, 16, payloadLengthBytes, 0, 4);
        System.arraycopy(headerBytes, 20, checkSumVerBytes, 0, 4);

        System.out.println("--------------------------------------------------");
        System.out.println("Header");

        header.setMagicNumber(convertByteArrayToHexString(magicNumberBytes));
        System.out.println("Magic number: " + header.getMagicNumber());
        ByteBuffer byteBuffer = ByteBuffer.wrap(commandBytes);
        header.setCommand(new String(byteBuffer.array(), StandardCharsets.UTF_8).trim());
        System.out.println("Command String: " + header.getCommand());
        byteBuffer = ByteBuffer.wrap(payloadLengthBytes).order(ByteOrder.LITTLE_ENDIAN);
        header.setPayloadLength(byteBuffer.getInt());
        System.out.println("Payload length: " + header.getPayloadLength());
        header.setChecksum(convertByteArrayToHexString(checkSumVerBytes));
        System.out.println("Check Sum: " + header.getChecksum());
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
            System.out.println("---------- Transaction Input " + (i+1));
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
            System.out.println("---------- Transaction Output " + (i+1));
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

    //I did not receive any blocks in my testing so can't verify this works - but the parse tx command definitely does work
    public static void parseBlockPayload(byte[] payload) {
        System.out.println("Parsing block payload:");

        int offset = 0;

        //Get version
        byte[] version = new byte[4];
        System.arraycopy(payload, 0, version, 0, 4);
        ByteBuffer byteBuffer = ByteBuffer.wrap(version).order(ByteOrder.LITTLE_ENDIAN);
        System.out.println("Block version information: " + byteBuffer.getInt());
        offset += 4;

        //Previous block
        byte[] prevBlock  = new byte[32];
        System.arraycopy(payload, offset, prevBlock, 0, 32);
        System.out.println("Hash value of the previous block: " + convertByteArrayToHexString(prevBlock));
        offset += 32;

        //Merkle Root
        byte[] merkleRoot = new byte[32];
        System.arraycopy(payload, offset, merkleRoot, 0, 32);
        System.out.println("Merkle tree collection hash:" + convertByteArrayToHexString(merkleRoot));
        offset += 32;

        //timestamp
        byte[] timestamp = new byte[4];
        System.arraycopy(payload, offset, timestamp, 0, 4);
        String timestampString = convertByteArrayToHexString(timestamp);
        long timestampValue = Long.parseLong(timestampString);

        //Convert to human-readable time
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestampValue), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        String humanReadableTimestamp = localDateTime.format(formatter);
        System.out.println("Timestamp: " + humanReadableTimestamp);

        offset += 4;

        //bits
        byte[] bits = new byte[4];
        System.arraycopy(payload, offset, bits, 0, 4);
        String bitsString = convertByteArrayToHexString(bits);
        long bitsValue = Long.parseLong(bitsString);
        System.out.println("Difficulty target: " + bitsValue);
        offset += 4;

        //nonce
        byte[] nonce = new byte[4];
        System.arraycopy(payload, offset, nonce, 0, 4);
        String nonceString = convertByteArrayToHexString(nonce);
        long nonceValue = Long.parseLong(nonceString);
        System.out.println("Nonce: " + nonceValue);
        offset += 4;

        //tx count
        byte txCountFirstByte = payload[offset];
        int txCountLength = getVarIntLength(txCountFirstByte);
        byte[] txCountBytes = new byte[txCountLength];
        System.arraycopy(payload, offset, txCountBytes, 0, txCountLength);
        long txCount = getIntFromVarInt(txCountBytes, txCountLength);
        System.out.println("Tx count in block: " + txCount);
        offset += txCountLength;

        byte[] remainingBytes = new byte[payload.length - offset];
        System.arraycopy(payload, offset, remainingBytes, 0, payload.length - offset);

        offset = 0; //Reset offset

        //Parse tx commands
        System.out.println("Parsing tx commands in block:");
        for (int i = 0; i < txCountLength; i++) {
            Header header = new Header();
            parseAndReadHeader(remainingBytes, header);
            byte[] txPayload = new byte[header.getPayloadLength()];
            System.arraycopy(remainingBytes, offset, txPayload, 0, remainingBytes.length);
            parseTxMessagePayload(txPayload);
            offset += txPayload.length;
        }

    }
}
