package com.client;

import com.ui.ClientUI;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class ClientThread extends Thread {

    public static Socket socket = null;

    private static String host="172.16.33.37";
    private static int port = 9999;

    private byte[] bytes = new byte[1024];


    public ClientThread() {


        while (true) {
            try {

                this.socket = new Socket(host, port);

                break;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    @Override
    public void run() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ClientUI.reFreshLeftBottom(true);
            }
        });


        while (true) {
            try {

                /**
                 * 一直尝试接收数据
                 */
                receiveMessage();

            } catch (IOException e) {
                e.printStackTrace();
            }


            if (isServerClose(socket)) {


                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ClientUI.reFreshLeftBottom(false);
                    }
                });


                while (true) {
                    try {
                        Thread.sleep(1500);
                        socket = new Socket(host, port);

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                ClientUI.reFreshLeftBottom(true);
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
    private void receiveMessage() throws IOException {

        InputStream inputStream = socket.getInputStream();

        int len = inputStream.read(bytes);

        /**
         * 如果接收到的数据长度不为0，输出到界面上
         */
        if (len > 0) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        String s = String.valueOf("[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "]" + "发来一条消息:");
                        s += new String(bytes, 0, len, "UTF-8");
                        s += "\n";
                        ClientUI.reFreshReceived(s);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });
        }


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

}