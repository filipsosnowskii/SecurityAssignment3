package org.example;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

            //Retrieve incoming data
            DataInputStream in = new DataInputStream(socket.getInputStream());
            int retries = 0;
            while (in.available() == 0 && retries < 3) {
                System.out.println("Waiting for version header");
                Thread.sleep(3000);
                retries++;
            }

            //TODO: Handle no data available
            //Receive version message back
            System.out.println("Incoming message size " + in.available());
            System.out.println("Read version header");
            byte[] versionHeaderBytes = new byte[24];
            in.readFully(versionHeaderBytes);
            Header versionHeader = new Header();
            parseAndReadHeader(versionHeaderBytes, versionHeader);

//            if (versionPayloadLength < 0 || versionPayloadLength > 255) {
//                System.out.println("Invalid version payload length");
//                return;
//            }

            //Read payload
            byte[] payload = new byte[versionHeader.getPayloadLength()];
            in.readFully(payload);
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

//            //Parse remaining headers
//            while (in.available() > 0) {
//                Header miscHeader = new Header();
//                byte[] miscHeaderBytes = new byte[24];
//                in.readFully(miscHeaderBytes);
//                parseAndReadHeader(miscHeaderBytes, miscHeader);
//            }

            //Send verack back
            out.write(verackHeader);
//            out.flush();

            //TODO: parse sendcmpct

            //Receive and parse payloads
            while (true) {

                retries = 0;
                while (in.available() == 0 && retries < 3) {
                    System.out.println("Waiting for header");
                    Thread.sleep(10000);
                    retries++;
                }

                Header invHeader = new Header();
                byte[] invHeaderBytes = new byte[24];
                in.readFully(invHeaderBytes);
                parseAndReadHeader(invHeaderBytes, invHeader);

                //Read payload TODO:add the logic for parsing inv messages
                byte[] invPayload = new byte[invHeader.getPayloadLength()];
                in.readFully(invPayload);
//                Header headerInv = new Header();
//                byte[] headerInvBytes = new byte[24];
//                System.arraycopy(invPayload, 0, headerInvBytes, 0, 24);
//                parseAndReadHeader(headerInvBytes, headerInv);
                System.out.println("Read payload");
                System.out.println(Arrays.toString(invPayload));
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

        System.out.println("--------------------------------------------------");
        System.out.println("Header");

        header.setMagicNumber(convertByteArrayToHexString(magicNumberBytes));
//        if (!"F9BEB4D9".equals(header.getMagicNumber())) {
//            System.out.println("Magic number " + header.getMagicNumber() + " not equal to F9BEB4D9, ignoring header");
//            return;
//        }
        System.out.println("Magic number: " + header.getMagicNumber());
        ByteBuffer byteBuffer = ByteBuffer.wrap(commandBytes);
        header.setCommand(new String(byteBuffer.array(), StandardCharsets.UTF_8).trim());
        System.out.println("Command String: " + header.getCommand());
        //TODO: int getting read wrong
        byteBuffer = ByteBuffer.wrap(payloadLengthBytes).order(ByteOrder.LITTLE_ENDIAN);
        header.setPayloadLength(byteBuffer.getInt());
        System.out.println("Payload length: " + header.getPayloadLength());
        header.setChecksum(convertByteArrayToHexString(checkSumVerBytes));
        System.out.println("Check Sum: " + header.getChecksum());
    }

//    public void parsesendcmpctMessage(byte[] sendcmpctBytes) {
//        byte[] magicNumberBytes = new byte[4];
//        byte[] commandBytes = new byte[12];
//    }

    public String convertByteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            String hex = Integer.toHexString(0xff & b);
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }
}
