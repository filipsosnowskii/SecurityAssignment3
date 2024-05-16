package org.example;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import static org.example.PayloadGenerator.*;
import static org.example.PayloadGenerator.parseTxMessagePayload;


public class Connector {

    Socket socket;

    public Connector() throws IOException {
        socket = new Socket("151.21.128.99", 8333);
//        socket.connect(new InetSocketAddress(dnsLookup("seed.bitcoin.sipa.be")[0].getHostAddress(), 8333)); //Alternative way of connecting to a node
    }

    private InetAddress[] dnsLookup(String domain) throws UnknownHostException {
        return InetAddress.getAllByName(domain);
    }

    public void connectToNetwork() throws IOException {
        try {
            OutputStream out = socket.getOutputStream();

            //Write version message
            byte[] versionMessage = generateVersionPayload();
            System.out.println("Sending Version Message");
            out.write(versionMessage);
//            out.flush();

            //Retrieve incoming data
            DataInputStream in = new DataInputStream(socket.getInputStream());

            //Wait for response
            int retries = 0;
            while (in.available() == 0 && retries < 3) {
                System.out.println("Waiting for version header");
                Thread.sleep(10000);
                retries++;
            }

            if (in.available() == 0) {
                System.out.println("No response from node, exiting program");
                return;
            }

            //Receive version message back
            System.out.println("Read incoming version header");
            byte[] versionHeaderBytes = new byte[24];
            in.readFully(versionHeaderBytes);
            Header versionHeader = new Header();
            parseAndReadHeader(versionHeaderBytes, versionHeader);

            //Verify magic number
            if (!"F9BEB4D9".equals(versionHeader.getMagicNumber())) {
                System.out.println("Invalid magic number for version header, exiting program");
                return;
            }

            //Read payload
            byte[] payload = new byte[versionHeader.getPayloadLength()];
            in.readFully(payload);

            System.out.println("--------------------------------------------------");
            //Verify checksum
            if (!versionHeader.getChecksum().equals(convertByteArrayToHexString(calculateCheckSum(payload)))) {
                System.out.println("Invalid checksum for version payload, exiting program");
                return;
            }

            System.out.println("Magic number and checksum verified for version message");

            //Get verack header
            Header header = new Header();
            byte[] verackHeader = new byte[24];
            while (!"verack".equals(header.getCommand())) {
                in.readFully(verackHeader);
                parseAndReadHeader(verackHeader, header);
            }

            System.out.println("--------------------------------------------------");

            if (!"F9BEB4D9".equals(versionHeader.getMagicNumber())) {
                System.out.println("Invalid magic number for verack header, exiting program");
                return;
            }

            System.out.println("Magic number verified for verack header");

            //Send verack back
            out.write(verackHeader);
//            out.flush();

            //Receive and parse incoming events indefinitely
            while (true) {

                //wait for event
                while (in.available() == 0) {
                    System.out.println("Waiting for header");
                    Thread.sleep(10000);
                }

                Header invHeader = new Header();
                byte[] invHeaderBytes = new byte[24];
                in.readFully(invHeaderBytes);
                parseAndReadHeader(invHeaderBytes, invHeader);

                //Read payload
                byte[] invPayload = new byte[invHeader.getPayloadLength()];
                in.readFully(invPayload);

                //Verify magic number
                if (!"F9BEB4D9".equals(invHeader.getMagicNumber())) {
                    System.out.println("Invalid magic number for event, continuing to next event");
                    System.out.println("--------------------------------------------------");
                    continue;
                }

                //Verify checksum
                if (!invHeader.getChecksum().equals(convertByteArrayToHexString(calculateCheckSum(invPayload)))) {
                    System.out.println("Invalid checksum for event, continuing to next event");
                    System.out.println("--------------------------------------------------");
                    continue;
                }


                //send a getData message for each inv vector received
                if("inv".equals(invHeader.getCommand())) {

                    //Get number of inventory vectors
                    byte invVarIntFirstByte = invPayload[0];
                    int invVarIntLength = getVarIntLength(invVarIntFirstByte);
                    byte[] invVarIntBytes = new byte[invVarIntLength];
                    System.arraycopy(invPayload, 0, invVarIntBytes, 0, invVarIntLength);
                    long invVarCount = getIntFromVarInt(invVarIntBytes, invVarIntLength);
                    System.out.println("Number of inventory entries: " + invVarCount);


                    byte[] getDataMessage = generateGetDataMessage(invPayload);
                    System.out.println("--------------------------------------------------");
                    System.out.println("Sending getData Message");
                    out.write(getDataMessage);
                    out.flush();
                }

                if ("tx".equals(invHeader.getCommand())) {
                    parseTxMessagePayload(invPayload);
                }

                //TODO:Parse block messages
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
}
