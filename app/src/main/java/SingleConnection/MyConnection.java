package SingleConnection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 不期然相遇丶 on 2016/3/14.
 */
public class MyConnection {
    private String url = "";
    private static volatile MyConnection connection = null;
    private HttpURLConnection httpConnection;


    private MyConnection() throws IOException {
        URL url = new URL(this.url);
        httpConnection= (HttpURLConnection) url.openConnection();
        httpConnection.setDoOutput(true);
        httpConnection.setDoInput(true);
        httpConnection.setRequestMethod("POST");
    }

    public HttpURLConnection getHttpConnection() {
        return httpConnection;
    }

    public static MyConnection getInstance() throws IOException {
        if(connection==null){
            synchronized (MyConnection.class){
                if(connection==null){
                    connection = new MyConnection();
                }
            }
        }
        return connection;
    }
}
