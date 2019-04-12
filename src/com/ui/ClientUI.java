package com.ui;

import com.client.ClientThread;
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

public class ClientUI extends Application {

    private static ClientThread client = new ClientThread();
    ;
    public static TextArea sendMessageArea;
    public static StringBuffer buffer = null;

    public static VBox leftVBox = new VBox();

    public static VBox leftUpVBox = null;


    public static VBox leftTextVBox = new VBox();

    public static VBox leftBottomVBox = new VBox();

    public static VBox rightVBox = null;

    public static HBox hBox = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        leftUpVBox = getLeftUpVBox();
        leftUpVBox.setPadding(new Insets(10, 5, 10, 10));


        leftBottomVBox.getChildren().add(getLeftBottomVBox(false));
        leftBottomVBox.setPadding(new Insets(10, 5, 10, 10));

        leftTextVBox = getLeftTextVBox();
        leftTextVBox.setPadding(new Insets(10, 5, 10, 10));

        leftVBox.getChildren().addAll(leftUpVBox, leftTextVBox, leftBottomVBox);

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

                try {
                    ClientThread.socket.shutdownInput();
                    ClientThread.socket.shutdownOutput();
                    ClientThread.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.exit(0);

            }
        });

        buffer = new StringBuffer();
//        client = new ClientThread();
        client.start();
    }

    /**
     * 界面左上侧
     *
     * @return
     */
    private VBox getLeftUpVBox() {


        HBox ipHBox = showInfo("IPAddress：", client.getLocalIPAddress());
        ipHBox.setPadding(new Insets(10, 10, 10, 10));
        ipHBox.setSpacing(10);

        HBox portHBox = showInfo("Port：         ", client.getLocalPort());
        portHBox.setPadding(new Insets(10, 10, 10, 10));
        portHBox.setSpacing(10);

        return new VBox(ipHBox, portHBox);
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
            String content = sendMessageArea.getText();
            client.sendMessage(content);
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
        receivedMessageArea.setWrapText(true); receivedMessageArea.setWrapText(true);
        receivedMessageArea.setMaxWidth(410);
        receivedMessageArea.setMaxHeight(900);


        return new VBox(receivedMessage, receivedMessageArea);
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
    private HBox showInfo(String str1, String str2) {

        Label info = new Label(str1);
        info.setFont(new Font(15));

        TextField textField = new TextField(str2);
        textField.setEditable(false);

        return new HBox(info, textField);

    }
}
