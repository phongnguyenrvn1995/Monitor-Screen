package com.example.monitorscreen.utils;

public class ByteArrayUtils {
    public static int indexOfSubArray(byte[] array, byte[] subArray) {
        // Nếu subArray lớn hơn array, trả về -1
        if (subArray.length > array.length) {
            return -1;
        }

        // Duyệt qua array và so sánh với subArray
        for (int i = 0; i <= array.length - subArray.length; i++) {
            boolean found = true;
            for (int j = 0; j < subArray.length; j++) {
                if (array[i + j] != subArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;  // Trả về chỉ số đầu tiên nếu tìm thấy
            }
        }

        return -1;  // Trả về -1 nếu không tìm thấy
    }
}
