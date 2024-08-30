package com.example.monitorscreen.comm;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UDPServer {

    public interface UDPServerListener {
        void onOpened(int port);

        void onReceivedImage(byte[] imageData);
    }

    public static int PORT = 9876;
    private static boolean isListening = false;
    private static DatagramSocket socket = null;

    public static void stop() {
        isListening = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void listen(UDPServerListener listener) {
        isListening = true;
        _listen(listener);
    }

    public static void _listen(UDPServerListener listener) {
        socket = null;
        try {
            // Tạo một DatagramSocket để lắng nghe trên cổng cụ thể
            socket = new DatagramSocket(PORT);
            System.out.println("UDP server is listening on port " + PORT);

            byte[] buffer = new byte[200 * 1024];
            byte[] imageBuffer = new byte[200 * 1024];
            int imageBufferIdx = 0;

            while (isListening) {
                // Tạo một DatagramPacket để nhận dữ liệu
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Lấy dữ liệu từ packet
                String message = new String(packet.getData(), 0, packet.getLength());
//                System.out.println("Received message: " + packet.getLength());

                if (message.startsWith("==IMAGE START==")) {
                    Arrays.fill(imageBuffer, (byte) 0); // clear image buf
                    imageBufferIdx = 0;
                    System.out.println("START IMAGE");
                    continue;
                }

                if (message.startsWith("==IMAGE END==")) {
                    // xử lý hiện ảnh
                    System.out.println("IMAGE size = " + imageBuffer.length);
                    System.out.println("imageBufferIdx = " + imageBufferIdx);

                    byte[] ret = new byte[imageBufferIdx];
                    System.arraycopy(imageBuffer, 0, ret, 0, imageBufferIdx);
                    listener.onReceivedImage(ret);
                    continue;
                }

                // đọc ảnh

                System.arraycopy(packet.getData(), 0, imageBuffer, imageBufferIdx, packet.getLength());
                imageBufferIdx += packet.getLength();


                // Gửi lại dữ liệu (echo) cho client
                /*InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                DatagramPacket responsePacket = new DatagramPacket(packet.getData(), packet.getLength(), clientAddress, clientPort);
                socket.send(responsePacket);
                System.out.println("Sent response to client.");*/
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (isListening)
                _listen(listener);
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
