package com.zhimu.news.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.zhimu.news.R;

/**
 * 设置字体大小
 * Created by Administrator on 2016.4.9.
 */
public class SetTextSize {

    // 记录类型
    private static int TYPE = 0;

    // 字体大小dialog
    public static void setTextSizeDialog(final Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_txt_size, null, false);
        final TextView txt_size_example = (TextView) view.findViewById(R.id.txt_size_example);

        TextView txt_normal = (TextView) view.findViewById(R.id.txt_normal);
        TextView txt_medium = (TextView) view.findViewById(R.id.txt_medium);
        TextView txt_large = (TextView) view.findViewById(R.id.txt_large);

        // 小
        txt_normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TYPE = 14;
                txt_size_example.setTextSize(TYPE);
            }
        });

        // 中
        txt_medium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TYPE = 18;
                txt_size_example.setTextSize(TYPE);
            }
        });

        // 大
        txt_large.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TYPE = 20;
                txt_size_example.setTextSize(TYPE);

            }
        });

        builder.setNegativeButton(R.string.set_text_size_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(R.string.set_text_size_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (TYPE) {
                    case 14:
                        SharedUtils.setSharedInt(context, SharedUtils.TEXT_SIZE, TYPE);
                        break;
                    case 18:
                        SharedUtils.setSharedInt(context, SharedUtils.TEXT_SIZE, TYPE);
                        break;
                    case 20:
                        SharedUtils.setSharedInt(context, SharedUtils.TEXT_SIZE, TYPE);
                        break;
                    default:
                        break;
                }

            }
        });

        // 修改低版本显示有边框的问题
        AlertDialog alertDialog = builder.create();
        alertDialog.setView(view, 0, 0, 0, 0);
        alertDialog.show();
    }
}
