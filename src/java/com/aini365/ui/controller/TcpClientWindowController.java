package com.aini365.ui.controller;

import com.aini365.protocols.Analyzer;
import com.aini365.socket.tcp.TcpSocketClient;
import com.aini365.protocols.EZSPAnalyzer;
import com.aini365.ui.model.SocketConnectStatus;
import com.aini365.ui.model.SocketContext;
import com.aini365.ui.model.SocketTreeItemContext;
import com.aini365.ui.model.TupleTwo;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

import java.nio.channels.SocketChannel;

public class TcpClientWindowController extends WindowBaseController {



    Analyzer analyzer = new EZSPAnalyzer((d)->{
        appendReceiveData("ezsp", d);
        return true;
    });

    public void paneLoad(MainWindowController root,
                         TcpServerWindowController tcpServerController,
                         SocketChannel acceptSocketChannel) {
        super.paneLoad(root,tcpServerController,acceptSocketChannel);
        if (acceptSocketChannel!=null) {
            switchConnectStatus(SocketConnectStatus.Connected);

            Platform.runLater(() -> {
                    lblRemoteIp.setText(acceptSocketChannel.socket().getInetAddress().toString().replace("/",""));
                    lblRemotePort.setText(String.valueOf(acceptSocketChannel.socket().getPort()));
                    lblLocalPort.setText(String.valueOf(acceptSocketChannel.socket().getLocalPort()));
            });

        } else {
            switchConnectStatus(SocketConnectStatus.DisConnected);
            SocketTreeItemContext socketConfig = (SocketTreeItemContext) rootPane.getUserData();
            Platform.runLater(() -> {
                lblRemoteIp.setText(String.valueOf(socketConfig.getNewConnectedAddress().getAddress().getHostAddress()));
                lblRemotePort.setText(String.valueOf(socketConfig.getNewConnectedAddress().getPort()));
            });
        }
    }


    @Override
    protected void handleStartButtonAction(ActionEvent event) {
        try {

            if (serverController != null) {
                return;
            }

            if (tcpSocketClient != null) {
                tcpSocketClient.closeSocket();
            }

            SocketTreeItemContext socketConfig = (SocketTreeItemContext) rootPane.getUserData();

            tcpSocketClient = new TcpSocketClient(socketConfig.getNewConnectedAddress(),
                    socketConfig.getCurrentTreeItem(),
                    (String exception) -> {
                        switchConnectStatus(SocketConnectStatus.DisConnected);
                        return true;
                    },
                    (SocketContext sc) -> {
                        socketConfig.setNewConnectedChannel(sc);

                        Platform.runLater(()->{
                                lblLocalPort.setText(String.valueOf(sc.getNewConnectedChannel().socket().getLocalPort()));
                        });
                        switchConnectStatus(SocketConnectStatus.Connected);
                        return true;
                    },
                    (SocketContext sc) -> {
                        switchConnectStatus(SocketConnectStatus.DisConnected);
                        return true;
                    },
                    (TupleTwo<Object, byte[]> receiveObj) -> {
                        totalReceiveBytes += receiveObj.getSecond().length;

                        if(selProtocols!=null &&
                                "EZSP".equals(selProtocols.getSelectionModel().getSelectedItem())){
                            analyzer.store(receiveObj.getSecond());
                        }
                        else{
                            appendReceiveData("",receiveObj.getSecond());
                        }
                        return true;
                    });

            tcpSocketClient.start();

            switchConnectStatus(SocketConnectStatus.Connecting);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (tcpSocketServer != null) {
                tcpSocketServer.closeAllSocket();
            }
            if (tcpSocketClient != null) {
                tcpSocketClient.closeSocket();
            }
        }

    }

    @Override
    protected void handleStopButtonAction(ActionEvent event) {
        if (acceptSocketChannel!=null) {
            serverController.stopClient(acceptSocketChannel);
        } else {
            tcpSocketClient.closeSocket();
        }
        switchConnectStatus(SocketConnectStatus.DisConnected);
    }

}
