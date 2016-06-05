package com.assassin.pbustest;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.List;
import java.util.Map;

import JPA.MyDateBase;
import adapter.SearchAdapter;
import model.Bean;
import widge.SearchView;

public class FragmentCurrentBus extends Fragment implements SearchView.SearchViewListener {
    private static String ERROR_FoundNoneLineNum = "001";
    private static String NET_CONNECTION_FAILED = "net_failed";

    private MyDateBase sldb;
    private SQLiteDatabase dbRead;
    private SQLiteDatabase dbWrite;
    protected final String TYPE_UTF8_CHARSET = "charset=UTF-8";

    private ListView lvResults;

    private SearchView searchView;
    private ArrayAdapter<String> hintAdapter;
    private ArrayAdapter<String> autoCompleteAdapter;
    private SearchAdapter resultAdapter;
    private List<Bean> dbData;
    private List<String> hintData;
    private List<String> autoCompleteData;
    private List<Bean> resultData;
    private static int hintSize;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_currentbus, container, false);
        initData();
        initView(view);

        return view;
    }

    private void initView(View view) {
        lvResults = (ListView) view.findViewById(R.id.main_lv_search_results);
        searchView = (SearchView) view.findViewById(R.id.main_search_layout);
        searchView.setSearchViewListener(this);
        searchView.setTipsHintAdapter(hintAdapter);
        searchView.setAutoCompleteAdapter(autoCompleteAdapter);

        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SearchLine(resultData.get(position).getTitle());

            }
        });

    }

    private void initData() {
        sldb = new MyDateBase(getContext());//创建MySQLiteOpenHelper辅助类对象
        dbRead = sldb.getReadableDatabase();//获取数据库对象
        dbWrite = sldb.getWritableDatabase();//获取数据库对象

        Cursor cursor = dbRead.query("commonline", null, null, null, null, null, null);
        hintSize = cursor.getCount();

        getDbData();
        getHintData();
        getAutoCompleteData(null);
        getResultData(null);
    }

    private void getDbData() {
        Cursor cursor = dbRead.query("commonline", null, null, null, null, null, null);
        int size = cursor.getCount();
        dbData = new ArrayList<>(size);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndex("linenum"));//获得列的名称
                dbData.add(new Bean(R.drawable.icon, title, "1", "2"));
            } while (cursor.moveToNext());

        }
    }

    private void getHintData() {
        Cursor cursor = dbRead.query("commonline", null, null, null, null, null, null);
        hintData = new ArrayList<>(hintSize);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String linenum = cursor.getString(cursor.getColumnIndex("linenum"));
                hintData.add("常用线路:" + linenum);
            } while (cursor.moveToNext());

        }
        hintAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, hintData);
    }

    private void getAutoCompleteData(String text) {
        if (autoCompleteData == null) {
            autoCompleteData = new ArrayList<>(hintSize);
        } else {
            autoCompleteData.clear();
            for (int i = 0, count = 0; i < dbData.size()
                    && count < hintSize; i++) {
                if (dbData.get(i).getTitle().contains(text.trim())) {
                    autoCompleteData.add(dbData.get(i).getTitle());
                    count++;
                }
            }
        }
        if (autoCompleteAdapter == null) {
            autoCompleteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, autoCompleteData);
        } else {
            autoCompleteAdapter.notifyDataSetChanged();
        }
    }

    private void getResultData(String text) {
        if (resultData == null) {
            resultData = new ArrayList<>();
        } else {
            resultData.clear();
            for (int i = 0; i < dbData.size(); i++) {
                if (dbData.get(i).getTitle().contains(text.trim())) {
                    resultData.add(dbData.get(i));
                }
            }
        }
        if (resultAdapter == null) {
            resultAdapter = new SearchAdapter(getContext(), resultData, R.layout.item_bean_list);
        } else {
            resultAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefreshAutoComplete(String text) {
        getAutoCompleteData(text);
    }

    @Override
    public void onSearch(String text) {
        searchView.hideListView();
        getResultData(text);
        lvResults.setVisibility(View.VISIBLE);
        if (lvResults.getAdapter() == null) {
            lvResults.setAdapter(resultAdapter);
        } else {
            resultAdapter.notifyDataSetChanged();
        }
        Toast.makeText(getContext(), "搜索完成", Toast.LENGTH_SHORT).show();
    }
     public class MyStringRequest extends StringRequest{
         public MyStringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
             super(method, url, listener, errorListener);
         }
         protected Response<String> parseNetworkResponse(NetworkResponse response) {
             String str = null;
             try {
                 str = new String(response.data, "utf-8");
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             }
             return Response.success(str,
                     HttpHeaderParser.parseCacheHeaders(response));
         }
     }

    public void SearchLine(final String line) {
        String url = getString(R.string.requestAddress) + "currentbus?";
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        MyStringRequest stringRequest = new MyStringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            System.out.println(new String(s.getBytes("ISO-8859-1"),"utf-8"));
                        } catch (UnsupportedEncodingException e) {
                            System.out.println("转换失败");
                        }
                        if(s.replaceAll("\n","").equals(ERROR_FoundNoneLineNum)){
                            Toast.makeText(getActivity(),"未找到此公交信息",Toast.LENGTH_SHORT).show();
                        }else if(s.replaceAll("\n","").equals(NET_CONNECTION_FAILED)) {
                            Toast.makeText(getActivity(),"请检查网络连接",Toast.LENGTH_SHORT).show();
                        }else{
                                Intent intent = new Intent(getActivity(), ChosenLine.class);
                                intent.putExtra("linenum", line);
                                intent.putExtra("result", s);
                                startActivity(intent);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println(volleyError);
            }
        }){
//            protected Response<String> parseNetworkResponse(
//                    NetworkResponse response) {
//                try {
//                    String type = response.headers.get(HTTP.CONTENT_TYPE);
//                    if (type == null) {
//                        type = TYPE_UTF8_CHARSET;
//                        response.headers.put(HTTP.CONTENT_TYPE, type);
//                    } else if (!type.contains("UTF-8")) {
//                        type += ";" + TYPE_UTF8_CHARSET;
//                        response.headers.put(HTTP.CONTENT_TYPE, type);
//                    }
//                } catch (Exception e) {
//                }
//                return super.parseNetworkResponse(response);
//            }
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Line", line);
                return map;
            }
        };

        requestQueue.add(stringRequest);
    }

//    public void sendMsg(String queryString) throws IOException {
//        HttpURLConnection connection = MyConnection.getInstance().getHttpConnection();
//        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "utf-8"));
//        bw.write(queryString);
//        bw.flush();
//    }

    //        new AsyncTask<String, String, String>() {
//            @Override
//            protected String doInBackground(String... params) {
//                try {
//                    HttpURLConnection connection = MyConnection.getInstance().getHttpConnection();
//                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
//
//                    String line;
//                    String result = null;
//                    while ((line = br.readLine()) != null) {
//                        System.out.println(line);
//                        result += line;
//                        if (line.equals("!00")) {
//                            publishProgress(result);
//                        }
//                    }
//                    br.close();
//
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                return null;
//            }
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//            }
//
//            @Override
//            protected void onPostExecute(String s) {
//                super.onPostExecute(s);
//            }
//
//            @Override
//            protected void onProgressUpdate(String... values) {
//                Intent intent = new Intent(getActivity(), ChosenLine.class);
//                intent.putExtra("result", values[0]);
//                startActivity(intent);
//            }
//
//            @Override
//            protected void onCancelled(String s) {
//                super.onCancelled(s);
//            }
//
//            @Override
//            protected void onCancelled() {
//                super.onCancelled();
//            }
//        }.execute();
}
