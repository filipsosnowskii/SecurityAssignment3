package org.example;

public class Header {
    private String magicNumber;
    private String command;
    private int payloadLength;
    private String checksum;

    public Header() {
        this.magicNumber = "";
        this.command = "";
        this.payloadLength = 0;
        this.checksum = "";
    }

    public Header(String magicNumber, String command, int payloadLength, String checksum) {
        this.magicNumber = magicNumber;
        this.command = command;
        this.payloadLength = payloadLength;
        this.checksum = checksum;
    }

    public String getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(String magicNumber) {
        this.magicNumber = magicNumber;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
