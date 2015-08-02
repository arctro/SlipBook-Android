package com.arctro.slipbook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ViewList extends Activity {

    AsyncTask<Void, Void, Void> mTask;
    AsyncTask<Void, Void, Void> mTask1;
    String jsonString;

    int offset=0;
    final int limit = 25;
    int sort=0;

    ImageButton photo_list;
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);

        photo_list=(ImageButton)findViewById(R.id.imageList);
        photo_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(getApplicationContext(), ImageList.class);
                startActivity(in);
            }
        });

        addImages();
    }

    public void addImages(){
        ll=(LinearLayout)findViewById(R.id.listLayout);

        mTask = new AsyncTask<Void, Void, Void> () {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    jsonString = getJsonFromServer("http://imageourworld.com/api/api.php?limit="+limit+"&offset="+offset);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                //Log.d("JSON String",jsonString.substring(1, jsonString.length() - 1));

                JSONArray jsonArray=null;
                List<String> JSON=null;
                try {
                    jsonArray = new JSONArray(jsonString);
                    JSON = new ArrayList<String>();
                    for (int i=0; i<jsonArray.length(); i++) {
                        JSON.add(jsonArray.getString(i));
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

                for(int i=0;i<JSON.size();i++){
                    final LayoutInflater factory = getLayoutInflater();
                    final View cellFragment = factory.inflate(R.layout.fragment_cell, null);
                    FrameLayout fl = (FrameLayout) cellFragment.findViewById(R.id.cell_frame);
                    final ImageView iv = (ImageView) cellFragment.findViewById(R.id.cell_background_image);
                    TextView ct = (TextView) cellFragment.findViewById(R.id.cell_text);
                    ImageButton uploaded = (ImageButton) cellFragment.findViewById(R.id.imageButton);
                    uploaded.setVisibility(View.GONE);

                    JSONObject jsonObject=null;
                    String url="";
                    try{
                        jsonObject = new JSONObject(JSON.get(i));
                        ct.setText(jsonObject.getString("title"));
                        url=jsonObject.getString("url");

                        final URL url1 = new URL(url);
                        final Bitmap[] bitmap=new Bitmap[1];

                        mTask1 = new AsyncTask<Void, Void, Void> () {
                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    bitmap[0] = BitmapFactory.decodeStream(url1.openConnection().getInputStream());
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void result) {
                                super.onPostExecute(result);
                                if (bitmap[0] != null) {
                                    float ratio = (float)(bitmap[0].getWidth())/(float)(bitmap[0].getHeight());
                                    Log.d("Ratio",""+ratio);

                                    iv.setImageBitmap(Bitmap.createScaledBitmap(bitmap[0], (int)(500*ratio), 500, false));
                                }
                            }
                        };
                        mTask1.execute();
                        final JSONObject jsonObjectFinal=jsonObject;
                        cellFragment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent in = new Intent(getApplicationContext(), ViewImage.class);
                                try{
                                    in.putExtra("url",jsonObjectFinal.getString("url"));
                                    in.putExtra("id",jsonObjectFinal.getString("id"));
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                startActivity(in);
                            }
                        });
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    ll.addView(cellFragment);
                }
            }

        };

        mTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static String getJsonFromServer(String url) throws IOException {

        BufferedReader inputStream = null;

        URL jsonUrl = new URL(url);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(
                dc.getInputStream()));

        // read the JSON results into a string
        String jsonResult = inputStream.readLine();
        return jsonResult;
    }

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }
}
