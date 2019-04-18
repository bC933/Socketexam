package com.ui;

import com.client.ClientThread;
import com.domain.Client;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static com.client.ClientThread.socket;

public class ClientUI extends Application {

    private static ClientThread client;

    public static TextArea sendMessageArea;
    public static StringBuffer buffer = null;

    public static VBox leftVBox = new VBox();

    public static VBox leftUpVBox = null;

    public static VBox leftListVBox = new VBox();

    public static VBox leftTextVBox = new VBox();

    public static VBox leftBottomVBox = new VBox();

    public static VBox rightVBox = null;

    public static HBox hBox = null;

    public static Label noneClient;

    public static List<Client> clientList = ClientThread.clientList;

    public static List<CheckBox> chooseClientList;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        leftUpVBox = getLeftUpVBox();
        leftUpVBox.setPadding(new Insets(10, 5, 10, 10));

        leftListVBox = getLeftListVBox();
        leftListVBox.setPadding(new Insets(10, 5, 10, 10));

        leftTextVBox = getLeftTextVBox();
        leftTextVBox.setPadding(new Insets(10, 5, 10, 10));

        leftBottomVBox.getChildren().add(getLeftBottomVBox(false));
        leftBottomVBox.setPadding(new Insets(10, 5, 10, 10));


        leftVBox.getChildren().addAll(leftUpVBox, leftListVBox, leftTextVBox, leftBottomVBox);

        rightVBox = getRightVBox(new StringBuffer());
        rightVBox.setPadding(new Insets(10, 5, 10, 10));

        hBox = new HBox(leftVBox, rightVBox);

        Scene scene = new Scene(hBox, 860, 450);
        primaryStage.setScene(scene);
        primaryStage.setTitle("client");
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {

                if (client != null) {
                    try {
                        socket.shutdownInput();
                        socket.shutdownOutput();
                        socket.close();
                        client.getDatagramSocket().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                System.exit(0);

            }
        });

        buffer = new StringBuffer();

        startClient();
    }

    private void startClient() {

        new Thread() {
            @Override
            public void run() {
                client = new ClientThread();
                client.start();
            }
        }.start();

    }

    /**
     * 界面左上侧
     *
     * @return
     */
    private VBox getLeftUpVBox() {

        HBox ipHBox = showInfo("IPAddress：     ", "Not Connect");
        ipHBox.setPadding(new Insets(10, 10, 10, 10));
        ipHBox.setSpacing(10);

        HBox portHBox = showInfo("Port：             ", "Not Connect");
        portHBox.setPadding(new Insets(10, 10, 10, 10));
        portHBox.setSpacing(10);

        return new VBox(ipHBox, portHBox);
    }

    public static void reFreshLeftUpvBox() {

        leftUpVBox.getChildren().clear();

        HBox ipHBox = showInfo("IPAddress：", client.getLocalIPAddress());
        ipHBox.setPadding(new Insets(10, 10, 10, 10));
        ipHBox.setSpacing(10);

        HBox portHBox = showInfo("Port：         ", client.getLocalPort());
        portHBox.setPadding(new Insets(10, 10, 10, 10));
        portHBox.setSpacing(10);

        leftUpVBox.getChildren().add(new VBox(ipHBox, portHBox));
        leftUpVBox.setPadding(new Insets(10, 5, 10, 10));

    }

    private VBox getLeftListVBox() {

        Label chooseClient = new Label("Choose Client：");
        chooseClient.setFont(new Font(15));
        chooseClient.setPadding(new Insets(10, 10, 10, 10));

        VBox vBox = new VBox(chooseClient);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.setSpacing(10);
        chooseClientList = getChooseClientList(clientList);
        if (chooseClientList.size() > 0) {
            vBox.getChildren().addAll(chooseClientList);
        } else {
            noneClient = new Label("None Client");
            noneClient.setFont(new Font(15));
            noneClient.setPadding(new Insets(10, 10, 10, 10));
            vBox.getChildren().add(noneClient);
        }
        return vBox;
    }

    private VBox getLeftTextVBox() {
        Label sendMessage = new Label("Send Message：");
        sendMessage.setFont(new Font(15));
        sendMessage.setPadding(new Insets(10, 10, 10, 10));
        sendMessage.setAlignment(Pos.CENTER);

        sendMessageArea = new TextArea();
        sendMessageArea.setWrapText(true);
        sendMessageArea.setMaxWidth(410);
        sendMessageArea.setMaxHeight(900);
        return new VBox(sendMessage, sendMessageArea);
    }

    private static HBox getLeftBottomVBox(boolean isConnected) {

        Label tip = null;
        if (isConnected) {
            tip = new Label("Success Connected" + "                                ");
        } else {
            tip = new Label("Connecting..." + "                                        ");
        }

        tip.setPadding(new Insets(10, 10, 10, 10));
        tip.setAlignment(Pos.CENTER_LEFT);

        Button sendButton = new Button("sendMessage");
        sendButton.setPadding(new Insets(10, 5, 10, 10));
        sendButton.setAlignment(Pos.CENTER_RIGHT);

        sendButton.setOnMouseClicked(event -> {

            if (chooseClientList.size() > 0) {

                for (CheckBox c : chooseClientList
                ) {
                    if (c.isSelected()) {
                        if ("服务端".equals(c.getText())) {
                            String content = sendMessageArea.getText();
                            client.sendMessage(content);
                        } else {
                            Client cli = clientList.get(Integer.parseInt(c.getId()));
                            String content = sendMessageArea.getText();
                            byte[] bytes = content.getBytes();
                            try {
                                DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(cli.getIPAddress()), Integer.parseInt(cli.getPort()));
                                DatagramSocket datagramSocket = client.getDatagramSocket();
                                datagramSocket.send(datagramPacket);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });


        return new HBox(tip, sendButton);
    }

    /**
     * 界面右侧
     *
     * @return
     */
    private static VBox getRightVBox(StringBuffer buffer) {

        Label receivedMessage = new Label("Received Message：");
        receivedMessage.setFont(new Font(15));

        receivedMessage.setPadding(new Insets(10, 10, 10, 10));
        receivedMessage.setAlignment(Pos.CENTER);

        TextArea receivedMessageArea = new TextArea(buffer.toString());
        receivedMessageArea.setWrapText(true);
        receivedMessageArea.setWrapText(true);
        receivedMessageArea.setMaxWidth(410);
        receivedMessageArea.setMaxHeight(900);


        return new VBox(receivedMessage, receivedMessageArea);
    }

    public static void reFreshList() {
        leftListVBox.getChildren().remove(noneClient);
        leftListVBox.getChildren().removeAll(chooseClientList);
        chooseClientList = getChooseClientList(clientList);
        if (chooseClientList.size() > 0) {
            leftListVBox.getChildren().addAll(chooseClientList);
        } else {
            leftListVBox.getChildren().add(noneClient);
        }
    }

    public static void reFreshReceived(String str) {
        buffer.append(str);

        hBox.getChildren().remove(rightVBox);

        rightVBox = getRightVBox(buffer);
        rightVBox.setPadding(new Insets(10, 5, 10, 10));

        hBox.getChildren().add(rightVBox);
    }

    public static void reFreshLeftBottom(boolean isConnected) {

        leftBottomVBox.getChildren().clear();
        leftBottomVBox.getChildren().add(getLeftBottomVBox(isConnected));
    }

    /**
     * 显示ip地址或端口等消息
     *
     * @param str1 名称
     * @param str2 内容
     * @return
     */
    private static HBox showInfo(String str1, String str2) {

        Label info = new Label(str1);
        info.setFont(new Font(15));

        TextField textField = new TextField(str2);
        textField.setEditable(false);

        return new HBox(info, textField);

    }

    /**
     * 获取客户端列表
     *
     * @param clientList
     * @return
     */
    private static List<CheckBox> getChooseClientList(List<Client> clientList) {

        List<CheckBox> checkBoxes = new ArrayList<>();

        for (Client c : clientList
        ) {
            if (client.getServer().getPort().equals(c.getPort())) {
                CheckBox checkBox = new CheckBox("服务端");
                checkBox.setId(String.valueOf(clientList.indexOf(c)));
                checkBoxes.add(checkBox);
            } else {
                CheckBox checkBox = new CheckBox(c.getIPAddress() + ":" + c.getPort());
                checkBox.setId(String.valueOf(clientList.indexOf(c)));
                checkBoxes.add(checkBox);
            }
        }
        return checkBoxes;
    }
}
