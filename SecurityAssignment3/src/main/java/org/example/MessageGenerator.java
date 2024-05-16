package org.example;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static org.example.HelperMethods.calculateCheckSum;

public class MessageGenerator {

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


}
