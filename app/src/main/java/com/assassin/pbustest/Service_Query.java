package com.assassin.pbustest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by 不期然相遇丶 on 2016/2/29.
 */
public class Service_Query extends Service {
    private BufferedReader reader;
    private BufferedWriter writer;

    private CallBack callBack;

    private boolean isSend;
    private String Origin;
    private String Destination;

    private boolean running;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    public class Binder extends android.os.Binder {
        public void setArgs(boolean isSend, String Origin, String Destination) {
            Service_Query.this.isSend = isSend;
            Service_Query.this.Origin = Origin;
            Service_Query.this.Destination = Destination;
        }

        public Service_Query getService() {
            return Service_Query.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        SendMessage();
        System.out.println("Service_Query:成功启动服务！");

        new Thread() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(getString(R.string.serverIp), 52229);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                    System.out.println("Service_Query:路线查询服务器连接成功");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String line;
                try {
                    if (reader != null) {
                        while ((line = reader.readLine()) != null) {
                            callBack.onResultChanged(line);
                        }
                    } else {
                        System.out.println("Service_Query:reader为null");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    public void send(String message) throws IOException {
        if (writer != null) {
            writer.write(message + "\n");
            writer.flush();
        } else {
            System.out.println("Service_Query:writer为null");
        }
    }

    private void SendMessage() {
        new Thread() {
            @Override
            public void run() {
                running = true;
                isSend = false;

                while (running) {
                    if (isSend) {
                        try {
                            send(Origin + "." + Destination);
                            isSend = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
    }

    public interface CallBack {
        void onResultChanged(String result);
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }
}
