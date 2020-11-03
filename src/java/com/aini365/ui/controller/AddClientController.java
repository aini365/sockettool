package com.aini365.ui.controller;

import com.aini365.util.InputCheckUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AddClientController extends AddBaseController {

    @FXML
    private TextField txtTcpIp;

    @FXML
    private TextField txtTcpPort;

    @FXML
    protected void handleConfirmAction(ActionEvent event) {

        if(!InputCheckUtil.isIp(txtTcpIp.getText())){
            alert("输入错误","ip输入错误");
            return;
        }
        if(!InputCheckUtil.isInterger(txtTcpPort.getText())){
            alert("输入错误","端口输入错误");
            return;
        }

        parentController.addTcpClientNode(treeItemType,txtTcpIp.getText(),Integer.valueOf(txtTcpPort.getText()));
        this.dialogStage.hide();

    }
}
