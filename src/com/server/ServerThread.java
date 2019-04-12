package com.server;

import com.ui.ServerUI;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServerThread extends Thread {

    private static String host = "172.16.33.37";
    private static int port = 9999;

    private static ServerSocket serverSocket = null;

    public static List<Socket> socketList = new ArrayList<>();

    private byte[] bytes = new byte[1024];

    public static int sumOfConnectedClient = socketList.size();

    static {
        try {
            serverSocket = new ServerSocket(port, 5, InetAddress.getByName(host));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerThread() {

    }

    @Override
    public void run() {
        while (true) {
            try {

                Socket socket = serverSocket.accept();

                socketList.add(socket);
                sumOfConnectedClient = socketList.size();


                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ServerUI.reFreshList();
                        ServerUI.reFreshLeftBottom();
                    }
                });


                new Thread() {
                    @Override
                    public void run() {

                        while (true) {
                            /**
                             * 一直尝试接收数据
                             */
                            try {
                                receiveMessage(socket);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            if (isServerClose(socket)) {

                                try {
                                    socketList.remove(socket);
                                    sumOfConnectedClient = socketList.size();
                                    socket.shutdownOutput();
                                    socket.shutdownInput();
                                    socket.close();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        ServerUI.reFreshList();
                                        ServerUI.reFreshLeftBottom();
                                    }
                                });
                                break;
                            }
                        }

                    }
                }.start();


            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    public static ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * 收数据
     *
     * @throws IOException
     */
    private void receiveMessage(Socket socket) throws IOException {

        InputStream inputStream = socket.getInputStream();

        int len = inputStream.read(bytes);

        /**
         * 如果接收到的数据长度不为0，输出到界面上
         */
        if (len > 0) {
            //预留输出到界面的接口

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        String s = String.valueOf("[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "]" + "发来一条消息:");
                        s += new String(bytes, 0, len, "UTF-8");
                        s += "\n";
                        ServerUI.reFreshReceived(s);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

    }


    /**
     * 发数据
     *
     * @param str
     * @throws IOException
     */
    public void sendMessage(Socket socket, String str) {

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
    public static String getLocalIPAddress() {
        return String.valueOf(serverSocket.getInetAddress().getHostAddress());
    }

    /**
     * 获取本地端口
     *
     * @return
     */
    public static String getLocalPort() {
        return String.valueOf(serverSocket.getLocalPort());
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

}
