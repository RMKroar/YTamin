package ac.yonsei.androidprototype;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    String id, receivedData, userRank;
    TextView rankingValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        rankingValue = (TextView) findViewById(R.id.rankingValueText);

        Intent receivedIntent = getIntent();
        id = receivedIntent.getExtras().getString("id");

        getData();
    }

    private void getData() {
        Thread selectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                RankingActivity.SelectDB sdb = new RankingActivity.SelectDB();
                try {
                    final int resultCode = sdb.execute().get();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!isFinishing()) {
                                if(resultCode == IndexActivity.SUCCESS) {
                                    showData();
                                }
                                else if(resultCode == IndexActivity.FAILURE) {
                                    showTrickData();
                                }
                                else {
                                    showTrickData();
                                }
                            }
                        }
                    });

                } catch(Exception e) {
                    e.printStackTrace();
                    Log.e("DEBUG", "Exception Occurred");
                }
            }
        });
        selectThread.setDaemon(true);
        selectThread.start();
    }

    private void showData() {
        if(receivedData != null) {
            receivedData = receivedData.replace("Select Success", "");
            String[] records = receivedData.split("#");
            ListView dataList = (ListView) findViewById(R.id.dataList);
            List<String> list = new ArrayList<>();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    R.layout.list_ranking, list);
            dataList.setAdapter(adapter);

            int iterator = 0;
            for(String record : records) {
                iterator++;
                if(record.contains(",")) {
                    String[] recordPieces = record.split(",");
                    list.add(ordinal(iterator) + " → " + recordPieces[0] + " (" + recordPieces[1] + ")");

                    if(id != null && id.equals(recordPieces[0])) {
                        rankingValue.setText(ordinal(iterator));
                    }
                }
            }
        }
    }

    private void showTrickData() {
        // TODO
    }

    public class SelectDB extends AsyncTask<Void, Void, Integer> {
        private final String SELECT_SUCCESS = "Select Success";
        private final String NO_DATA = "0 results";
        private final String MYSQL_ERROR = "MySQL Failed";

        @Override
        protected Integer doInBackground(Void... unused) {
            // Input Parameters
            String param = "";
            try {
                // Server Connection
                URL url = new URL("http://" + IndexActivity.SERVER_IP + "/rank.php");
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

                if (data != null && data.contains(SELECT_SUCCESS)) {
                    receivedData = data;
                    return IndexActivity.SUCCESS;
                }
                else if(data != null && data.contains(NO_DATA)) {
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

    private String ordinal(int num) {
        if((num % 100) / 10 == 1) return ("" + num + "th");
        else if (num % 10 == 1) return ("" + num + "st");
        else if (num % 10 == 2) return ("" + num + "nd");
        else if (num % 10 == 3) return ("" + num + "rd");
        else return ("" + num + "th");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
