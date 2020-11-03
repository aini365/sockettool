package com.aini365.ui.model;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

public class SocketContext {

    SocketTreeItem parentTreeItem;

    public SocketTreeItem getParentTreeItem() {
        return parentTreeItem;
    }


    public SocketContext(SocketTreeItem parentTreeItem) {
        this.parentTreeItem= parentTreeItem;
    }

    public SocketContext(SocketTreeItem parentTreeItem,AbstractSelectableChannel listenChannel, SocketChannel newConnectedChannel) {
        this.parentTreeItem= parentTreeItem;
        this.listenChannel = listenChannel;
        this.newConnectedChannel = newConnectedChannel;
    }

    public AbstractSelectableChannel getListenChannel() {
        return listenChannel;
    }

    AbstractSelectableChannel listenChannel;



    public SocketChannel getNewConnectedChannel() {
        return newConnectedChannel;
    }

    public void setNewConnectedChannel(SocketContext sc) {
        if(sc!=null) {
            this.newConnectedChannel = sc.getNewConnectedChannel();
        }
    }

    SocketChannel newConnectedChannel;
}
