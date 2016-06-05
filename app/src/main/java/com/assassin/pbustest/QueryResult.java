package com.assassin.pbustest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class QueryResult extends AppCompatActivity {
    private TextView queryrlt;

    private String OriginStr;
    private String DestinationStr;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_result);

        Intent intent = getIntent();
        OriginStr = intent.getStringExtra("etOrigin");
        DestinationStr = intent.getStringExtra("etDestination");
        result = intent.getStringExtra("result");

        queryrlt = (TextView) findViewById(R.id.queryResult);
        queryrlt.setText("起点：" + OriginStr + "\n" + "终点：" + DestinationStr + "\n" + result);
    }
}
