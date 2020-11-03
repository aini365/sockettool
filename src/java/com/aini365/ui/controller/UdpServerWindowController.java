package com.aini365.ui.controller;

import com.aini365.socket.udp.UdpSocketServer;
import com.aini365.ui.model.SocketConnectStatus;
import com.aini365.ui.model.SocketTreeItemContext;
import com.aini365.ui.model.TupleTwo;
import javafx.event.ActionEvent;

import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

public class UdpServerWindowController extends WindowBaseController {



    public void paneLoad(MainWindowController root,
                         UdpServerWindowController tcpServerController) {
        super.paneLoad(root,tcpServerController,null);
        switchConnectStatus(SocketConnectStatus.DisConnected);

    }


    @Override
    protected void handleStartButtonAction(ActionEvent event) {
        try {
            if (tcpSocketServer != null) {
                tcpSocketServer.closeAllSocket();
            }

            SocketTreeItemContext socketConfig = (SocketTreeItemContext) rootPane.getUserData();

            tcpSocketServer = new UdpSocketServer(
                    socketConfig.getCurrentTreeItem(),
                    socketConfig.getListenAddress(),
                    (AbstractSelectableChannel serverSocketChannel) -> {
                        socketConfig.setListenChannel(serverSocketChannel);
                        switchConnectStatus(SocketConnectStatus.Connected);
                        return true;
                    },
                    (String exception) -> {
                        switchConnectStatus(SocketConnectStatus.DisConnected);
                        return true;
                    },
                    (String exception) -> {
                        switchConnectStatus(SocketConnectStatus.DisConnected);
                        return true;
                    },
                    (TupleTwo<Object,byte[]> receiveObj) -> {
                        if(receiveObj.getFirst() instanceof SocketAddress) {
                            appendReceiveData(receiveObj.getFirst().toString(),receiveObj.getSecond());
                        }
                        return true;
                    });
            switchConnectStatus(SocketConnectStatus.Connecting);
            tcpSocketServer.start();

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
        tcpSocketServer.closeAllSocket();
    }

    public void stopClient(SocketChannel channel){
        tcpSocketServer.stopClient(channel);
    }


}
