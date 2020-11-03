package com.aini365.ui.controller;

import com.aini365.protocols.EZSPAnalyzer;
import com.aini365.socket.SocketBase;
import com.aini365.socket.tcp.TcpSocketClient;
import com.aini365.util.DateTimeUtil;
import com.aini365.ui.model.SocketConnectStatus;
import com.aini365.ui.model.SocketSendDataStatus;
import com.aini365.ui.model.SocketTreeItemContext;
import com.aini365.ui.model.SocketTreeItemType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public abstract class WindowBaseController extends LayoutBaseController {


    protected MainWindowController root;

    protected WindowBaseController serverController;

    @FXML
    protected Label lblConnectStatus;
    @FXML
    private Button btnStart;

    @FXML
    private Button btnStop;
    @FXML
    protected Label lblRemotePort;
    @FXML
    protected Label lblRemoteIp;
    @FXML
    protected Label lblLocalPort;


    @FXML
    protected ComboBox selProtocols;



    @FXML
    protected TextArea txtReceiveData;
    @FXML
    protected TextArea txtSendData;


    @FXML
    protected ComboBox selSendProtocol;
    @FXML
    protected Button btnSendData;
    @FXML
    protected ComboBox selTimes;
    @FXML
    protected TextField txtSendInterval;
    @FXML
    protected Button btnStartRepeatSend;
    @FXML
    protected Button btnStopRepeatSend;


    @FXML
    protected CheckBox chkFormat;
    @FXML
    private Label lblTotalSendBytes;
    @FXML
    private Label lblTotalReceiveBytes;



    protected SocketBase tcpSocketServer;

    public SocketChannel getAcceptSocketChannel() {
        return acceptSocketChannel;
    }

    protected SocketChannel acceptSocketChannel;

    protected SocketBase tcpSocketClient;







    boolean stopAllSend;

    int totalSendBytes = 0;
    protected int totalReceiveBytes = 0;

    ExecutorService displayThread = Executors.newSingleThreadExecutor();

    Queue<String> receiveQueue = new ConcurrentLinkedQueue<>();

    public void initialize() {
        if (rootPane != null) {
            SocketTreeItemContext socketConfig = (SocketTreeItemContext) rootPane.getUserData();
            if (socketConfig != null && socketConfig.getListenAddress() != null) {
                lblLocalPort.setText(String.valueOf(socketConfig.getListenAddress().getPort()));
            }
            switchConnectStatus(SocketConnectStatus.DisConnected);
        }
        if(selSendProtocol!=null){
            selSendProtocol.getSelectionModel().selectFirst();
        }
        if(selProtocols!=null){
            selProtocols.getSelectionModel().selectFirst();
        }

        displayThread.submit(() -> {
            while (true) {

                int displaySendBytes = Integer.valueOf(lblTotalSendBytes.getText());
                if (displaySendBytes != totalSendBytes) {
                    Platform.runLater(() -> {
                        lblTotalSendBytes.setText(String.valueOf(totalSendBytes));
                    });
                }

                int displayReceiveBytes = Integer.valueOf(lblTotalReceiveBytes.getText());
                if (displayReceiveBytes != totalReceiveBytes) {
                    Platform.runLater(() -> {
                        lblTotalReceiveBytes.setText(String.valueOf(totalReceiveBytes));
                    });
                }
                String appendText;
                int pollRows=0;
                StringBuilder sbdAppend=new StringBuilder();
                while (pollRows<1000 && (appendText = receiveQueue.poll()) != null) {
                    sbdAppend.append(appendText);
                    sbdAppend.append("\n");
                }
                if(sbdAppend.length()>0){
                    Platform.runLater(() -> {
                        txtReceiveData.appendText(sbdAppend.toString());
                    });
                }

                Thread.sleep(1000);
            }
        });

    }

    public void paneLoad(MainWindowController rootController,WindowBaseController tcpServerController,
                                  SocketChannel acceptSocketChannel){
        this.root =rootController;
        this.serverController = tcpServerController;
        this.acceptSocketChannel = acceptSocketChannel;

    }

    public void switchSendStatus(SocketSendDataStatus status) {
        switch (status) {
            case SENDING:
                btnSendData.setDisable(true);
                btnStartRepeatSend.setDisable(true);
                selTimes.setDisable(true);
                txtSendInterval.setDisable(true);
                btnStopRepeatSend.setDisable(false);
                break;
            default:
                btnSendData.setDisable(false);
                btnStartRepeatSend.setDisable(false);
                selTimes.setDisable(false);
                txtSendInterval.setDisable(false);
                btnStopRepeatSend.setDisable(true);
                break;
        }
    }

    public void switchConnectStatus(SocketConnectStatus status) {
        SocketTreeItemContext socketConfig = (SocketTreeItemContext) rootPane.getUserData();
        if (socketConfig == null) {
            return;
        }

        SocketTreeItemType nodeType = socketConfig.getCurrentTreeItem().getItemType();
        Platform.runLater(() -> {
            switch (status) {
                case Connecting:
                    lblConnectStatus.setText(nodeType == SocketTreeItemType.TCP_SERVER ? "启动中" : "连接中");
                    btnStart.setDisable(true);
                    btnStop.setDisable(false);
                    break;
                case Connected:
                    lblConnectStatus.setText(nodeType == SocketTreeItemType.TCP_SERVER ? "已启动" : "已连接");
                    btnStart.setDisable(true);
                    btnStop.setDisable(false);
                    break;
                case DisConnected:
                    lblConnectStatus.setText(nodeType == SocketTreeItemType.TCP_SERVER ? "已停止" : "已断开");
                    btnStart.setDisable(nodeType == SocketTreeItemType.TCP_SERVER_NODE_CLIENT ? true : false);
                    btnStop.setDisable(true);
                    break;
                case Disable:
                    lblConnectStatus.setText("");
                    btnStart.setDisable(true);
                    btnStop.setDisable(true);
                    break;
            }
        });

    }

    @FXML
    protected abstract void handleStartButtonAction(ActionEvent event);

    @FXML
    protected abstract void handleStopButtonAction(ActionEvent event);

    @FXML
    protected void handleSendDataButtonAction(ActionEvent event) {
        stopAllSend = false;
        switchSendStatus(SocketSendDataStatus.SENDING);
        sendData(1, 1, (str) -> {
            switchSendStatus(SocketSendDataStatus.COMPLETEED);
            return true;
        });
    }


    protected void sendData(SocketChannel receiveClient,byte[] data){

    }

    private void sendData(final int expectSendTimes, final int interval,
                          Function<String, Boolean> onSendCompleted) {
        String sendText = txtSendData.getText();
        byte[] sendBytes;
        if (chkFormat.isSelected()) {
            sendText= sendText.replace("\n","");
            sendBytes = hexStringToBytes(sendText);
        } else {
            sendBytes = sendText.getBytes();
        }

        if("EZSP".equals(selSendProtocol.getSelectionModel().getSelectedItem())){
            sendBytes = EZSPAnalyzer.send(sendBytes);
        }

        final byte[] actualSendBytes = sendBytes;


        ExecutorService sendThread = Executors.newSingleThreadExecutor();
        sendThread.submit(() -> {
            int actualSendTimes = 0;
            while (actualSendBytes!=null && actualSendBytes.length>0 &&
                    !stopAllSend && actualSendTimes < expectSendTimes) {
                if ( acceptSocketChannel != null) {
                        serverController.sendData(acceptSocketChannel, actualSendBytes);
                        appendSendData("ha", actualSendBytes);
                } else if(tcpSocketClient!=null) {
                    tcpSocketClient.sendData(actualSendBytes);
                    appendSendData("ha", actualSendBytes);
                }

                totalSendBytes += actualSendBytes.length;

                actualSendTimes++;
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            if (onSendCompleted != null) {
                onSendCompleted.apply("send data complete");
            }
        });
    }


    @FXML
    protected void handleStartRepeatSendDataButtonAction(ActionEvent event) {
        stopAllSend = false;
        switchSendStatus(SocketSendDataStatus.SENDING);
        int sendTimes = Integer.valueOf(selTimes.getSelectionModel().getSelectedItem().toString());
        int interval = Integer.valueOf(txtSendInterval.getText());
        sendData(sendTimes, interval, (str) -> {
            switchSendStatus(SocketSendDataStatus.COMPLETEED);
            return true;
        });
    }

    @FXML
    protected void handleStopRepeatSendDataButtonAction(ActionEvent event) {
        stopAllSend = true;
        switchSendStatus(SocketSendDataStatus.COMPLETEED);
    }

    @FXML
    protected void handleClearCountButtonAction(ActionEvent event) {
        totalSendBytes = 0;
        totalReceiveBytes = 0;
    }





    protected String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    protected byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;

    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public void appendReceiveData(SocketChannel channel, byte[] d) {
        totalReceiveBytes+=d.length;
        if (acceptSocketChannel != null && acceptSocketChannel.equals(channel)) {
            if (chkFormat.isSelected()) {
                receiveQueue.add(String.format("%s Recv:0x%s", DateTimeUtil.getNowTime(), bytesToHexString(d)));
            } else {
                receiveQueue.add(String.format("%s Recv:%s", DateTimeUtil.getNowTime(), new String(d)));
            }
        }
    }

    protected void appendReceiveData(String from, byte[] d) {

        if (chkFormat.isSelected()) {
            String hex = bytesToHexString(d);
            String command = null;
            if ("EZSP".equals(selProtocols.getSelectionModel().getSelectedItem())
                    && ((d[0]&0x80)==0)
                && hex.length() >= 12) {
                command = hex.substring(8, 12);
            }
            if (command != null) {
                String tempStr = hex.substring(0, 8) + " cmd:" + hex.substring(8, 12);
                if (hex.length() > 12) {
                    tempStr += " para:" + hex.substring(12, hex.length());
                }
                hex = tempStr;
            }

            receiveQueue.add(String.format("%s Recv %s:0x%s", DateTimeUtil.getNowTime(), from, hex));
            //System.out.println(String.format("%s Recv %s:0x%s", DateTimeUtil.getNowTime(),from, hex));
        } else {
            receiveQueue.add(String.format("%s Recv %s:%s", DateTimeUtil.getNowTime(), from, new String(d)));
        }

    }

    protected void appendSendData(String from, byte[] d) {

        if (chkFormat.isSelected()) {

            receiveQueue.add(String.format("%s Send:0x%s", DateTimeUtil.getNowTime(), bytesToHexString(d)));
        } else {
            receiveQueue.add(String.format("%s Send:%s", DateTimeUtil.getNowTime(), new String(d)));
        }

    }

    public void closeSocket() {

        if (tcpSocketServer != null) {
            tcpSocketServer.closeAllSocket();
        }
        if (tcpSocketClient != null) {
            tcpSocketClient.closeSocket();
        }

        if (acceptSocketChannel != null) {
            serverController.stopClient(acceptSocketChannel);
        }
    }


    public  void stopClient(SocketChannel channel){

    }

}
