package org.example;
import org.example.Connector;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Connector connector = new Connector();
        try {
            connector.connectToNetwork();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//        try {
//            while (true) {
//                connector.connectToNetwork();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}