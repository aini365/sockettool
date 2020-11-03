package com.aini365.ui.controller;

import com.aini365.ui.controller.LayoutBaseController;
import com.aini365.ui.controller.WindowBaseController;
import com.aini365.ui.model.SocketContext;
import com.aini365.ui.model.SocketTreeItemContext;
import com.aini365.ui.model.SocketTreeItem;
import com.aini365.ui.model.SocketTreeItemType;
import com.aini365.ui.controller.TcpServerWindowController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.InetSocketAddress;

public class MainWindowController extends LayoutBaseController {

    @FXML
    protected TreeView treeView;

    protected Stage primaryStage;
    protected Stage tcpServerStage;
    protected Stage tcpClientStage;

    protected Stage udpServerStage;
    protected Stage udpClientStage;


    private static final SocketTreeItem root = new SocketTreeItem("", SocketTreeItemType.ROOT, null);
    private static final SocketTreeItem tcpServerTreeItem = new SocketTreeItem("TCP Server", SocketTreeItemType.TCP_SERVER, null);
    private static final SocketTreeItem tcpClientTreeItem = new SocketTreeItem("TCP Client", SocketTreeItemType.TCP_CLIENT, null);
    private static final SocketTreeItem udpServerTreeItem = new SocketTreeItem("UDP Server", SocketTreeItemType.UDP_SERVER, null);
    private static final SocketTreeItem udpClientTreeItem = new SocketTreeItem("UDP Client", SocketTreeItemType.UDP_CLIENT, null);


    public void initialize(Stage primaryStage,
                           Stage tcpServerStage,
                           Stage tcpClientStage,
                           Stage udpServerStage,
                           Stage udpClientStage) {
        this.primaryStage = primaryStage;
        this.tcpServerStage = tcpServerStage;
        this.tcpClientStage = tcpClientStage;
        this.udpServerStage = udpServerStage;
        this.udpClientStage = udpClientStage;


        root.getChildren().add(tcpServerTreeItem);
        root.getChildren().add(tcpClientTreeItem);
        root.getChildren().add(udpServerTreeItem);
        root.getChildren().add(udpClientTreeItem);

        treeView.setRoot(root);
        treeView.setShowRoot(false);
    }

    @FXML
    protected void handleTreeOnMouseClicked(MouseEvent event) {

        SocketTreeItem selectItem = (SocketTreeItem) treeView.getSelectionModel().getSelectedItem();

        if (selectItem == null) {
            return;
        }

        rootPane.setCenter(selectItem.getContentPane());
    }


    @FXML
    protected void handleCreateButtonAction(ActionEvent event) {

        SocketTreeItem selectItem = (SocketTreeItem) treeView.getSelectionModel().getSelectedItem();
        if (selectItem == null) {
            return;
        }

        switch (selectItem.getItemType()) {
            case TCP_SERVER:
                tcpServerStage.show();
                break;
            case TCP_CLIENT:
                tcpClientStage.show();
                break;
            case UDP_SERVER:
                udpServerStage.show();
                break;
            case UDP_CLIENT:
                udpClientStage.show();
                break;
        }

    }

    @FXML
    protected void handleDeleteButtonAction(ActionEvent event) {
        SocketTreeItem selectItem = (SocketTreeItem) treeView.getSelectionModel().getSelectedItem();
        if (selectItem == null) {
            return;
        }

        switch (selectItem.getItemType()) {
            case TCP_SERVER_NODE:
            case TCP_SERVER_NODE_CLIENT:
            case TCP_CLIENT_NODE:
                selectItem.dispose();
                selectItem.getParent().getChildren().remove(selectItem);
                break;
        }
    }

    @FXML
    protected void handleQuitButtonAction(ActionEvent event) {
        Platform.exit();
    }


    public void addServerNode(SocketTreeItemType treeItemType, int port) {
        InetSocketAddress listenAddress = new InetSocketAddress(port);
        SocketTreeItem addItem = null;
        if (treeItemType == SocketTreeItemType.TCP_SERVER_NODE) {

            addItem = new SocketTreeItem(getTreeNodeName(listenAddress),
                    SocketTreeItemType.TCP_SERVER_NODE,
                    new SocketTreeItemContext(tcpServerTreeItem, listenAddress, null));
            WindowBaseController controller = addItem.getContentController();
            controller.paneLoad(this, null, null);
            tcpServerTreeItem.getChildren().add(addItem);
            tcpServerTreeItem.setExpanded(true);
        } else if(treeItemType == SocketTreeItemType.UDP_SERVER_NODE){
            addItem = new SocketTreeItem(getTreeNodeName(listenAddress),
                    SocketTreeItemType.UDP_SERVER_NODE,
                    new SocketTreeItemContext(udpServerTreeItem, listenAddress, null));
            WindowBaseController controller = addItem.getContentController();
            controller.paneLoad(this, null, null);
            udpServerTreeItem.getChildren().add(addItem);
            udpServerTreeItem.setExpanded(true);
        }

    }

    public void addServerNodeClient(WindowBaseController tcpServerController,
                                    SocketContext socketTreeItemContext) {

        InetSocketAddress connectAddress = (InetSocketAddress) socketTreeItemContext.getNewConnectedChannel().socket().getRemoteSocketAddress();
        SocketTreeItem addItem = new SocketTreeItem(getTreeNodeName(connectAddress),
                SocketTreeItemType.TCP_SERVER_NODE_CLIENT,
                new SocketTreeItemContext(socketTreeItemContext.getParentTreeItem(),
                        socketTreeItemContext.getListenChannel(),
                        socketTreeItemContext.getNewConnectedChannel()));
        WindowBaseController controller = addItem.getContentController();
        controller.paneLoad(this, tcpServerController, socketTreeItemContext.getNewConnectedChannel());
        tcpServerController.childController.add(controller);
        socketTreeItemContext.getParentTreeItem().getChildren().add(addItem);
        socketTreeItemContext.getParentTreeItem().setExpanded(true);
    }

    public void addTcpClientNode(SocketTreeItemType treeItemType, String ip, int port) {

        InetSocketAddress connectAddress = new InetSocketAddress(ip, port);
        SocketTreeItem addItem = null;

        if (treeItemType == SocketTreeItemType.TCP_CLIENT_NODE) {
            addItem = new SocketTreeItem(getTreeNodeName(connectAddress),
                    SocketTreeItemType.TCP_CLIENT_NODE, new SocketTreeItemContext(tcpClientTreeItem, null, connectAddress));
            WindowBaseController controller = addItem.getContentController();
            controller.paneLoad(this, null, null);
            tcpClientTreeItem.getChildren().add(addItem);
            tcpClientTreeItem.setExpanded(true);
        } else if (treeItemType == SocketTreeItemType.UDP_CLIENT_NODE) {
            addItem = new SocketTreeItem(getTreeNodeName(connectAddress),
                    SocketTreeItemType.UDP_CLIENT_NODE, new SocketTreeItemContext(udpClientTreeItem, null, connectAddress));
            WindowBaseController controller = addItem.getContentController();
            controller.paneLoad(this, null, null);
            udpClientTreeItem.getChildren().add(addItem);
            udpClientTreeItem.setExpanded(true);
        }
    }

    private String getTreeNodeName(InetSocketAddress address) {
        return String.format("%s[%d]", address.getAddress().toString(), address.getPort());
    }
}
