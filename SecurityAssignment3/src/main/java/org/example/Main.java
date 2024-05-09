package org.example;
import org.example.Connector;

import java.io.IOException;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws IOException {
        Connector connector = new Connector();
        try {
            while (true) {
                connector.connectToNetwork();
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
}