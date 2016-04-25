package com.zhimu.news.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {
    /**
     * @param is 读入流
     * @return String
     * @throws IOException
     */
    public static String readFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream br = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            br.write(buffer, 0, len);
        }
        is.close();
        String result = br.toString();
        br.close();
        return result;
    }
}
