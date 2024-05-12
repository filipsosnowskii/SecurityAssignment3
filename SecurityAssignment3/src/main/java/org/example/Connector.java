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
            byte[] magicNumberRet = new byte[4];
            byte[] command = new byte[12];
            byte[] payloadLengthVer = new byte[4];
            byte[] checkSumVer = new byte[4];
            in.readFully(magicNumberRet);
            in.readFully(command);
            in.readFully(payloadLengthVer);
            in.readFully(checkSumVer);
            ByteBuffer byteBuffer = ByteBuffer.wrap(payloadLengthVer);
            int versionPayloadLength = byteBuffer.getInt(); //Integer.parseInt(Arrays.toString(payloadLengthVer));
            System.out.println("Payload length: " + versionPayloadLength);
            if (versionPayloadLength < 0 || versionPayloadLength > 255) {
                System.out.println("Invalid version payload length");
                return;
            }
            //Read payload
            byte[] payload = new byte[versionPayloadLength];
            in.readFully(payload);
            System.out.println("Payload: " + Arrays.toString(payload));

            //Read verack
            byte[] verackHeader = new byte[24];
            in.readFully(verackHeader);

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
                int headerPayloadLen = Integer.parseInt(Arrays.toString(payloadLengthVer));

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
}
