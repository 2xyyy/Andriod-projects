package com.example.exp1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ServerParse();

        findViewById(R.id.button_main).setOnClickListener(this);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_main) {
            openMainActivity();
        }
    }

    /** 解析数据-内部存储 */
    private void InternalStorageParse() {
        ListView listView = findViewById(R.id.listview);
        List<Map<String, String>> data = MainActivity.readFromInternalStorage(this);

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                data,
                R.layout.activity_listitem,
                new String[]{"name", "time"},
                new int[]{R.id.tvImageName, R.id.tvModifyTime}
        );

        listView.setAdapter(adapter);
    }

    /** 解析数据-服务器 */
    private void ServerParse() {
        ListView listView = findViewById(R.id.listview);
        List<Map<String, String>> data = new ArrayList<>();

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                data,
                R.layout.activity_listitem,
                new String[]{"name", "time"},
                new int[]{R.id.tvImageName, R.id.tvModifyTime}
        );

        listView.setAdapter(adapter);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/Download") // 服务器 URL
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(HistoryActivity.this, "获取历史记录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    String responseText = response.body().string(); // 服务器返回的数据

                    // 解析每行记录
                    String[] lines = responseText.split("\n");
                    List<Map<String,String>> newData = new ArrayList<>();
                    for(String line : lines) {
                        String[] parts = line.split(",");
                        if(parts.length == 2) {
                            Map<String,String> map = new HashMap<>();
                            map.put("name", parts[0]);
                            map.put("time", parts[1]);
                            newData.add(map);
                        }
                    }

                    // 更新 ListView（UI线程）
                    runOnUiThread(() -> {
                        data.clear();
                        data.addAll(newData);
                        adapter.notifyDataSetChanged();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(HistoryActivity.this, "服务器返回失败: " + response.message(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    /** 打开主界面 */
    private void openMainActivity() {
        finish();
    }
}