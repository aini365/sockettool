package com.aini365.ui.model;

import com.aini365.ui.controller.WindowBaseController;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TreeItem;

import java.io.IOException;

public class SocketTreeItem extends TreeItem<String> {


    public Node getContentPane() {
        return contentPane;
    }

    private Node contentPane;

    public WindowBaseController getContentController() {
        return contentController;
    }

    private WindowBaseController contentController;

    public SocketTreeItemType getItemType() {
        return itemType;
    }


    private SocketTreeItemType itemType;

    public SocketTreeItem(String value, SocketTreeItemType itemType, SocketTreeItemContext treeItemContext) {
        super(value);
        this.itemType = itemType;
        if (treeItemContext != null) {
            treeItemContext.setCurrentTreeItem(this);
        }

        TupleTwo<Parent, WindowBaseController> uiObject = getPane(treeItemContext);

        this.contentPane = uiObject.getFirst();
        this.contentController = uiObject.getSecond();
        if (this.contentController != null) {
            this.contentController.initialize();
        }

    }

    public TupleTwo<Parent, WindowBaseController> getPane(Object userData) {
        Parent result = null;
        WindowBaseController controller = null;
        String resourcePath=null;
        try {
            FXMLLoader loader = null;
            switch (itemType) {
                case TCP_SERVER_NODE:
                    resourcePath = "../fxml/tcpServerWindow.fxml";
                    break;
                case UDP_SERVER_NODE:
                    resourcePath = "../fxml/udpServerWindow.fxml";
                    break;
                case TCP_SERVER_NODE_CLIENT:
                case TCP_CLIENT_NODE:
                    resourcePath ="../fxml/tcpClientWindow.fxml";
                    break;
                case UDP_CLIENT_NODE:
                    resourcePath ="../fxml/udpClientWindow.fxml";
                    break;
            }
            if(resourcePath!=null) {
                loader = new FXMLLoader(getClass().getResource(resourcePath),
                        null, new JavaFXBuilderFactory());
                result = loader.load();
                controller = loader.getController();
            }
            if (result != null) {
                result.setUserData(userData);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new TupleTwo<>(result, controller);
    }


    public void dispose() {
        disposeChild(this);
    }


    private void disposeChild(SocketTreeItem item) {

        WindowBaseController controller = item.getContentController();

        if (controller != null) {
            controller.closeSocket();
        }
        for (TreeItem child : item.getChildren()) {
            if (child != null && child instanceof SocketTreeItem) {
                disposeChild((SocketTreeItem) child);
            }
        }
    }

    public void onSocketClosed() {
        this.getContentController().switchConnectStatus(SocketConnectStatus.DisConnected);
    }


}
