package com.example.monitorscreen;

import com.example.monitorscreen.comm.TCPServer;
import com.example.monitorscreen.comm.UDPServer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;


public class HelloController {

    @FXML
    public Button btnStart;
    @FXML
    public Button btnStop;
    @FXML
    public TextField port;
    @FXML
    public RadioButton rBtnUdp;
    @FXML
    public RadioButton rBtnTcp;
    @FXML
    private ImageView mainScreen;


    private final ToggleGroup group = new ToggleGroup();

    @FXML
    private void initialize() {
        System.out.println("RUN");
        mainScreen.setPreserveRatio(true);
        mainScreen.setSmooth(true);

        btnStart.setDisable(false);
        btnStop.setDisable(true);

        rBtnUdp.setToggleGroup(group);
        rBtnTcp.setToggleGroup(group);
    }

    private boolean isUDP() {
        System.out.println("is UDP" + (group.getSelectedToggle() == rBtnUdp));
        return group.getSelectedToggle() == rBtnUdp;
    }

    @FXML
    protected void onBtnStopClick() {
        System.out.println("STOP----->");
        UDPServer.stop();
        TCPServer.stop();
        btnStart.setDisable(false);
        btnStop.setDisable(true);
        port.setDisable(false);
        group.getToggles().forEach(toggle -> ((RadioButton) toggle).setDisable(false));
        try {
            mainScreen.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/main_image_welcome.png"))));
        } catch (Exception ex) {
            System.out.println("PORT invalid >> use default " + UDPServer.PORT);
            System.out.println("PORT invalid >> use default " + TCPServer.PORT);
        }
    }

    @FXML
    protected void onBtnListenClick() {
        System.out.println("START----->");
        btnStart.setDisable(true);
        btnStop.setDisable(false);

        try {
            mainScreen.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/main_image.png"))));
            UDPServer.PORT = Integer.parseInt(port.getCharacters().toString());
            TCPServer.PORT = Integer.parseInt(port.getCharacters().toString());
        } catch (Exception ex) {
            System.out.println("PORT invalid >> use default " + UDPServer.PORT);
            System.out.println("PORT invalid >> use default " + TCPServer.PORT);
        }

        if (isUDP())
            port.setText(UDPServer.PORT + "");
        else
            port.setText(TCPServer.PORT + "");
        port.setDisable(true);
        group.getToggles().forEach(toggle -> ((RadioButton) toggle).setDisable(true));


        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                if (isUDP()) {
                    UDPServer.listen(new UDPServer.UDPServerListener() {
                        @Override
                        public void onOpened(int port) {
                            System.out.println("OPEN AT PORT: " + port);
                        }

                        @Override
                        public void onReceivedImage(byte[] imageData) {
                            System.out.println("CALLBACK ====> " + imageData.length);
                            showImage(imageData);
                        }
                    });
                } else {
                    TCPServer.listen(new TCPServer.TCPServerListener() {
                        @Override
                        public void onOpened(int port) {
                            System.out.println("OPEN AT PORT: " + port);
                        }

                        @Override
                        public void onReceivedImage(byte[] imageData) {
                            System.out.println("CALLBACK ====> " + imageData.length);
                            showImage(imageData);
                        }
                    });
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);// Đặt luồng nền là daemon để nó tự động tắt khi ứng dụng kết thúc
        thread.start();
    }

    private void showImage(byte[] imageData) {
        Platform.runLater(() -> {
            System.out.println("CHANGE IMAGE");
            try {
                Image image = byteArrayToImage(imageData); //new Image(Objects.requireNonNull(getClass().getResourceAsStream("/trinh.png")));
                mainScreen.setImage(image);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private Image byteArrayToImage(byte[] imageData) {
        InputStream inputStream = new ByteArrayInputStream(imageData);
        return new Image(inputStream);
    }
}