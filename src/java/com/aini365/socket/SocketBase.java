package com.aini365.socket;
import com.aini365.ui.model.SocketTreeItem;
import com.aini365.ui.model.TupleTwo;
import com.aini365.util.ByteUtil;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayList;
import java.util.function.Function;

public abstract class SocketBase {

    protected SocketTreeItem container;

    protected Function<TupleTwo<Object, byte[]>, Boolean> onDataReceived;

    protected Function<String, Boolean> onStopped;
    protected Function<AbstractSelectableChannel, Boolean> onStartSuccess;
    protected Function<String, Boolean> onStartException;

    protected static final int BUF_SIZE = 102400;
    protected static final int TIMEOUT = 3000;



    protected abstract void onReceiveException(SelectableChannel sc);

    protected void handleRead(SelectionKey key) {

        try {
            if (!key.isValid()) {
                return;
            }
            SelectableChannel sc = key.channel();
            if (sc == null) {
                return;
            }
            ByteBuffer buf = (ByteBuffer) key.attachment();
            if(sc.isOpen()){
                long bytesRead = 0;
                Object clientSocket=null;
                if(sc instanceof SocketChannel){
                    clientSocket =sc;
                    bytesRead = ((SocketChannel)sc).read(buf);
                } else if(sc instanceof DatagramChannel){
                    clientSocket = ((DatagramChannel)sc).receive(buf);
                    bytesRead = buf.position();
                }

                if (bytesRead > 0) {
                    buf.flip();
                    ArrayList<Byte> receiveData = new ArrayList<>();
                    while (buf.hasRemaining()) {
                        receiveData.add(buf.get());
                    }
                    if (receiveData.size() > 0 && onDataReceived != null) {
                        onDataReceived.apply(new TupleTwo<>(clientSocket, ByteUtil.arrayListToBytes(receiveData)));
                    }
                    buf.clear();
                    sc.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocateDirect(BUF_SIZE));
                }
                if (bytesRead == -1) {
                    onReceiveException(sc);
                }
            } else{
                onReceiveException(sc);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }




    protected void handleWrite(SelectionKey key) {
        try {
            if (!key.isValid()) {
                return;
            }
            ByteBuffer buf = (ByteBuffer) key.attachment();
            buf.flip();
            SocketChannel sc = (SocketChannel) key.channel();
            while (buf.hasRemaining()) {
                sc.write(buf);
            }
            buf.compact();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public abstract void start();
    public void closeAllSocket(){

    }
    public void stopClient(SocketChannel channel){

    }

    public void closeSocket(){

    }

    public  void sendData(SocketChannel receiveClient, byte[] data){

    }

    public  void sendData(byte[] data){
        System.out.println("xxx");
    }


}
