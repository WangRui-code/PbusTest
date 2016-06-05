package com.assassin.pbustest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
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

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReminderService extends Service {
    private String line;
    private String toStation;
    private String endStation;
    protected final String TYPE_UTF8_CHARSET = "charset=UTF-8";
    private String loc;
    private  String busInfo;
    Intent mIntent = null;
    PendingIntent mPendIntent = null;
    NotificationManager mNotifyManager = null;
    Notification mNotify = null;
    private static String ERROR_FoundNoneLineNum = "001";
    private static String NET_CONNECTION_FAILED = "net_failed";
    public ReminderService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null){
        line=intent.getStringExtra("line");
        toStation=intent.getStringExtra("toStation");;
        endStation=intent.getStringExtra("endStation");
        busInfo=new String();
        loc=new String();
            //提醒进程
            queryLine(line, endStation);
        }else {
            System.out.print("intent===null-");
        }
        return super.onStartCommand(intent, Service.START_REDELIVER_INTENT, startId);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
    public class RemindThread implements Runnable{
        public void run() {
            String[] sta = new String[0];
            //queryLine(line,endStation);
            if (busInfo.length()>1){
                sta=busInfo.split("\\ ");
                for(int i=0;i<sta.length;i++){
                System.out.println("sta["+i+"]=" + sta[i]);}
            }else {
                System.out.print("途径站台查询有错");
            }

            int distance;
            int pos=0;//记录station在sta中的位置

            int now=0;//记录currentStation在sta中的当前位置
            //查询服务器，解析得到当前位置
            queryStation(line,endStation,toStation);

            if (loc=="暂无车辆到达"){

                now= -1000;
            }



            //找到station的位置pos
            for (int i=0;i<sta.length;i++){
                if (toStation==sta[i]){
                    pos=i;
                }
                if (loc==sta[i]){
                    now=i;
                }
            }


            distance=pos-now;
            System.out.print("---------haiyou   "+distance+"   zhan---------------");

            while (distance>=2){
                try {
                    Thread.sleep(180000);//休眠三分钟
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //查询服务器，刷新currentStation和now
                queryStation(line,endStation,toStation);
                for (int i=0;i<=sta.length;i++){
                    if (loc==sta[i]){
                        now=i;
                    }
                }
                //刷新distance
                distance=pos-now;
                System.out.print("haiyou"+distance+"zhan---------------");

            }
            //调用提醒
            Bitmap btm = BitmapFactory.decodeResource(getResources(),
                    R.drawable.timg);
            Notification noti = new Notification.Builder(ReminderService.this)
                    .setContentTitle("New:")
                    .setContentText("公交车快要到达 "+toStation )
                    .setSmallIcon(R.drawable.timg)
                    .setLargeIcon(btm)
                    .build();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, noti);

            stopSelf();


        }


    }
    public  void queryLine(final String line,final  String direction){

            String url = getString(R.string.requestAddress) + "line?";//传入的是公交车号（开往XX站台），返回的是路线信息，格式为站台/站台。。。
            System.out.println(url);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            //s格式为当前站点-去往的站点
                            System.out.println(s);
                            if (s.equals(ERROR_FoundNoneLineNum)) {
                            } else {
                                busInfo = s;
                                System.out.println("busInfo="+busInfo);
                                RemindThread remindThread=new RemindThread();
                                Thread thread=new Thread(remindThread);
                                thread.start();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    System.out.println("对不起,出错了");
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
                map.put("Line",line);
                map.put("statioName",direction);
//                    map.put("Line", "夜2");
//                    map.put("statioName", "火车站");
                    return map;
                }
            };
            requestQueue.add(stringRequest);
            requestQueue.start();
        }

    public void queryStation(final String line,final String direction,final String toStation) {

            final String[] str = new String[1];
            String url = getString(R.string.requestAddress) + "reminder?";//传入的是公交车号（开往XX站台），返回的是开往该方向的最近一辆公交当前站台
            System.out.println(url);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            final StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            //s格式为当前站点-去往的站点
                            System.out.println(s);
                            if (s.equals(ERROR_FoundNoneLineNum)) {
                            } else {
                                loc = s;
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    System.out.println("对不起,出错了");
                }
            }) {
                @Override
//                protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                    try {
//                        String str = new String(response.data, "UTF-8");
//                        return Response.success(str, HttpHeaderParser.parseCacheHeaders(response));
//                    } catch (UnsupportedEncodingException e) {
//                        return Response.error(new com.android.volley.ParseError());
//                    }
//
//                }
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
                    map.put("endStation", endStation);
                    map.put("toStation",toStation);
                    return map;
                }
            };
            requestQueue.add(stringRequest);
            requestQueue.start();
        }
    }
