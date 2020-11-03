package com.aini365.ui.model;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

public class SocketTreeItemContext extends SocketContext {


    private SocketTreeItem currentTreeItem;
    InetSocketAddress listenAddress;
    InetSocketAddress newConnectedAddress;


    public SocketTreeItemContext(SocketTreeItem parentTreeItem,
                                 AbstractSelectableChannel listenChannel,
                                 SocketChannel newConnectedChannel){
        super(parentTreeItem,listenChannel,newConnectedChannel);
        this.parentTreeItem=parentTreeItem;
    }

    public SocketTreeItemContext(SocketTreeItem parentTreeItem,
                                 InetSocketAddress listenAddress,
                                 InetSocketAddress newConnectedAddress){
        super(parentTreeItem);
        this.listenAddress=listenAddress;
        this.newConnectedAddress=newConnectedAddress;
    }


    public InetSocketAddress getListenAddress() {
        return listenAddress;
    }






    public AbstractSelectableChannel getListenChannel() {
        return listenChannel;
    }

    public void setListenChannel(AbstractSelectableChannel listenChannel) {
        this.listenChannel = listenChannel;
    }

    public InetSocketAddress getNewConnectedAddress() {
        return newConnectedAddress;
    }




    public SocketTreeItem getCurrentTreeItem() {
        return currentTreeItem;
    }

    public void setCurrentTreeItem(SocketTreeItem currentTreeItem) {
        this.currentTreeItem = currentTreeItem;
    }




}
