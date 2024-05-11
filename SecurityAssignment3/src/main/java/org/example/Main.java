package org.example;
import org.example.Connector;
//import org.example.PayloadGenerator

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HexFormat;

//import static org.example.PayloadGenerator.convertStringToByteArray;

public class Main {
    public static void main(String[] args) throws IOException {
//        String magicNumber = /*0x*/ "F9BEB4D9";
//        System.out.println(Arrays.toString(HexFormat.of().parseHex(magicNumber)));//convertStringToByteArray(magicNumber, 4)));
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