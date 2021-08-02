package com.dxh.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dxh.myapplication.widget.BigTurntableView;

public class MainActivity extends AppCompatActivity {

    private BigTurntableView bigTurntableView;
    private Button btnAutoSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        bigTurntableView.setUseTouchEvent(false);//设置触摸事件是否有效
        btnAutoSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开启自动选择
                bigTurntableView.startAutoSelect(new BigTurntableView.OnSelectedListener() {
                    @Override
                    public void onSelected(int posion, String[] bisectionContent) {
                        Toast.makeText(MainActivity.this, bisectionContent[posion], Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initView() {
        bigTurntableView = (BigTurntableView) findViewById(R.id.bigTurntableView);
        btnAutoSelect = (Button) findViewById(R.id.btn_autoSelect);
    }
}
