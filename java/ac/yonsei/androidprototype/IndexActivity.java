package ac.yonsei.androidprototype;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class IndexActivity extends AppCompatActivity {
    public static final String SERVER_IP = "3.17.135.91";
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;
    public static final int ERROR = 2;
    String sId, sPw; // for sign in
    String pId, pPw, pPwchk; // for sign up

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        final Button signinButton = (Button)findViewById(R.id.signin);
        final Button signupButton = (Button)findViewById(R.id.signup);

        signinButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
                signinButton.startAnimation(animation);
                signin();
            }
        });

        signupButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
                signupButton.startAnimation(animation);
                signup();
            }
        });
    }

    private void signin() {
        EditText eId = (EditText)findViewById(R.id.userid);
        sId = eId.getText().toString();

        EditText ePw = (EditText)findViewById(R.id.password);
        sPw = ePw.getText().toString();

        Thread signinThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("DEBUG", sId + "/" + sPw);

                LoginDB ldb = new LoginDB();
                try {
                    final int resultCode = ldb.execute().get();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!isFinishing()) {
                                if(resultCode == SUCCESS) {
                                    Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_LONG).show();
                                    login();
                                }
                                else if(resultCode == FAILURE) {
                                    Toast.makeText(getApplicationContext(), "Login Failure", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Check Your Internet Connection", Toast.LENGTH_LONG).show();
                                    login(); // Trick
                                }
                            }
                        }
                    });

                } catch(Exception e) {
                    e.printStackTrace();
                    Log.e("DEBUG", "Exception Occured");
                }
            }
        });

        signinThread.setDaemon(true);
        signinThread.start();
    }

    private void signup() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(IndexActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.modal_signup, null);
        final Button signupSubmit = (Button) mView.findViewById(R.id.submitButton);
        final EditText eId = (EditText) mView.findViewById(R.id.inputId);
        final EditText ePw = (EditText) mView.findViewById(R.id.inputPw);
        final EditText ePwChk = (EditText) mView.findViewById(R.id.inputPwCheck);

        signupSubmit.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
                signupSubmit.startAnimation(animation);
                Thread signupThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pId = eId.getText().toString();
                        pPw = ePw.getText().toString();
                        pPwchk = ePwChk.getText().toString();

                        if(pId != null && !"".equals(pId) && pPw != null && !"".equals(pPw) && pPw.equals(pPwchk)) {
                            SignupDB sdb = new SignupDB();
                            try {
                                final int resultCode = sdb.execute().get();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(!isFinishing()) {
                                            if(resultCode == SUCCESS) {
                                                Toast.makeText(getApplicationContext(), "Signup Success", Toast.LENGTH_LONG).show();
                                            }
                                            else if(resultCode == FAILURE){
                                                Toast.makeText(getApplicationContext(), "Signup Failure", Toast.LENGTH_LONG).show();
                                            }
                                            else Toast.makeText(getApplicationContext(), "Check Your Internet Connection.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Invalid Information", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                signupThread.setDaemon(true);
                signupThread.start();
            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    private void login() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.putExtra("nickname", sId);

        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    public class LoginDB extends AsyncTask<Void, Void, Integer> {
        private final String LOGIN_SUCCESS = "Login Success";
        private final String LOGIN_FAILURE = "Login Failure";
        private final String MYSQL_ERROR = "MySQL Failed";

        @Override
        protected Integer doInBackground(Void... unused) {
            // Input Parameters
            String param = "Nickname=" + sId + "&Password=" + sPw + "";
            try {
                // Server Connection
                URL url = new URL("http://" + SERVER_IP + "/login.php");
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

                if (LOGIN_SUCCESS.equals(data)) {
                    return SUCCESS;
                }
                else if(LOGIN_FAILURE.equals(data)) {
                    return FAILURE;
                }
                else return ERROR;

            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }

            return ERROR;
        }
    }

    public class SignupDB extends AsyncTask<Void, Void, Integer> {
        private final String SIGNUP_SUCCESS = "Signup Success";
        private final String SIGNUP_FAILURE = "Signup Failure";
        private final String MYSQL_ERROR = "MySQL Failure";

        @Override
        protected Integer doInBackground(Void... unused) {
            // Input Parameters
            String param = "Nickname=" + pId + "&Password=" + pPw + "";
            try {
                // Server Connection
                URL url = new URL("http://" + SERVER_IP + "/signup.php");
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

                if (SIGNUP_SUCCESS.equals(data)) {
                    return SUCCESS;
                }
                else if (SIGNUP_FAILURE.equals(data)) {
                    return FAILURE;
                }
                else return ERROR;

            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }

            return ERROR;
        }
    }
}
