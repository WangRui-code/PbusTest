package com.assassin.pbustest;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

import JPA.MyDateBase;

public class SetLineNumPage extends ListActivity implements View.OnClickListener {
    private MyDateBase sldb;
    private SQLiteDatabase dbRead;
    private SQLiteDatabase dbWrite;
    private SimpleCursorAdapter sAdapter;
    private EditText etLineNum;
    private Button btnAddLineNum;
    private Button btnBackUp;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setlinepage);
        sldb = new MyDateBase(this);
        dbRead = sldb.getReadableDatabase();
        dbWrite = sldb.getWritableDatabase();

        sAdapter = new SimpleCursorAdapter(this, R.layout.location_list_cell, null, new String[]{"linenum"}, new int[]{R.id.tvLineNum}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(sAdapter);
        refreshList();

        etLineNum = (EditText) findViewById(R.id.etAddLineNum);
        btnAddLineNum = (Button) findViewById(R.id.btnAddLineNum);
        btnBackUp = (Button) findViewById(R.id.btnBackUp);

        btnBackUp.setOnClickListener(this);
        btnAddLineNum.setOnClickListener(this);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(SetLineNumPage.this).setTitle("提醒").setMessage("您确定要删除此记录吗？").setNegativeButton("否", null).setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Cursor cursor = sAdapter.getCursor();
                        cursor.moveToPosition(position);
                        int itemId = cursor.getInt(cursor.getColumnIndex("_id"));

                        dbWrite.delete("commonline", "_id=?", new String[]{itemId + ""});

                        refreshList();
                    }
                }).show();


                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBackUp:
                finish();
                break;
            case R.id.btnAddLineNum:
                ContentValues cv = new ContentValues();
                cv.put("linenum", etLineNum.getText().toString());
                dbWrite.insert("commonline", null, cv);
                refreshList();
                etLineNum.setText("");
                break;
        }
    }

    public void refreshList(){
        Cursor cursor = dbRead.query("commonline", null, null, null, null, null, null);
        sAdapter.swapCursor(cursor);
    }
}
