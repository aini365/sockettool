package com.aini365.ui.controller;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;

public abstract class LayoutBaseController {

    @FXML
    protected BorderPane rootPane;

    public ArrayList<WindowBaseController> childController=new ArrayList<>();


}
