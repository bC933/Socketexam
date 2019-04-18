package com.domain;

public class Client {

    private String IPAddress;
    private String port;

    public Client(String IPAddress, String port) {
        this.IPAddress = IPAddress;
        this.port = port;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Client{" +
                "IPAddress='" + IPAddress + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}
