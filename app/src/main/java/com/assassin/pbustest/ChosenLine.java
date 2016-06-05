package com.assassin.pbustest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ChosenLine extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chosen_line);
        textView = (TextView) findViewById(R.id.textView);

        String result = getIntent().getStringExtra("result");
        String LineNum = getIntent().getStringExtra("linenum");
        textView.setText("您选择了：" + LineNum +"路公交\n"+"来自服务器的信息：\n"+result);

    }

}
