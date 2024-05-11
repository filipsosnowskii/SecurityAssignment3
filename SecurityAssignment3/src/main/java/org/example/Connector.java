package org.example;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
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

    public void connectToNetwork() throws IOException, InterruptedException, NoSuchAlgorithmException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.write(HexFormat.of().parseHex(generateVersionPayload()));
        out.flush();
        System.out.println("Connected to " + socket.getInetAddress().getHostAddress());

        DataInputStream in = new DataInputStream(socket.getInputStream());
        byte[] versionHeader = new byte[24];
        while (in.available() == 0) {
            System.out.println("Waiting for version header");
            Thread.sleep(10000);
        }
        System.out.println("Read version header");
        in.read(versionHeader);
    }
}
