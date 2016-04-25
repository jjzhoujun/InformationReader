package com.zhimu.news.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.pgyersdk.crash.PgyCrashManager;
import com.zhimu.news.R;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

/**
 * 下载图片工具类
 * Created by Administrator on 2016.4.8.
 */
public class LoadImageUtils {

    private Context context;
    private static LoadImageUtils mImageDialog = new LoadImageUtils();

    public static LoadImageUtils getInit() {
        return mImageDialog;
    }

    /**
     *
     * @param ctx 上下文
     * @param imgUrl      图片链接地址
     */
    public void saveImage(Context ctx, final String imgUrl) {

        context = ctx;

        new AlertDialog.Builder(context)
                .setTitle(R.string.load_image_title)
                .setMessage(R.string.load_image_message)
                .setNegativeButton(R.string.load_image_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton(R.string.load_image_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 下载图片
                        uploadImage(imgUrl);
                    }
                })
                .show();
    }

    /**
     * 下载图片
     * @param imgUrl 图片链接
     */
    private void uploadImage(String imgUrl) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, R.string.load_image_no_sd, Toast.LENGTH_SHORT).show();
            return;
        }

        // 采用android-async-http，使用volley也可以实现下载
        AsyncHttpClient client = new AsyncHttpClient();
        String[] imageTypes = new String[]{"image/png", "image/jpeg"};
        client.get(imgUrl, new BinaryHttpResponseHandler(imageTypes) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {

                // 创建文件目录
                File fileDir = new File(Environment.getExternalStorageDirectory(), "ZhiMu/MyImage");
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }

                // 创建图片名称
                String imgName = System.currentTimeMillis() + ".jpg";
                File file = new File(fileDir, imgName);

                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    DataOutputStream dous = new DataOutputStream(fos);
                    dous.write(binaryData);

                    fos.flush();
                    fos.close();
                    dous.close();

                } catch (IOException e) {

                    PgyCrashManager.reportCaughtException(context, e);
                }

                try {

                    // 添加到图库：以便取出图片
                    String imagePath = file.getAbsolutePath();

                    MediaStore.Images.Media.insertImage(context.getContentResolver(),
                            imagePath, imgName, null);

                    // 发送广播通知图库更新:
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.parse("file://" + imagePath)));

                } catch (FileNotFoundException e) {
                    PgyCrashManager.reportCaughtException(context, e);
                }

                // 扫描sd卡下所有的图片，耗时长
//                getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//                        Uri.parse("file://" + Environment.getExternalStorageDirectory()))));

                Toast.makeText(context, R.string.load_image_success, Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                Toast.makeText(context, R.string.load_image_fail, Toast.LENGTH_SHORT).show();
            }

            /**
             * 再弹出一个dialog可以显示进度
             * @param bytesWritten 目标图片字节大小，当前显示
             * @param totalSize     总大小
             */
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
            }
        });
    }
}
