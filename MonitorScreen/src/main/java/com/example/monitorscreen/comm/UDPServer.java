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

            byte[] buffer = new byte[4 * 1024 * 1024];
            byte[] imageBuffer = new byte[4 * 1024 * 1024];
            int imageBufferIdx = 0;
            boolean isReading = false;
            String imageTimeStamp = "";
            int numBlocks = 0;
            int numBlocksReadAlready = 0;

            while (isListening) {
                // Tạo một DatagramPacket để nhận dữ liệu
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Lấy dữ liệu từ packet
                String message = new String(packet.getData(), 0, packet.getLength());
//                System.out.println("Received message: " + packet.getLength());

                if (message.startsWith("==IMAGE START==")) { // client => _sendData2Server("==IMAGE START==|$timeStamp|$totalBlock".toByteArray())
                    Arrays.fill(imageBuffer, (byte) 0); // clear image buf
                    imageBufferIdx = 0;
                    String[] spl = message.split("\\|");
                    isReading = true;
                    imageTimeStamp = spl[1];
                    numBlocks = Integer.parseInt(spl[2]);
                    numBlocksReadAlready = 0;
                    System.out.println("START IMAGE imageTimeStamp = " + imageTimeStamp + " numBlocks = " + numBlocks);
                    continue;
                }

                if (message.startsWith("==IMAGE END==")) { // client => _sendData2Server("==IMAGE END==|$timeStamp".toByteArray())
                    // xử lý hiện ảnh
                    System.out.println("IMAGE size = " + imageBuffer.length);
                    System.out.println("imageBufferIdx = " + imageBufferIdx);

                    String[] spl = message.split("\\|");
                    System.out.println("==IMAGE END== imageTimeStamp = " + spl[1]);
                    System.out.println("==IMAGE END== numBlocksReadAlready = " + numBlocksReadAlready);
                    System.out.println("==IMAGE END== numBlocks = " + numBlocks);

                    if(imageTimeStamp.equals(spl[1]) && numBlocks == numBlocksReadAlready) {
                        System.out.println("Hiển thị hình");
                        byte[] ret = new byte[imageBufferIdx];
                        System.arraycopy(imageBuffer, 0, ret, 0, imageBufferIdx);
                        listener.onReceivedImage(ret);
                    }
                    isReading = false;
                    continue;
                }

                // đọc ảnh client >> _sendData2Server("$timeStamp|$idx|$expectSize|${String(expectBuff)}".toByteArray())
                String[] spl = message.split("\\|");
                if(isReading
                        && imageTimeStamp.equals(spl[0])) {
                    int idx = Integer.parseInt(spl[1]);
                    int expectSize = Integer.parseInt(spl[2]);
//                    System.out.println("message.lastIndexOf(spl[3]) " + message.lastIndexOf(spl[3]));
                    System.arraycopy(packet.getData(), message.lastIndexOf(spl[3]), imageBuffer, idx, expectSize);
                    imageBufferIdx += expectSize;
                    numBlocksReadAlready++;
                }



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
