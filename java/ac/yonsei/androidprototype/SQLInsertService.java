package ac.yonsei.androidprototype;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SQLInsertService extends Service {
    String pId, pNum, pLoc;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Intent data = intent;
        Thread insertThread = new Thread(new Runnable() {
            @Override
            public void run() {
                pId = data.getExtras().get("id").toString();
                pNum = data.getExtras().get("num").toString();
                pLoc = data.getExtras().get("loc").toString();
                Log.e("DEBUG", pId + "/" + pNum + "/" + pLoc);

                if(pId != null && !"".equals(pId)) {
                    InsertDB idb = new InsertDB();
                    try {
                        final int resultCode = idb.execute().get();
                        if(resultCode == IndexActivity.SUCCESS) {
                            Log.e("DEBUG", "Insert Success");
                        }
                        else if(resultCode == IndexActivity.FAILURE){
                            Log.e("DEBUG", "Insert Failure");
                        }
                        else Log.e("DEBUG", "Check Internet Connection");
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Log.e("DEBUG", "Invalid Information");
                }
            }
        });
        insertThread.setDaemon(true);
        insertThread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class InsertDB extends AsyncTask<Void, Void, Integer> {
        private final String INSERT_SUCCESS = "Insert Success";
        private final String INSERT_FAILURE = "Insert Failure";
        private final String MYSQL_ERROR = "MySQL Failure";

        @Override
        protected Integer doInBackground(Void... unused) {
            // Input Parameters
            String param = "Nickname=" + pId + "&Number=" + pNum + "&Place=" + pLoc + "";
            try {
                // Server Connection
                URL url = new URL("http://" + IndexActivity.SERVER_IP + "/insert.php");
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                // Android -> Server Parameter Request
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                // Server -> Android Parameter Response
                InputStream is = null;
                BufferedReader in = null;
                String data = "";

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();

                while((line = in.readLine()) != null) {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.e("RECV DATA", data);

                if (INSERT_SUCCESS.equals(data)) {
                    return IndexActivity.SUCCESS;
                }
                else if (INSERT_FAILURE.equals(data)) {
                    return IndexActivity.FAILURE;
                }
                else return IndexActivity.ERROR;

            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }

            return IndexActivity.ERROR;
        }
    }
}
