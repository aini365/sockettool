package com.aini365.socket.tcp;

import com.aini365.ui.controller.WindowBaseController;
import com.aini365.ui.model.SocketConnectStatus;
import com.aini365.ui.model.SocketContext;
import com.aini365.ui.model.SocketTreeItem;
import com.aini365.ui.model.TupleTwo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class TcpSocketServer extends TcpSocketBase {

    ServerSocketChannel serverSocketChannel;
    Set<SocketChannel> clientSocketChannels;

    InetSocketAddress listenAddress;


    public TcpSocketServer(SocketTreeItem container,
                           InetSocketAddress listenAddress,
                           Function<AbstractSelectableChannel, Boolean> onStartSuccess,
                           Function<String, Boolean> onStartException,
                           Function<String, Boolean> onStopped,
                           Function<SocketContext, Boolean> onConnected,
                           Function<TupleTwo<Object, byte[]>, Boolean> onReadData) {
        this.listenAddress = listenAddress;
        this.container = container;
        this.onDataReceived = onReadData;
        this.onStopped = onStopped;
        this.onStartSuccess = onStartSuccess;
        this.onStartException = onStartException;
        this.onConnected = onConnected;
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
            closeAllSocket();
            e.printStackTrace();
        }
    }

    private void selector() throws IOException {


        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(listenAddress);
        serverSocketChannel.configureBlocking(false);


        TcpSocketThreadPool.submit(() -> {
            Selector selector = null;
            try {
                selector = Selector.open();
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                while (true) {
                    if (selector.select(TIMEOUT) == 0) {
                        continue;
                    }
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        if (key.isValid()) {
                            try {
                                if (key.isValid() && key.isAcceptable()) {
                                    handleAccept(key);
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
                closeAllSocket();
            }

        });


    }

    private void handleAccept(SelectionKey key) throws IOException {
        try {
            if (!key.isValid()) {
                return;
            }
            ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
            if (ssChannel == null) {
                return;
            }
            SocketChannel sc = ssChannel.accept();
            if (sc != null) {
                clientSocketChannels.add(sc);
                sc.configureBlocking(false);
                sc.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocateDirect(BUF_SIZE));

                if (onConnected != null) {
                    onConnected.apply(new SocketContext(container, ssChannel, sc));
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void sendData(SocketChannel receiveClient, byte[] data) {
        try {

            for (SocketChannel client : clientSocketChannels) {
                if (client == null || client.socket() == null) {
                    continue;
                }

                if (client.equals(receiveClient) || receiveClient == null) {
                    ByteBuffer outBuf = ByteBuffer.allocateDirect(BUF_SIZE);    //
                    for (int i = 0; i < data.length; i++) {
                        outBuf.put(data[i]);    // 向缓冲区中设置内容

                        if (outBuf.position() == BUF_SIZE || i == (data.length - 1)) {

                            outBuf.flip();
                            while (outBuf.hasRemaining()) {
                                client.write(outBuf);    // 输出内容
                            }
                            outBuf.clear();
                        }

                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void closeAllSocket() {

        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        if (clientSocketChannels != null) {
            for (SocketChannel channel : clientSocketChannels) {
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        if (onStopped != null) {
            onStopped.apply("");
        }
    }


    public void stopClient(SocketChannel stopClient) {
        if (clientSocketChannels != null) {

            Iterator<SocketChannel> iterator = clientSocketChannels.iterator();
            SocketChannel channel;
            while (iterator.hasNext()) {
                channel = iterator.next();
                if (stopClient.equals(channel)) {
                    closeSocket(channel);
                    iterator.remove();
                }
            }
        }
    }


    @Override
    protected void onSocketClosed(SocketChannel sc) {

        for (WindowBaseController c : container.getContentController().childController) {
            if (c != null && c.getAcceptSocketChannel() != null
                    && c.getAcceptSocketChannel().equals(sc)) {
                c.switchConnectStatus(SocketConnectStatus.DisConnected);
            }
        }

    }
}
