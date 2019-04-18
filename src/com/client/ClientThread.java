package com.client;

import com.domain.Client;
import com.ui.ClientUI;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;

public class ClientThread extends Thread {

    public static Socket socket = null;
    private DatagramSocket datagramSocket;

    private static String host = "localhost";
    private static int port = 9999;
    private Client server = new Client(host, String.valueOf(port));

    private byte[] bytes = new byte[1024];

    private static String receiveMessage;


    public static List<Client> clientList = new ArrayList<>();

    public ClientThread() {

        while (true) {
            try {

                this.socket = new Socket(host, port);

                datagramSocket = new DatagramSocket(socket.getLocalPort());

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ClientUI.reFreshLeftUpvBox();
                        ClientUI.reFreshLeftBottom(true);
                    }
                });

                break;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    @Override
    public void run() {


        while (true) {
            try {

                /**
                 * 一直尝试接收数据
                 */
                receiveTcpMessage();

                new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                receiveUdpMessage();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();


            } catch (IOException e) {
                e.printStackTrace();
            }


            if (isServerClose(socket)) {


                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ClientUI.reFreshLeftBottom(false);
                        clientList.remove(server);
                        ClientUI.reFreshList();

                    }
                });


                while (true) {
                    try {
                        Thread.sleep(1500);
                        socket = new Socket(host, port);

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                ClientUI.reFreshLeftUpvBox();
                                ClientUI.reFreshLeftBottom(true);
                                ClientUI.reFreshList();
                                try {
                                    datagramSocket = new DatagramSocket(socket.getLocalPort());
                                } catch (SocketException e) {
                                    e.printStackTrace();
                                }

                            }
                        });

                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }


    }


    /**
     * 接收数据
     *
     * @throws IOException
     */
    private void receiveTcpMessage() throws IOException {

        InputStream inputStream = socket.getInputStream();

        int len = inputStream.read(bytes);

        /**
         * 如果接收到的数据长度不为0，输出到界面上
         */
        if (len > 0) {

            receiveMessage = new String(bytes, 0, len, "UTF-8");

            System.out.println("长度是" + len + "---收到的消息是" + receiveMessage);

            if (receiveMessage.charAt(0) == 'c' && receiveMessage.charAt(1) == 'l' && receiveMessage.charAt(2) == 'i' && receiveMessage.charAt(3) == 'e' && receiveMessage.charAt(4) == 'n' && receiveMessage.charAt(5) == 't') {

                clientList.clear();

                clientList.add(server);

                String[] info = receiveMessage.split("#");
                String list = info[1];

                String[] client = list.split(",");

                for (String s : client) {
                    String[] split = s.split(":");
                    if (!split[1].equals(String.valueOf(socket.getLocalPort()))) {
                        clientList.add(new Client(split[0], split[1]));
                    }
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ClientUI.reFreshList();
                    }
                });
            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        String s = String.valueOf("[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "]" + "发来一条消息:");
                        s += receiveMessage;
                        s += "\n";
                        ClientUI.reFreshReceived(s);

                    }
                });

            }
        }

    }

    /**
     * 接收数据
     *
     * @throws IOException
     */
    private void receiveUdpMessage() throws IOException {

        byte[] data = new byte[1024];

        DatagramPacket datagramPacket = new DatagramPacket(data, data.length);

        datagramSocket.receive(datagramPacket);

        InetAddress address = datagramPacket.getAddress();

        int port = datagramPacket.getPort();

        int length = datagramPacket.getLength();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                String s = String.valueOf("[" + address.getHostAddress() + ":" + port + "]" + "发来一条消息:");
                s += new String(data, 0, length);
                s += "\n";
                ClientUI.reFreshReceived(s);

            }
        });

    }

    /**
     * 发送数据
     *
     * @param str
     * @throws IOException
     */
    public static void sendMessage(String str) {

        try {
            OutputStream outputStream = socket.getOutputStream();

            outputStream.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 获取本地ip地址
     *
     * @return
     */
    public String getLocalIPAddress() {
        return String.valueOf(socket.getLocalAddress().getHostAddress());
    }

    /**
     * 获取本地端口
     *
     * @return
     */
    public String getLocalPort() {
        return String.valueOf(socket.getLocalPort());
    }

    /**
     * 判断是否断开连接，断开返回true,没有返回false
     *
     * @param socket
     * @return
     */
    public Boolean isServerClose(Socket socket) {
        try {
            socket.sendUrgentData(0xFF);//发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
            return false;
        } catch (Exception se) {
            return true;
        }
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public Client getServer() {
        return server;
    }
}