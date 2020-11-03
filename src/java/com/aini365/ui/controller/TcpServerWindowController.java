package com.aini365.ui.controller;

import com.aini365.socket.tcp.TcpSocketServer;
import com.aini365.ui.model.SocketConnectStatus;
import com.aini365.ui.model.SocketContext;
import com.aini365.ui.model.SocketTreeItemContext;
import com.aini365.ui.model.TupleTwo;
import javafx.event.ActionEvent;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

public class TcpServerWindowController extends WindowBaseController {



    public void paneLoad(MainWindowController root,
                         TcpServerWindowController tcpServerController,
                         SocketChannel acceptSocketChannel) {
        super.paneLoad(root,tcpServerController,acceptSocketChannel);
        switchConnectStatus(SocketConnectStatus.DisConnected);



    }


    @Override
    protected void handleStartButtonAction(ActionEvent event) {
        try {
            if (tcpSocketServer != null) {
                tcpSocketServer.closeAllSocket();
            }

            SocketTreeItemContext socketConfig = (SocketTreeItemContext) rootPane.getUserData();

            tcpSocketServer = new TcpSocketServer(
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
                    (SocketContext newConnectClient) -> {
                        root.addServerNodeClient(this,newConnectClient);
                        return true;
                    },
                    (TupleTwo<Object,byte[]> receiveObj) -> {
                        notifyChildController(receiveObj);
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
    public void sendData(SocketChannel receiveClient,byte[] data){
        tcpSocketServer.sendData(receiveClient,data);
    }


    private void notifyChildController(TupleTwo<Object,byte[]> receiveObj){
        for (WindowBaseController child:this.childController){
            if(receiveObj.getFirst() instanceof SocketChannel) {
                child.appendReceiveData((SocketChannel)receiveObj.getFirst(), receiveObj.getSecond());
            }
        }
    }



    @Override
    protected void handleStopButtonAction(ActionEvent event) {

        tcpSocketServer.closeAllSocket();
    }

    @Override
    public void stopClient(SocketChannel channel){
        tcpSocketServer.stopClient(channel);
    }


}
