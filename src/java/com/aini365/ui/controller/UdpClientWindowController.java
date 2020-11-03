package com.aini365.ui.controller;

import com.aini365.protocols.Analyzer;
import com.aini365.protocols.EZSPAnalyzer;
import com.aini365.socket.tcp.TcpSocketClient;
import com.aini365.socket.udp.UdpSocketClient;
import com.aini365.ui.model.SocketConnectStatus;
import com.aini365.ui.model.SocketContext;
import com.aini365.ui.model.SocketTreeItemContext;
import com.aini365.ui.model.TupleTwo;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class UdpClientWindowController extends WindowBaseController {


    public void paneLoad(MainWindowController rootController,
                         WindowBaseController tcpServerController,
                         SocketChannel acceptSocketChannel) {
        super.paneLoad(root,tcpServerController,acceptSocketChannel);
        if (acceptSocketChannel!=null) {

        } else {
            switchConnectStatus(SocketConnectStatus.Disable);
            SocketTreeItemContext socketConfig = (SocketTreeItemContext) rootPane.getUserData();
            Platform.runLater(() -> {
                lblRemoteIp.setText(String.valueOf(socketConfig.getNewConnectedAddress().getAddress().getHostAddress()));
                lblRemotePort.setText(String.valueOf(socketConfig.getNewConnectedAddress().getPort()));
            });

            tcpSocketClient = new UdpSocketClient(
                    socketConfig.getCurrentTreeItem(),
                    socketConfig.getNewConnectedAddress(),
                    (TupleTwo<Object, byte[]> receiveObj) -> {
                        totalReceiveBytes += receiveObj.getSecond().length;
                        if(receiveObj.getFirst() instanceof SocketAddress) {
                            appendReceiveData(receiveObj.getFirst().toString(),receiveObj.getSecond());
                        }
                        return true;
                    });

            tcpSocketClient.start();
        }
    }


    @Override
    protected void handleStartButtonAction(ActionEvent event) {

    }

    @Override
    protected void handleStopButtonAction(ActionEvent event) {

    }

    @Override
    protected void sendData(SocketChannel receiveClient, byte[] data) {

    }

    @Override
    public void stopClient(SocketChannel channel) {

    }
}
