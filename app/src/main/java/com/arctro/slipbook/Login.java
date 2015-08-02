package com.arctro.slipbook;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.arctro.slipbook.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Login extends Activity {

    EditText email;
    EditText password;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);
        login=(Button)findViewById(R.id.login_button);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email_string=email.getText().toString();
                final String password_string=password.getText().toString();

                Thread t = new Thread(){
                    public void run(){
                        SharedPreferences settings = getApplicationContext().getSharedPreferences("slipbook_prefs", 0);
                        SharedPreferences.Editor editor = settings.edit();


                        String url = "http://arctro.com/api/index.php?request=LOGIN";

                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(url);

                        // add header
                        post.setHeader("User-Agent", ImageList.USER_AGENT);

                        MultipartEntityBuilder httpEntity = MultipartEntityBuilder.create();
                        httpEntity.addPart("email", new StringBody(email_string, ContentType.TEXT_PLAIN));
                        httpEntity.addPart("password", new StringBody(password_string, ContentType.TEXT_PLAIN));

                        post.setEntity(httpEntity.build());

                        try {
                            HttpResponse response = client.execute(post);

                            BufferedReader rd = new BufferedReader(
                                    new InputStreamReader(response.getEntity().getContent()));

                            String line = rd.readLine();
                            JSONObject returnData = new JSONObject(line);

                            Log.d("Return", line);

                            if(returnData.getString("error").equals("")) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                                String inputString = "00:01:30.500";

                                Date date = sdf.parse(returnData.getString("session_expire"));

                                editor.putString("session_id", returnData.getString("session_id"));
                                Log.d("Expire",date.getTime()+"");
                                editor.putLong("session_expire", date.getTime());

                                editor.apply();

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        finish();
                                    }
                                });
                            }else{
                                final JSONObject returnDataFinal=returnData;
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        try {
                                            Toast toast = Toast.makeText(ImageList.context, ""+returnDataFinal.getString("error"), Toast.LENGTH_SHORT);
                                            toast.show();
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        });
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
