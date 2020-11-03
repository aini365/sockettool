package com.aini365.socket.tcp;

import com.aini365.ui.model.SocketContext;
import com.aini365.ui.model.SocketTreeItem;
import com.aini365.ui.model.TupleTwo;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.function.Function;

public class TcpSocketClient extends TcpSocketBase {

    SocketChannel socketChannel;
    InetSocketAddress connectAddress;

    public TcpSocketClient(InetSocketAddress connectAddress,
                           SocketTreeItem container,
                           Function<String, Boolean> onStopped,
                           Function<SocketContext, Boolean> onConnected,
                           Function<SocketContext, Boolean> onConnectRefused,
                           Function<TupleTwo<Object, byte[]>, Boolean> onReadData) {
        this.connectAddress = connectAddress;
        this.container = container;
        this.onDataReceived = onReadData;
        this.onStopped = onStopped;
        this.onConnected = onConnected;
        this.onConnectRefused = onConnectRefused;
    }

    public void start() {
        selector();
    }

    private void selector() {
        try {

            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT, ByteBuffer.allocateDirect(BUF_SIZE));
            socketChannel.connect(connectAddress);


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
                                    if (key.isValid() && key.isConnectable()) {
                                        try {
                                            if (!socketChannel.finishConnect()) {
                                                continue;
                                            }
                                            handleConnect(key);
                                        } catch (ConnectException connectEx) {
                                            if (onConnectRefused != null) {
                                                onConnectRefused.apply(new SocketContext(container,null,socketChannel));
                                            }
                                            connectEx.printStackTrace();
                                        }
                                    }
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

    private void handleConnect(SelectionKey key) throws IOException {
        try {
            if (!key.isValid()) {
                return;
            }
            SocketChannel sc = (SocketChannel) key.channel();
            if (sc != null) {
                sc.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocateDirect(BUF_SIZE));
                if (onConnected != null) {
                    onConnected.apply(new SocketContext(container, null,sc));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
                        socketChannel.write(outBuf);    // 输出内容
                    }
                    outBuf.clear();
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void closeSocket() {
        if (socketChannel != null) {
            closeSocket(socketChannel);
        }
    }

    @Override
    protected void onSocketClosed(SocketChannel sc) {
        if (container != null) {
            container.onSocketClosed();
        }
    }
}
