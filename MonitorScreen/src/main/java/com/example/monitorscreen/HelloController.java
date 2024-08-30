package com.example.monitorscreen;

import com.example.monitorscreen.comm.UDPServer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
    private ImageView mainScreen;

    @FXML
    private void initialize() {
        System.out.println("RUN");
        mainScreen.setPreserveRatio(true);
        btnStart.setDisable(false);
        btnStop.setDisable(true);
    }

    @FXML
    protected void onBtnStopClick() {
        System.out.println("STOP----->");
        UDPServer.stop();
        btnStart.setDisable(false);
        btnStop.setDisable(true);
        port.setDisable(false);
        try {
            mainScreen.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/main_image_welcome.png"))));
        } catch (Exception ex){
            System.out.println("PORT invalid >> use default " + UDPServer.PORT);
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
        } catch (Exception ex){
            System.out.println("PORT invalid >> use default " + UDPServer.PORT);
        }
        port.setText(UDPServer.PORT + "");
        port.setDisable(true);


        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
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