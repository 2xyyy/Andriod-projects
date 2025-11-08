package com.example.exp1;

import static android.graphics.Color.rgb;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DrawingBoardView drawingBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawingBoard = findViewById(R.id.DrawingBoard);

        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_clear).setOnClickListener(this);
        findViewById(R.id.button_changehistory).setOnClickListener(this);

        findViewById(R.id.music_play).setOnClickListener(this);
        findViewById(R.id.music_pause).setOnClickListener(this);

        Spinner spinner = findViewById(R.id.color_spinner);

        // 获取颜色选项
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String color = parent.getItemAtPosition(position).toString();

                // 这里可以让你的 DrawingBoardView 改变画笔颜色
                switch (color) {
                    case "red":
                        drawingBoard.setPaintColor(rgb(255, 0, 0));
                        break;
                    case "green":
                        drawingBoard.setPaintColor(rgb(0, 255, 0));
                        break;
                    case "blue":
                        drawingBoard.setPaintColor(rgb(0, 0, 255));
                        break;
                    case "black":
                        drawingBoard.setPaintColor(rgb(0, 0, 0));
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 啥也不选的时候可以不管
            }
        });

        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_save) {
            saveWithDialog();
        }
        if (v.getId() == R.id.button_clear) {
            drawingBoard.clear(); // 清空画布
            Toast.makeText(this, "画布已清空~", Toast.LENGTH_SHORT).show();
        }
        if (v.getId() == R.id.button_changehistory) {
            openHistoryActivity();
        }
        if (v.getId() == R.id.music_play) {
            Intent intent = new Intent(this, MyMediaPlay.class);
            intent.setAction("PLAY");
            startService(intent);
        }
        if (v.getId() == R.id.music_pause) {
            Intent intent = new Intent(this, MyMediaPlay.class);
            intent.setAction("PAUSE");
            startService(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    /** 弹出对话框输入图片名，保存图片到相册并记录到内部存储 */
    public void saveWithDialog() {
        Bitmap bitmap = drawingBoard.getBitmap();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        if (drawingBoard.isEmpty()) {
            Toast.makeText(this, "画板为空，无法保存图片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. 创建 EditText 实例
        android.widget.EditText etName = new android.widget.EditText(this);
        etName.setHint("请输入图片名");

        // 2. 弹出输入框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入图片名")
                .setView(etName)
                .setPositiveButton("保存", (dialog, which) -> {
                    String imageName = etName.getText().toString().trim();
                    if (imageName.isEmpty()) {
                        imageName = "image";
                    }
                    savePicture(imageName);
                    saveToInternalStorage(imageName + "," + timeStamp+"\n");
//                    saveToInternalStorage("imagename:" + imageName + "\ntimestamp:" + timeStamp);
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();

    }

    /** 保存图片到相册 */
    private void savePicture(String imageName) {
        Bitmap bitmap = drawingBoard.getBitmap();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName + ".png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri saveUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
        );

        if (saveUri == null) {
            updateUI(false, "无法创建文件");
            return;
        }

        new Thread(() -> {
            try (OutputStream out = getContentResolver().openOutputStream(saveUri)){
                bitmap.compress(Bitmap.CompressFormat.PNG,100,out);
                updateUI(true, "图片保存成功");
            } catch (IOException e) {
                e.printStackTrace();
                updateUI(false, "图片保存失败");
            }
        }).start();
    }

    /** 更新UI */
    private void updateUI(boolean isSuccess, String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            // 可选：根据 isSuccess 更新界面
        });
    }

    /** 打开历史记录界面 */
    private void openHistoryActivity() {
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra("user","test");
        startActivity(intent);
    }

    /** 从内部存储读取 */
    public static List<Map<String, String>> readFromInternalStorage(Context context) {
        List<Map<String, String>> data = new ArrayList<>();
        try (FileInputStream fis = context.openFileInput("record.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    Map<String, String> item = new HashMap<>();
                    item.put("name", parts[0]);
                    item.put("time", parts[1]);
                    data.add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    /** 保存到内部存储 */
    private void saveToInternalStorage(String content) {
        // 实现保存逻辑
        if (content.isEmpty()) {
            Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        try (FileOutputStream fos = openFileOutput("record.txt", Context.MODE_PRIVATE | Context.MODE_APPEND)) {
            fos.write((content).getBytes());
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }
}
