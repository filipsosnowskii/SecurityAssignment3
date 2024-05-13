package org.example;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
//import org.example.PayloadGenerator;

import static org.example.PayloadGenerator.generateVersionPayload;

public class Connector {

    Socket socket;

    //TODO: set so that it connects to specified address, another constructor maybe?
    public Connector() throws IOException {
        socket = new Socket("151.21.128.99", 8333);
//        socket = new Socket("54.226.137.205"/*"82.96.96.40"*/, 8333);
//        socket.connect(new InetSocketAddress(dnsLookup("seed.bitcoin.sipa.be")[0].getHostAddress(), 8333));
    }

    private InetAddress[] dnsLookup(String domain/*String host, int port*/) throws UnknownHostException {
//        String domain = "seed.bitcoin.sipa.be";
//        try {
//        InetAddress[] addresses = InetAddress.getAllByName(domain);
        return InetAddress.getAllByName(domain);

//            for (InetAddress address : addresses) {
//                System.out.println(address.getHostAddress());
//            }
//        } catch (UnknownHostException e) {}
//    }

    }

    public void connectToNetwork() throws IOException, InterruptedException, NoSuchAlgorithmException, CloneNotSupportedException {
        try {
//            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            OutputStream out = socket.getOutputStream();

            //Write version message
            byte[] versionMessage = generateVersionPayload();
            System.out.println("Outgoing Version Message");
            System.out.println(Arrays.toString(versionMessage));
            out.write(versionMessage);
//            out.flush();
            DataInputStream in = new DataInputStream(socket.getInputStream());
//          InputStream in = socket.getInputStream();
            int retries = 0;
            while (in.available() == 0 && retries < 3) {
                System.out.println("Waiting for version header");
                Thread.sleep(3000);
                retries++;
            }
            //Receive version message back
            System.out.println("Incoming message size " + in.available());
            System.out.println("Read version header");
            byte[] versionMagicNumber = new byte[4];
            byte[] versionCommand = new byte[12];
            byte[] versionPayloadLengthBytes = new byte[4];
            byte[] versionCheckSum = new byte[4];
            in.readFully(versionMagicNumber);
            in.readFully(versionCommand);
            in.readFully(versionPayloadLengthBytes);
            in.readFully(versionCheckSum);
            ByteBuffer byteBuffer = ByteBuffer.wrap(versionMagicNumber);
            int magicNumber = Integer.reverse(byteBuffer.getInt());
            System.out.println("Magic number: " + magicNumber);
            byteBuffer = ByteBuffer.wrap(versionCommand);
            String commandString = new String(byteBuffer.array(), StandardCharsets.UTF_8);
            System.out.println("Command String: " + commandString);
            byteBuffer = ByteBuffer.wrap(versionPayloadLengthBytes);
            int versionPayloadLength = Integer.reverse(byteBuffer.getInt()); //Integer.parseInt(Arrays.toString(versionPayloadLengthBytes));
            System.out.println("Payload length: " + versionPayloadLength);
            if (versionPayloadLength < 0 || versionPayloadLength > 255) {
                System.out.println("Invalid version payload length");
                return;
            }
            //Read payload
            byte[] payload = new byte[versionPayloadLength];
            in.readFully(payload);
            System.out.println("Header: " + Arrays.toString(versionMagicNumber) + Arrays.toString(versionCommand) + Arrays.toString(versionPayloadLengthBytes) + Arrays.toString(versionCheckSum));
            System.out.println("Payload: " + Arrays.toString(payload));

//            System.out.println(in.available());
//            while (in.available() == 0) {
//                System.out.println("Waiting for version header");
//                Thread.sleep(3000);
//                retries++;
//            }

            //Get verack header
            //TODO: add code to handle payloads in case one gets sent before verack is received (shouldn't happen though)
            Header header = new Header();
            byte[] verackHeader = new byte[24];
            while (!"verack".equals(header.getCommand())) {
            in.readFully(verackHeader);
            parseAndReadHeader(verackHeader, header);
            }

            //Send verack back
            out.write(verackHeader);
            out.flush();

            while (true) {
                byte[] magicNum = new byte[4];
                byte[] headerCommand = new byte[12];
                byte[] headerPayloadLength = new byte[4];
                byte[] headerCheckSumVer = new byte[4];
                in.readFully(magicNum);
                in.readFully(headerCommand);
                in.readFully(headerPayloadLength);
                in.readFully(headerCheckSumVer);
                int headerPayloadLen = Integer.parseInt(Arrays.toString(headerPayloadLength));

                //Read payload TODO:add the logic for parsing inv messages
                byte[] payload2 = new byte[headerPayloadLen];
                in.readFully(payload2);
                System.out.println("Read payload");
                System.out.println(Arrays.toString(payload2));
            }
        } catch (Exception e) {
            socket.close();
        }
    }

    public void parseAndReadHeader(byte[] headerBytes, Header header) {
        byte[] magicNumberBytes = new byte[4];
        byte[] commandBytes = new byte[12];
        byte[] payloadLengthBytes = new byte[4];
        byte[] checkSumVerBytes = new byte[4];
        System.arraycopy(headerBytes, 0, magicNumberBytes, 0, 4);
        System.arraycopy(headerBytes, 4, commandBytes, 0, 12);
        System.arraycopy(headerBytes, 16, payloadLengthBytes, 0, 4);
        System.arraycopy(headerBytes, 20, checkSumVerBytes, 0, 4);
        header.setMagicNumber(convertByteArrayToHexString(magicNumberBytes));
        System.out.println("Magic number: " + header.getMagicNumber());
        ByteBuffer byteBuffer = ByteBuffer.wrap(commandBytes);
        header.setCommand(new String(byteBuffer.array(), StandardCharsets.UTF_8).trim());
        System.out.println("Command String: " + header.getCommand());
        byteBuffer = ByteBuffer.wrap(payloadLengthBytes);
        header.setPayloadLength(Integer.reverse(byteBuffer.getInt()));
        System.out.println("Payload length: " + header.getPayloadLength());
        header.setChecksum(convertByteArrayToHexString(checkSumVerBytes));
        System.out.println("Check Sum: " + header.getChecksum());
    }

    public String convertByteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            String hex = Integer.toHexString(0xff & b);
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }
}
