package com.aini365.socket.udp;

import com.aini365.socket.SocketBase;
import com.aini365.socket.tcp.TcpSocketThreadPool;
import com.aini365.ui.model.SocketTreeItem;
import com.aini365.ui.model.TupleTwo;
import com.aini365.util.ByteUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class UdpSocketServer extends SocketBase {

    DatagramChannel serverSocketChannel;
    Set<SocketChannel> clientSocketChannels;

    InetSocketAddress listenAddress;


    public UdpSocketServer(SocketTreeItem container,
                           InetSocketAddress listenAddress,
                           Function<AbstractSelectableChannel, Boolean> onStartSuccess,
                           Function<String, Boolean> onStartException,
                           Function<String, Boolean> onStopped,
                           Function<TupleTwo<Object, byte[]>, Boolean> onReadData) {
        this.listenAddress = listenAddress;
        this.container = container;
        this.onDataReceived = onReadData;
        this.onStopped = onStopped;
        this.onStartSuccess = onStartSuccess;
        this.onStartException = onStartException;
        clientSocketChannels = new HashSet<>();
    }


    public void start() {
        try {
            selector();
            if (onStartSuccess != null) {
                onStartSuccess.apply(serverSocketChannel);
            }
        } catch (IOException e) {
            if (onStartException != null) {
                onStartException.apply(e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void selector() throws IOException {


        serverSocketChannel = DatagramChannel.open();
        serverSocketChannel.socket().bind(listenAddress);


        TcpSocketThreadPool.submit(() -> {
            try {

                ByteBuffer buf = ByteBuffer.allocateDirect(BUF_SIZE);
                while (true) {
                    buf.clear();

                    SocketAddress socketAddress = serverSocketChannel.receive(buf);
                    buf.flip();
                    ArrayList<Byte> receiveData = new ArrayList<>();
                    while (buf.hasRemaining()) {
                        receiveData.add(buf.get());
                    }
                    if (receiveData.size() > 0 && onDataReceived != null) {
                        onDataReceived.apply(new TupleTwo<>(socketAddress, ByteUtil.arrayListToBytes(receiveData)));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();

            }

        });
    }


    @Override
    protected void onReceiveException(SelectableChannel sc) {

    }


    @Override
    public void closeAllSocket() {
        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (onStopped != null) {
            onStopped.apply("");
        }
    }

    @Override
    public void stopClient(SocketChannel channel) {

    }

}
