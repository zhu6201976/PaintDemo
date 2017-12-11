package com.example.administrator.test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * 画画板小案例
 * 2017年12月11日16:00:59
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageView iv;
    private Canvas mCanvas;
    private Paint mPaint;
    private float mStrokeWidth = 5;
    private Bitmap mBitmap_copy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv = (ImageView) findViewById(R.id.iv);

        // 加载原图
        Bitmap bitmap_src = BitmapFactory.decodeResource(this.getResources(), R.drawable.bg_360_500);

        // 创建原图的副本
        mBitmap_copy = Bitmap.createBitmap(bitmap_src.getWidth(), bitmap_src.getHeight(),
                bitmap_src.getConfig());
        mCanvas = new Canvas(mBitmap_copy);
        mPaint = new Paint();
        mCanvas.drawBitmap(bitmap_src, new Matrix(), mPaint);

        iv.setImageBitmap(mBitmap_copy);

        // 设置iv的触摸事件，触摸后画线
        iv.setOnTouchListener(new View.OnTouchListener() {

            private int downY;
            private int downX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = (int) event.getX();
                        downY = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = (int) event.getX();
                        int moveY = (int) event.getY();

                        mCanvas.drawLine(downX, downY, moveX, moveY, mPaint);

                        iv.setImageBitmap(mBitmap_copy);

                        downX = moveX;
                        downY = moveY;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }

    // 变红
    public void click1(View view) {
        mPaint.setColor(Color.RED);
    }

    // 变黑
    public void click2(View view) {
        mPaint.setColor(Color.BLACK);
    }

    // 变粗
    public void click3(View view) {
        mStrokeWidth += 5;
        if (mStrokeWidth < 30) {
            mPaint.setStrokeWidth(mStrokeWidth);
        }
    }

    // 变细
    public void click4(View view) {
        mStrokeWidth -= 5;
        if (mStrokeWidth > 0) {
            mPaint.setStrokeWidth(mStrokeWidth);
        }
    }

    // 保存大作到sdcard
    public void click5(View view) {
        this.checkPermission();
    }

    // 保存
    private void save() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/dazuo.jpg");
            boolean compress = mBitmap_copy.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            if (compress) {
                Toast.makeText(this, "save success", Toast.LENGTH_SHORT).show();
                // 发送一条无序广播通知图库应用加载图片
                // 判断系统内版本
                Intent intent = new Intent();
                int sdkInt = Build.VERSION.SDK_INT;
                if (sdkInt <= 19) {//4.4之前
                    intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
                } else {// 4.4之后
                    intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                }
                intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
                this.sendBroadcast(intent);
            } else {
                Toast.makeText(this, "save fail", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            this.save();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "you allow the permission", Toast.LENGTH_SHORT).show();
                    this.save();
                } else {
                    Toast.makeText(this, "you denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
