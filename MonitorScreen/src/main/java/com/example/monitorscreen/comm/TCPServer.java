package com.example.monitorscreen.comm;

import com.example.monitorscreen.utils.ByteArrayUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class TCPServer {

    public interface TCPServerListener {
        void onOpened(int port);

        void onReceivedImage(byte[] imageData);
    }

    public static int PORT = 9876;
    private static boolean isListening = false;
    private static ServerSocket serverSocket;
    private static Socket lastestSocket;

    public static void stop() {
        isListening = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                lastestSocket.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void listen(TCPServerListener listener) {
        isListening = true;
        _listen(listener);
    }

    public static void _listen(TCPServerListener listener) {
        serverSocket = null;
        try {
            // Tạo một DatagramSocket để lắng nghe trên cổng cụ thể
            serverSocket = new ServerSocket(PORT);
            System.out.println("TCP server is listening on port " + PORT);

            byte[] buffer = new byte[4 * 1024 * 1024];
            int currentIdx = 0;
            int sizeBlock = 100 * 1024;

            lastestSocket = serverSocket.accept();
            System.out.println("New client connected");

            // Tạo luồng để đọc dữ liệu từ client

            while (isListening) { // data client ==IMAGE START== DATA BITMAP ==IMAGE END==
                BufferedInputStream input = new BufferedInputStream(lastestSocket.getInputStream());

                int ret = input.read(buffer, currentIdx, sizeBlock);
                currentIdx += ret;

                if (currentIdx + sizeBlock > buffer.length) { // hủy do kích thước hình lớn hơn buff
                    currentIdx = 0;
                    Arrays.fill(buffer, (byte) 0);
                    continue;
                }

                int checkStart = ByteArrayUtils.indexOfSubArray(buffer, "==IMAGE START==".getBytes());
                if (checkStart == -1) {
                    continue;
                }

                int checkEnd = ByteArrayUtils.indexOfSubArray(buffer, "==IMAGE END==".getBytes());
                if (checkEnd == -1) {
                    continue;
                }

                if (checkEnd < checkStart) { // end trước start ???
                    currentIdx = 0;
                    Arrays.fill(buffer, (byte) 0);
                    continue;
                }

                checkStart = (checkStart + "==IMAGE START==".length());
                int imageSize = checkEnd - checkStart;
                byte[] imageBuff = new byte[imageSize];
                System.out.println("=====got image===== imageSize = " + imageSize);
                System.arraycopy(buffer, checkStart, imageBuff, 0, imageSize);

                listener.onReceivedImage(imageBuff);

                currentIdx = 0;
                Arrays.fill(buffer, (byte) 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                    lastestSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (isListening)
                _listen(listener);
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                    lastestSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
