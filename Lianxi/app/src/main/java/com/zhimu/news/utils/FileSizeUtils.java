package com.zhimu.news.utils;

import com.pgyersdk.crash.PgyCrashManager;
import com.zhimu.news.app.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * 计算文件夹和文件的大小，删除文件
 * Created by Administrator on 2016.4.19.
 */
public class FileSizeUtils {

    // 单位b
    public static final int TYPE_B = 1;
    public static final int TYPE_K = 2;
    public static final int TYPE_M = 3;
    public static final int TYPE_G = 4;

    /**
     * 指定文件和输出的单位大小
     *
     * @param filePath 文件路径
     * @param sizeType 单位类型
     * @return 大小
     */
    public double getFileAndFileSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getSizes(file);
            } else {
                blockSize = getSize(file);
            }
        } catch (IOException e) {
            // 抓异常，上传蒲公英SDK
            PgyCrashManager.reportCaughtException(MyApplication.getInstance(), e);
        }

        return formatFileSize(blockSize, sizeType);
    }

    /**
     * 获取指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 大小, 带单位值
     */
    public String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getSizes(file);
            } else {
                blockSize = getSize(file);
            }
        } catch (IOException e) {
            // 抓异常，上传蒲公英SDK
            PgyCrashManager.reportCaughtException(MyApplication.getInstance(), e);
        }

        return formatFileSize(blockSize);
    }

    /**
     * 指定文件大小
     *
     * @param file 文件
     * @return 大小
     */
    private long getSize(File file) throws IOException {
        long size = 0;
        if (file.exists()) {
            FileInputStream fins = new FileInputStream(file);
            size = fins.available();
        } else {
            file.createNewFile();
        }

        return size;
    }

    /**
     * 指定文件夹大小
     *
     * @param file 文件名称
     * @return 文件夹大小
     */
    private long getSizes(File file) throws IOException {
        long size = 0;
        File listFile[] = file.listFiles();
        // 遍历所有文件
        for (File list : listFile) {
            if (list.isDirectory()) {
                size = size + getSizes(list);
            } else {
                size = size + getSize(list);
            }
        }
        return size;
    }

    /**
     * 转换文件大小
     *
     * @param blockSize 文件大小
     * @return 返回单位长度
     */
    private String formatFileSize(long blockSize) {

        DecimalFormat df = new DecimalFormat("#.00");
        String fileSize = "";

        if (blockSize == 0) {
            fileSize = 0.00 + "B";
        }

        if (blockSize < 1024 && blockSize > 0) {
            fileSize = df.format((double) blockSize) + "B";
        }

        if (blockSize < 1048576 && blockSize >= 1024) {
            fileSize = df.format((double) (blockSize / 1024)) + "K";
        }

        if (blockSize < 1073741824 && blockSize >= 1048576) {
            fileSize = df.format((double) blockSize / 1048576) + "M";
        }

        if (blockSize >= 1073741824){
            fileSize = df.format((double) blockSize / 1073741824) + "G";
        }

        return fileSize;
    }

    /**
     * 转换文件大小，指定转换的类型
     *
     * @param blockSize 文件的大小
     * @param sizeType  单位类型
     * @return 返回指定单位大小
     */
    private double formatFileSize(long blockSize, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSize = 00.00;

        if (blockSize == 0) {
            return fileSize;
        }

        switch (sizeType) {
            case TYPE_B:
                fileSize = Double.valueOf(df.format((double) blockSize));
                break;
            case TYPE_K:
                fileSize = Double.valueOf(df.format((double) blockSize / 1024));
                break;
            case TYPE_M:
                fileSize = Double.valueOf(df.format((double) blockSize / 1048576));
                break;
            case TYPE_G:
                fileSize = Double.valueOf(df.format((double) blockSize / 1073741824));
                break;
            default:
                break;
        }
        return fileSize;
    }

    /**
     * 删除指定文件夹下的所有图片文件
     *
     * @param path 文件路径
     * @return 是否删除成功
     */
    public boolean deleteAllImgFile(String path) {

        boolean flag = false;
        File sdfile = new File(path);

        // 指定文件是否存在
        if (!sdfile.exists()) {
            return flag;
        }

        // 该路径表示的是否是一个目录
        if (!sdfile.isDirectory()) {
            return flag;
        }

        // 只要是图片，全部删除
        File[] files = sdfile.listFiles();
        for (File file : files) {
            if (file.toString().endsWith(".jpg") || file.toString().endsWith("png")) {
                flag = file.delete();
            }
        }

        return flag;
    }


}
