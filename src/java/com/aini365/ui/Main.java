package com.aini365.ui;

import com.aini365.ui.controller.AddBaseController;
import com.aini365.ui.controller.MainWindowController;
import com.aini365.ui.model.SocketTreeItemType;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

//        Parent root = FXMLLoader.load(getClass().getResource("mainWindow.fxml"));

        URL location = getClass().getResource("fxml/mainWindow.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();

        primaryStage.setTitle("Socket Tool");
        primaryStage.setScene(new Scene(root, 800, 600));

        MainWindowController mainController = fxmlLoader.getController();   //获取Controller的实例对象


        // add tcp server
        final Stage stageTcpServer = new Stage();
        FXMLLoader tcpServerFxmlLoader = new FXMLLoader(getClass().getResource("fxml/addServer.fxml"),
                null,new JavaFXBuilderFactory());
        stageTcpServer.initModality(Modality.APPLICATION_MODAL);
        stageTcpServer.initOwner(primaryStage);
        stageTcpServer.setScene(new Scene(tcpServerFxmlLoader.load(), 300, 200));
        ((AddBaseController) tcpServerFxmlLoader.getController()).Init(mainController,
                SocketTreeItemType.TCP_SERVER_NODE,
                stageTcpServer);

        // add tcp client
        final Stage stageTcpClient = new Stage();
        FXMLLoader tcpClientFxmlLoader = new FXMLLoader(getClass().getResource("fxml/addClient.fxml"),
                null,new JavaFXBuilderFactory());
        stageTcpClient.initModality(Modality.APPLICATION_MODAL);
        stageTcpClient.initOwner(primaryStage);
        stageTcpClient.setScene(new Scene(tcpClientFxmlLoader.load(), 300, 200));
        ((AddBaseController) tcpClientFxmlLoader.getController()).Init(mainController,
                SocketTreeItemType.TCP_CLIENT_NODE,
                stageTcpClient);

        // add tcp server
        final Stage stageUdpServer = new Stage();
        FXMLLoader udpServerFxmlLoader = new FXMLLoader(getClass().getResource("fxml/addServer.fxml"),
                null,new JavaFXBuilderFactory());
        stageUdpServer.initModality(Modality.APPLICATION_MODAL);
        stageUdpServer.initOwner(primaryStage);
        stageUdpServer.setScene(new Scene(udpServerFxmlLoader.load(), 300, 200));
        ((AddBaseController) udpServerFxmlLoader.getController()).Init(mainController,
                SocketTreeItemType.UDP_SERVER_NODE,
                stageUdpServer);

        // add tcp client
        final Stage stageUdpClient = new Stage();
        FXMLLoader udpClientFxmlLoader = new FXMLLoader(getClass().getResource("fxml/addClient.fxml"),
                null,new JavaFXBuilderFactory());
        stageUdpClient.initModality(Modality.APPLICATION_MODAL);
        stageUdpClient.initOwner(primaryStage);
        stageUdpClient.setScene(new Scene(udpClientFxmlLoader.load(), 300, 200));
        ((AddBaseController) udpClientFxmlLoader.getController()).Init(mainController,
                SocketTreeItemType.UDP_CLIENT_NODE,
                stageUdpClient);


        //Controller中写的初始化方法
        mainController.initialize(primaryStage,stageTcpServer,stageTcpClient,stageUdpServer,stageUdpClient);

        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
