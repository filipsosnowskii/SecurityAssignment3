package org.example;
import java.io.*;
import java.net.*;
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
        socket = new Socket();
        socket.connect(new InetSocketAddress(dnsLookup("seed.bitcoin.sipa.be")[0].getHostAddress(), 8333));
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

        //DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        OutputStream out = socket.getOutputStream();

        //Write version message
        byte[] versionMessage = generateVersionPayload();
        System.out.println(Arrays.toString(versionMessage));
        out.write(versionMessage);
//        out.flush();

        DataInputStream in = new DataInputStream(socket.getInputStream());
//        InputStream in = socket.getInputStream();

        //Receive version message
        byte[] versionHeaderRet = new byte[versionMessage.length];
        if/*while*/ (in.available() == 0) {
            System.out.println("Waiting for version header");
            Thread.sleep(1000);
        }
        System.out.println("Read version header");
        byte[] header = new byte[24];
        in.readFully(header);
        System.out.println(Arrays.toString(header));
//        byte[] magicNumber = in.readNBytes(4);
//        byte[] command = in.readNBytes(12);
//        byte[] payload = in.readNBytes(4);
//        byte[] checkSum = in.readNBytes(4);
//        byte[] payload = new byte[payloadLength];
//        in.readFully(payload);
//        int input = 0;
//        int a = in.readInt();
//        System.out.println(a);
//        in.readFully(versionHeaderRet);
//        System.out.println(Arrays.toString(versionHeaderRet));
//        if (input == -1) {
//            System.out.println("No input received");
//            return;
//        }
    }
}
