package ac.yonsei.androidprototype;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class BluetoothActivity extends AppCompatActivity {

    private BluetoothSPP bt;

    private int workoutCount;

    private String locationText;
    private TextView idView, valueText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        workoutCount = 0;

        idView = (TextView)findViewById(R.id.userIdText);
        valueText = (TextView)findViewById(R.id.receivedText);
        final Intent receivedIntent = getIntent();
        if(receivedIntent != null) idView.setText(receivedIntent.getExtras().get("id").toString());

        bt = new BluetoothSPP(this);

        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                if(message != null) {
                    int receivedValue = -1;
                    try {
                        receivedValue = Integer.parseInt(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if(receivedValue != -1) {
                        if(receivedValue == -2) {
                            finish();
                        }
                        if(workoutCount <= receivedValue) workoutCount = receivedValue;
                        valueText.setText("" + workoutCount);
                    }
                    else valueText.setText("Parsing Error");
                }
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {

            @Override
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext(),
                        "Connected to " + name + "\n" + address,
                        Toast.LENGTH_SHORT).show();

                TextView locationView = (TextView) findViewById(R.id.locationText);
                locationText = "Songdo_Intl_Campus_1";
                locationView.setText(locationText);
                valueText.setText("Waiting...");
                StartTimer();
            }

            @Override
            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext(),
                        "Connection lost",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext(),
                        "Unable to connect",
                        Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) bt.disconnect();
                else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bt.stopService();
        // if(workoutCount != 0 && locationText != null) {
        if (workoutCount != 0) { // test
            Intent serviceIntent = new Intent(this, SQLInsertService.class);
            serviceIntent.putExtra("id", idView.getText());
            serviceIntent.putExtra("num", workoutCount);
            if(locationText != null) serviceIntent.putExtra("loc", locationText);
            else serviceIntent.putExtra("loc", "Songdo_Intl_Campus_1"); // trick
            startService(serviceIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }
        else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            }
        }
    }

    public void setup() {
        // Button btnSend = findViewById(R.id.btnSend);
        // btnSend.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        bt.send("Hello, Arduino!", true);
        //    }
        // });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK) bt.connect(data);
        }
        else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            }
        }
        else {
            Toast.makeText(getApplicationContext(),
                    "Bluetooth is not enabled",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void StartTimer() {
        final TextView timerText = (TextView)findViewById(R.id.timeText);
        Thread timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (true) {
                    final int second = count;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timerText.setText(second / 60 + "m " + second % 60 + "s");
                        }
                    });

                    try {
                        Thread.sleep(1000);
                        count++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
