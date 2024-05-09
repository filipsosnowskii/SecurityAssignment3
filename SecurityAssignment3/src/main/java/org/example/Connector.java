package org.example;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

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

    public void connectToNetwork() throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write("version".getBytes());

//        InputStream in = socket.getInputStream();
        DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        
    }
}
