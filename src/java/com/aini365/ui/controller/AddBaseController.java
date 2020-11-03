package com.aini365.ui.controller;

import com.aini365.ui.model.SocketTreeItemType;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public class AddBaseController {
    protected SocketTreeItemType treeItemType;
    protected Stage dialogStage;
    protected MainWindowController parentController;

    public final void Init(MainWindowController parentController,
                           SocketTreeItemType treeItemType,
                           Stage dialogStage) {
        this.parentController = parentController;
        this.treeItemType =treeItemType;
        this.dialogStage = dialogStage;
    }

    protected void alert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                content, new ButtonType("取消", ButtonBar.ButtonData.NO),
                new ButtonType("确定", ButtonBar.ButtonData.YES));
        //设置窗口的标题
        alert.setTitle("确认");
        alert.setHeaderText(title);
        //设置对话框的 icon 图标，参数是主窗口的 stage
        alert.initOwner(dialogStage);
        //showAndWait() 将在对话框消失以前不会执行之后的代码
        Optional<ButtonType> _buttonType = alert.showAndWait();
        //        根据点击结果返回
        if (_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
            //return true;
        } else {
            //return false;
        }
    }
}
