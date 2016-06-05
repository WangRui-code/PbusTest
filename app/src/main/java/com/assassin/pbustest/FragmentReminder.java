package com.assassin.pbustest;


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.impl.io.ContentLengthInputStream;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.jsoup.parser.ParseError;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FragmentReminder extends Fragment {
    private String direc;
    private  String loc;
    protected final String TYPE_UTF8_CHARSET = "charset=UTF-8";
    private String endStation=null;
    private static String ERROR_FoundNoneLineNum = "001";
    private static String NET_CONNECTION_FAILED = "net_failed";
    private EditText etLineNum;
    private Spinner etDirection;
    private EditText etStation;
    private Button btnQuery;
    private ArrayList<String> data_list;
    private ArrayAdapter<String> arr_adapter;
    private  TextView tx;
    private Boolean flag=false;
private Button cancelBt;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reminder, container, false);
        initView(view);
        NotificationManager nNotificaitonMan = (NotificationManager) FragmentReminder.this.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nNotificaitonMan.cancel(10);


        etDirection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                endStation=parent.getAdapter().getItem(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        etLineNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                } else {
                    Toast.makeText(getActivity(), "失去焦点", Toast.LENGTH_SHORT).show();
                    String lineNum=etLineNum.getText().toString();
                    if(lineNum!=null){
                    directionQuery(lineNum);
                    }
                }
            }
        });
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etLineNum.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "请填写公交线路！", Toast.LENGTH_SHORT).show();
                } else if (etStation.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "请填写站台名！", Toast.LENGTH_SHORT).show();
                } else if (endStation == null) {
                    Toast.makeText(getActivity(), "请填写方向！", Toast.LENGTH_SHORT).show();
                } else {
                    String dline = etLineNum.getText().toString();//车号
                    String s;
                    final String toStation = etStation.getText().toString();//站台
                    final String st[];
                    st = endStation.split("往");
                    if (st.length == 2) {
                        final String etdirection = st[1];//方向
                        queryStation(dline, etdirection, toStation);
                    } else {
                    }
                }
            }
        });
        return view;
    }
    public void initView(View view) {
        direc=new String("无此路");
        loc=new String ("暂无公交车到达");
        etLineNum = (EditText) view.findViewById(R.id.etLineNum);
        cancelBt= (Button) view.findViewById(R.id.reminderCancelBt);
        tx= (TextView) view.findViewById(R.id.tx);
        etStation = (EditText) view.findViewById(R.id.etStation);
        etStation.setText("齐门");
        //tvResult = (TextView) view.findViewById(R.id.tvResult);
        etDirection= (Spinner) view.findViewById(R.id.etDirection);
        btnQuery = (Button) view.findViewById(R.id.btnQuery);
        data_list=new ArrayList<>();
        arr_adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, data_list);
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etDirection.setAdapter(arr_adapter);

    }
    public void  changAdapter() throws InterruptedException {
        //String lineNum=etLineNum.getText().toString();
        //arr_adapter.add(" ");
       // if(lineNum!=null){
         //  directionQuery(lineNum);

            String[] sArray = new String[0];
            System.out.print(direc + "-------------");
            //Thread.sleep(1000);
            sArray = direc.split("\\/");
            for (int i =0;i<=sArray.length;i++){
                arr_adapter.add("开往"+sArray[i]);
            }
        }

    public void directionQuery( final String line){
//        synchronized (direc){
//            final String[][] sArray = {new String[1]};
            String url = getString(R.string.requestAddress) + "to?";
            RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            if (s.replaceAll("\n", "").equals(ERROR_FoundNoneLineNum)) {
                                Toast.makeText(getActivity(), "未找到此公交信息", Toast.LENGTH_SHORT).show();
                            } else if (s.replaceAll("\n", "").equals(NET_CONNECTION_FAILED)) {
                                Toast.makeText(getActivity(), "请检查网络连接", Toast.LENGTH_SHORT).show();
                            } else {
                                flag=true;
                                direc = s;
                                String[] sArray = new String[0];
                                sArray = direc.split("\\/");
                                for (int i =0;i<sArray.length;i++){
                                    arr_adapter.add("开往"+sArray[i]);
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    System.out.println(volleyError);
                }
            }) {
                protected Response<String> parseNetworkResponse(
                        NetworkResponse response) {
                    try {
                        String type = response.headers.get(HTTP.CONTENT_TYPE);
                        if (type == null) {
                            type = TYPE_UTF8_CHARSET;
                            response.headers.put(HTTP.CONTENT_TYPE, type);
                        } else if (!type.contains("UTF-8")) {
                            type += ";" + TYPE_UTF8_CHARSET;
                            response.headers.put(HTTP.CONTENT_TYPE, type);
                        }
                    } catch (Exception e) {
                    }
                    return super.parseNetworkResponse(response);
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Line", line);
                    return map;
                }
            };
            //stringRequest.setShouldCache(false);
            requestQueue.add(stringRequest);
           // requestQueue.start();
    //    }
    }
//    class  DirectionThread implements Runnable{
//     String lineNum;
//       DirectionThread(String line){
//         this.lineNum=line;
//    }
//
//    public void run() {
//        synchronized (list){
//            final String[][] sArray = {new String[1]};
//            String url = getString(R.string.requestAddress) + "to?";
//            RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
//            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
//                    new Response.Listener<String>() {
//                        @Override
//                        public void onResponse(String s) {
//                            if (s.replaceAll("\n", "").equals(ERROR_FoundNoneLineNum)) {
//                                Toast.makeText(getActivity(), "未找到此公交信息", Toast.LENGTH_SHORT).show();
//                                list.add("未找到此公交信息");
//                                list.add("未找到此公交信息");
//                            } else if (s.replaceAll("\n", "").equals(NET_CONNECTION_FAILED)) {
//                                Toast.makeText(getActivity(), "请检查网络连接", Toast.LENGTH_SHORT).show();
//                                list.add("未找到此公交信息");
//                                list.add("未找到此公交信息");
//                            } else {
//                                direc = s;
//                                sArray[0] = direc.split("\\/");
//
//                                String l1 = "开往" + sArray[0][0];
//                                String l2 = "开往" + sArray[0][1];
//                                list.add(l1);
//                                list.add(l2);
//                                //Toast.makeText(getActivity(), "查询完毕", Toast.LENGTH_SHORT).show();
//                                //Toast.makeText(getActivity(),"l1="+l1,Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError volleyError) {
//                    System.out.println(volleyError);
//                }
//            }) {
//                protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                    try {
//                        String str = new String(response.data, "UTF-8");
//                        return Response.success(str, HttpHeaderParser.parseCacheHeaders(response));
//                    } catch (UnsupportedEncodingException e) {
//                        return Response.error(new com.android.volley.ParseError());
//                    }
//
//                }
//
//                @Override
//                protected Map<String, String> getParams() throws AuthFailureError {
//                    HashMap<String, String> map = new HashMap<>();
//                    map.put("Line", lineNum);
//                    return map;
//                }
//            };
//            requestQueue.add(stringRequest);
//
//        }
//
//    }
//}
    public void queryStation(final String line,final String direction,final String toStation) {

            Toast.makeText(getActivity(), "正在查询" + line + "车" + direction + "方向的" + toStation + "站", Toast.LENGTH_SHORT).show();
            final String[] str = new String[1];
            String url = getString(R.string.requestAddress) + "reminder?";//传入的是公交车号（开往XX站台），返回的是开往该方向的最近一辆公交当前站台
            System.out.println(url);
            RequestQueue requestQueue = Volley.newRequestQueue(FragmentReminder.this.getContext());
            final StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            //s格式为当前站点-去往的站点
                            Toast.makeText(getActivity(), "queryStation查询结果为" + s, Toast.LENGTH_LONG).show();
                            System.out.println(s);
                            if (s.equals(ERROR_FoundNoneLineNum)) {
                            } else {
                                loc = s;
                                if ("暂无公交车到达" !=loc) {
                                    final Intent intent;
                                    intent = new Intent();
                                    intent.putExtra("line", etLineNum.getText().toString());
                                    intent.putExtra("endStation", "火车站");
                                    intent.putExtra("toStation", "齐门");
                                    intent.setClass(FragmentReminder.this.getContext(), ReminderService.class);
                                    FragmentReminder.this.getContext().startService(intent);
                                    cancelBt.setVisibility(View.VISIBLE);
                                    tx.setText("正在查询" + endStation + "方向的" + etLineNum.getText().toString() + "路公交车。提醒站台为" + etStation.getText().toString() + "当前站台为" + loc);
                                    //启动service，在提醒之后service自动销毁
                                    cancelBt.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            FragmentReminder.this.getContext().stopService(intent);
                                            tx.setText("此时无提醒正在进行");
                                        }
                                    });
                                } else {
                                }
                                // tvResult.setText(s);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    System.out.println("对不起,出错了");
                }
            }) {
//                @Override
//                protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                    try {
//                        String str = new String(response.data, "UTF-8");
//                        return Response.success(str, HttpHeaderParser.parseCacheHeaders(response));
//                    } catch (UnsupportedEncodingException e) {
//                        return Response.error(new com.android.volley.ParseError());
//                    }
//
//                } protected final String TYPE_UTF8_CHARSET = "charset=UTF-8";

                // 重写parseNetworkResponse方法改变返回头参数解决乱码问题
                // 主要是看服务器编码，如果服务器编码不是UTF-8的话那么就需要自己转换，反之则不需要
                @Override
                protected Response<String> parseNetworkResponse(
                        NetworkResponse response) {
                    try {
                        String type = response.headers.get(HTTP.CONTENT_TYPE);
                        if (type == null) {
                            type = TYPE_UTF8_CHARSET;
                            response.headers.put(HTTP.CONTENT_TYPE, type);
                        } else if (!type.contains("UTF-8")) {
                            type += ";" + TYPE_UTF8_CHARSET;
                            response.headers.put(HTTP.CONTENT_TYPE, type);
                        }
                    } catch (Exception e) {
                    }
                    return super.parseNetworkResponse(response);
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Line",line );
                    map.put("endStation","火车站" );
                    map.put("toStation", "齐门");
                    return map;
                }
            };
            requestQueue.add(stringRequest);
            requestQueue.start();
        }
    }
