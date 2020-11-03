package com.aini365.socket.udp;

import com.aini365.socket.SocketBase;
import com.aini365.socket.tcp.TcpSocketThreadPool;
import com.aini365.ui.model.SocketTreeItem;
import com.aini365.ui.model.TupleTwo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.function.Function;

public class UdpSocketClient extends SocketBase {
    DatagramChannel socketChannel;
    InetSocketAddress connectAddress;

    public UdpSocketClient(SocketTreeItem container,
                           InetSocketAddress connectAddress,
                           Function<TupleTwo<Object, byte[]>, Boolean> onReadData) {
        this.connectAddress = connectAddress;
        this.container = container;
        this.onDataReceived = onReadData;
    }

    public void start() {
        selector();
    }

    private void selector() {
        try {

            socketChannel = DatagramChannel.open();
            socketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocateDirect(BUF_SIZE));

            TcpSocketThreadPool.submit(() -> {

                try {

                    while (true) {
                        if (selector.select(TIMEOUT) == 0) {
                            continue;
                        }
                        Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                        while (iter.hasNext()) {
                            SelectionKey key = iter.next();
                            if (key.isValid()) {
                                try {
                                    if (key.isValid() && key.isReadable()) {
                                        handleRead(key);
                                    }
                                    if (key.isValid() && key.isWritable()) {
                                        handleWrite(key);
                                    }
                                    if (key.isValid() && key.isConnectable()) {
                                        System.out.println("isConnectable = true");
                                    }
                                } catch (CancelledKeyException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            iter.remove();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();

                } finally {
                    try {
                        if (selector != null) {
                            selector.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    try {
                        if (socketChannel != null) {
                            socketChannel.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            });


        } catch (IOException e) {
            try {
                if (socketChannel != null) {
                    socketChannel.close();
                }
            } catch (IOException ex) {
                e.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void sendData(byte[] data) {
        try {
            ByteBuffer outBuf = ByteBuffer.allocateDirect(BUF_SIZE);
            for (int i = 0; i < data.length; i++) {
                outBuf.put(data[i]);    // 向缓冲区中设置内容

                if (outBuf.position() == BUF_SIZE || i == (data.length - 1)) {

                    outBuf.flip();
                    while (outBuf.hasRemaining()) {
                        socketChannel.send(outBuf,connectAddress);    // 输出内容
                    }
                    outBuf.clear();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    protected void onReceiveException(SelectableChannel sc) {

    }
}
