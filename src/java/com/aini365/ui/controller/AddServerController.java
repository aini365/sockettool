package com.aini365.ui.controller;

import com.aini365.util.InputCheckUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AddServerController extends AddBaseController {

    @FXML
    private TextField txtTcpPort;

    @FXML
    protected void handleConfirmAction(ActionEvent event) {

        if(!InputCheckUtil.isInterger(txtTcpPort.getText())){
            alert("输入错误","端口输入错误");
            return;
        }
        parentController.addServerNode(treeItemType,Integer.valueOf(txtTcpPort.getText()));
        this.dialogStage.hide();

    }

}
