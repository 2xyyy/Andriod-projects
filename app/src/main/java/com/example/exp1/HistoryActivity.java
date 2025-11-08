package com.example.exp1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        findViewById(R.id.button_main).setOnClickListener(this);

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

    /** 打开主界面 */
    private void openMainActivity() {
        finish();
    }
}