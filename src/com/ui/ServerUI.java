package com.ui;

import com.server.ServerThread;
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
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ServerUI extends Application {

    private static ServerThread serverThread = null;
    public static StringBuffer buffer = null;

    public static VBox leftVBox = new VBox();

    public static VBox leftUpVBox = null;

    public static VBox leftListVBox = new VBox();

    public static VBox leftTextVBox = new VBox();

    public static VBox leftBottomVBox = new VBox();

    public static VBox rightVBox = null;

    public static HBox hBox = null;

    public static Label noneClient;

    public static List<Socket> serverThreadList = ServerThread.socketList;

    public static List<CheckBox> chooseClientList;

    public static TextArea sendMessageArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        leftUpVBox = getLeftUpVBox();

        leftUpVBox.setPadding(new Insets(10, 10, 10, 10));
        leftUpVBox.setAlignment(Pos.CENTER);


        leftListVBox = getLeftListVBox();
        leftListVBox.setPadding(new Insets(10, 5, 10, 10));

        leftBottomVBox.getChildren().add(getLeftBottomVBox());
        leftBottomVBox.setPadding(new Insets(10, 5, 10, 10));
        leftBottomVBox.setAlignment(Pos.CENTER);

        leftTextVBox = getLeftTextVBox();
        leftTextVBox.setPadding(new Insets(10, 5, 10, 10));


        leftVBox.getChildren().addAll(leftUpVBox, leftListVBox, leftTextVBox, leftBottomVBox);
        rightVBox = getRightVBox(new StringBuffer());
        rightVBox.setPadding(new Insets(10, 5, 10, 10));

        hBox = new HBox(leftVBox, rightVBox);
        Scene scene = new Scene(hBox, 900, 550);
        primaryStage.setScene(scene);
        primaryStage.setTitle("server");
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    ServerThread.getServerSocket().close();
                    for (Socket s : serverThreadList
                    ) {
                        s.shutdownInput();
                        s.shutdownOutput();
                        s.close();
                    }
                    serverThreadList.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });

        buffer = new StringBuffer();
        serverThread = new ServerThread();
        serverThread.start();

    }

    /**
     * 界面左上侧
     *
     * @return
     */
    private VBox getLeftUpVBox() {

        HBox ipHBox = showInfo("IPAddress：", ServerThread.getLocalIPAddress());
        ipHBox.setPadding(new Insets(10, 10, 10, 10));
        ipHBox.setSpacing(10);

        HBox portHBox = showInfo("Port：         ", ServerThread.getLocalPort());
        portHBox.setPadding(new Insets(10, 10, 10, 10));
        portHBox.setSpacing(10);

        return new VBox(ipHBox, portHBox);
    }

    private VBox getLeftTextVBox() {
        Label sendMessage = new Label("Send Message：");
        sendMessage.setFont(new Font(15));
        sendMessage.setPadding(new Insets(10, 5, 10, 10));
        sendMessage.setAlignment(Pos.CENTER);

        sendMessageArea = new TextArea();
        sendMessageArea.setWrapText(true);
        sendMessageArea.setMaxWidth(410);
        sendMessageArea.setMaxHeight(900);
        return new VBox(sendMessage, sendMessageArea);
    }

    private VBox getLeftListVBox() {

        Label chooseClient = new Label("Choose Client：");
        chooseClient.setFont(new Font(15));
        chooseClient.setPadding(new Insets(10, 10, 10, 10));

        VBox vBox = new VBox(chooseClient);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.setSpacing(10);
        chooseClientList = getChooseClientList(serverThreadList);
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

    private static HBox getLeftBottomVBox() {
        Label sumOfConnectedClient = new Label(ServerThread.sumOfConnectedClient + " " + "Connect Success" + "                                ");
        sumOfConnectedClient.setPadding(new Insets(10, 10, 10, 10));
        sumOfConnectedClient.setAlignment(Pos.CENTER_LEFT);


        Button sendButton = new Button("sendMessage");
        sendButton.setPadding(new Insets(10, 5, 10, 10));
        sendButton.setAlignment(Pos.CENTER_RIGHT);

        sendButton.setOnMouseClicked(event -> {
            if (chooseClientList.size() > 0) {
                for (CheckBox c : chooseClientList
                ) {
                    if (c.isSelected()) {
                        Socket socket = serverThreadList.get(Integer.parseInt(c.getId()));
                        String content = sendMessageArea.getText();
                        serverThread.sendMessage(socket, content);
                    }
                }
            }
        });


        return new HBox(sumOfConnectedClient, sendButton);
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
        receivedMessageArea.setMaxWidth(450);
        receivedMessageArea.setMaxHeight(900);


        return new VBox(receivedMessage, receivedMessageArea);
    }

    public static void reFreshList() {
        leftListVBox.getChildren().remove(noneClient);
        leftListVBox.getChildren().removeAll(chooseClientList);
        chooseClientList = getChooseClientList(serverThreadList);
        if (chooseClientList.size() > 0) {
            leftListVBox.getChildren().addAll(chooseClientList);
            StringBuilder list = new StringBuilder();
            for (CheckBox checkBox : chooseClientList) {
                String[] info = checkBox.getText().split(":");
                String client = new String(info[0] + ":" + info[1] + ",");
                list.append(client);
            }

            list.delete(list.length() - 1, list.length());// 去掉最后一个逗号

            for (Socket socket : serverThreadList) {

                serverThread.sendMessage(socket, "client#" + list);

            }
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

    public static void reFreshLeftBottom() {

        leftBottomVBox.getChildren().clear();
        leftBottomVBox.getChildren().add(getLeftBottomVBox());
    }

    /**
     * 返回客户端连接列表，选择发送信息的对象
     *
     * @param serverThreadList
     * @return
     */
    private static List<CheckBox> getChooseClientList(List<Socket> serverThreadList) {

        List<CheckBox> checkBoxes = new ArrayList<>();

        for (Socket s : serverThreadList
        ) {
            CheckBox checkBox = new CheckBox(String.valueOf(s.getInetAddress().getHostAddress()) + ":" + s.getPort());
            checkBox.setId(String.valueOf(serverThreadList.indexOf(s)));
            checkBoxes.add(checkBox);
        }
        return checkBoxes;
    }

    /**
     * 显示ip地址或端口等消息
     *
     * @param str1 名称
     * @param str2 内容
     * @return
     */
    private HBox showInfo(String str1, String str2) {

        Label info = new Label(str1);
        info.setFont(new Font(15));

        TextField textField = new TextField(str2);
        textField.setEditable(false);

        return new HBox(info, textField);

    }
}
