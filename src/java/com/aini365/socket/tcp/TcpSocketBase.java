package com.aini365.socket.tcp;

import com.aini365.socket.SocketBase;
import com.aini365.ui.model.SocketContext;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.function.Function;

public abstract class TcpSocketBase extends SocketBase {

    Function<SocketContext, Boolean> onConnected;
    Function<SocketContext, Boolean> onConnectRefused;
    protected abstract void onSocketClosed(SocketChannel sc);

    protected void closeSocket(SocketChannel channel) {
        if (channel == null || !channel.isOpen()){
            return;
        }
        try {
            channel.shutdownOutput();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            channel.shutdownInput();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            channel.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    protected void onReceiveException(SelectableChannel sc) {

        if(sc instanceof SocketChannel){
            closeSocket((SocketChannel)sc);
            onSocketClosed((SocketChannel)sc);
        }

    }
}
