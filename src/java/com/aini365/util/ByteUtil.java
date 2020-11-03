package com.aini365.util;

import java.util.ArrayList;

public class ByteUtil {
    public static byte[] arrayListToBytes(ArrayList<Byte> data){

        byte[] result = new byte[data.size()];

        for (int i = 0; i < data.size(); i++) {
            result[i]=data.get(i).byteValue();
        }

        return result;
    }
}
