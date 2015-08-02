package com.arctro.slipbook;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;


public class ImageList extends Activity {

    public static Context context;
    public static FloatingActionMenu fam;
    public static FloatingActionButton openCamera;
    public static FloatingActionButton stats;
    public static FloatingActionButton logout;
    public static FloatingActionButton login;
    public static File outputFile;
    public static String imagefilepath="";
    public static String previewfilepath="";

    public static LocationManager lm;
    public static double lat;
    public static double lng;
    int CAMERA_INTENT = 1337;
    String uniqueID;

    public static Animation InBottom;
    public static Animation OutBottom;

    public static float dp;

    SearchView searchView;
    SearchManager searchManager;

    public static final String USER_AGENT = "Mozilla/5.0";

    LinearLayout ll;

    public static String path=Environment.getExternalStorageDirectory() + File.separator + "slipbook_images" + File.separator + "images.data";

    public static List<String[]> data;
    public static Map<String, Bitmap> image_cache = new HashMap<String, Bitmap>();
    public static SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);
        settings = getApplicationContext().getSharedPreferences("slipbook_prefs", 0);

        Intent intent = new Intent(getApplicationContext(), Statistics.class);
        //startActivityForResult(intent,2016);

        context=this.getApplicationContext();

        InBottom = AnimationUtils.loadAnimation(this, R.anim.inbottom);
        OutBottom = AnimationUtils.loadAnimation(this, R.anim.outbottom);

        openCamera=(FloatingActionButton)findViewById(R.id.menu_item_photo);
        openCamera.setButtonSize(FloatingActionButton.SIZE_MINI);
        openCamera.setColorNormal(getResources().getColor(R.color.miniButton));
        openCamera.setColorPressed(getResources().getColor(R.color.miniButton));

        stats=(FloatingActionButton)findViewById(R.id.menu_item_stats);
        stats.setButtonSize(FloatingActionButton.SIZE_MINI);
        stats.setColorNormal(getResources().getColor(R.color.miniButton));
        stats.setColorPressed(getResources().getColor(R.color.miniButton));

        logout=(FloatingActionButton)findViewById(R.id.menu_item_logout);
        logout.setButtonSize(FloatingActionButton.SIZE_MINI);
        logout.setColorNormal(getResources().getColor(R.color.miniButton));
        logout.setColorPressed(getResources().getColor(R.color.miniButton));

        login=(FloatingActionButton)findViewById(R.id.menu_item_login);
        login.setButtonSize(FloatingActionButton.SIZE_MINI);
        login.setColorNormal(getResources().getColor(R.color.miniButton));
        login.setColorPressed(getResources().getColor(R.color.miniButton));

        fam=(FloatingActionMenu)findViewById(R.id.menu);
        fam.setMenuButtonColorNormal(getResources().getColor(R.color.menuButton));
        fam.setMenuButtonColorPressed(getResources().getColor(R.color.menuButton));

        dp=getResources().getDisplayMetrics().density;

        if(settings.getLong("session_expire",-1)!=-1||settings.getLong("session_expire",-1)>System.currentTimeMillis()/1000){
            logout.setVisibility(View.VISIBLE);
            login.setVisibility(View.GONE);
        }
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SharedPreferences.Editor editor = settings.edit();

                Thread t = new Thread(){
                    public void run(){
                        final SharedPreferences settings = getApplicationContext().getSharedPreferences("slipbook_prefs", 0);
                        final SharedPreferences.Editor editor = settings.edit();

                        String url = "http://arctro.com/api/index.php?request=LOGOUT";

                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(url);

                        // add header
                        post.setHeader("User-Agent", ImageList.USER_AGENT);

                        MultipartEntityBuilder httpEntity = MultipartEntityBuilder.create();
                        Log.d("session",settings.getString("session_id",""));
                        httpEntity.addPart("session_id", new StringBody(settings.getString("session_id", ""), ContentType.TEXT_PLAIN));

                        post.setEntity(httpEntity.build());

                        try {
                            HttpResponse response = client.execute(post);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                editor.clear();
                                editor.commit();
                                logout.setVisibility(View.GONE);
                                login.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                };
                t.start();
            }
        });

        data=readSerialized(path);
        setupGallery(data);

        //Buttons
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
        stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Statistics.class);
                startActivityForResult(intent, 2015);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivityForResult(intent,2015);
            }
        });
    }

    public void openCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        uniqueID = UUID.randomUUID().toString();
        File outputDir=new File(Environment.getExternalStorageDirectory() + File.separator + "slipbook_images");
        outputFile = new File(Environment.getExternalStorageDirectory() + File.separator + "slipbook_images" + File.separator +  uniqueID + ".jpg");
        Log.d("File Directory", Environment.getExternalStorageDirectory() + File.separator + "slipbook_images" + File.separator + uniqueID + ".jpg");
        outputDir.mkdirs();
        try {
            outputFile.createNewFile();
        }catch(Exception e){
            Log.e("Error Creating File","File Error: " + e.getMessage());
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));
        imagefilepath=Environment.getExternalStorageDirectory() + File.separator + "slipbook_images" + File.separator + uniqueID + ".jpg";
        previewfilepath=Environment.getExternalStorageDirectory() + File.separator + "slipbook_images" + File.separator + "preview" + File.separator + uniqueID + ".jpg";
        startActivityForResult(intent, CAMERA_INTENT);
    }

    public void setupGallery(List<String[]> data){
        ll = (LinearLayout)findViewById(R.id.listLayout);
        //List<File> files = getListFiles(new File(Environment.getExternalStorageDirectory() + File.separator + "slipbook_images"));

        File f = new File(path);
        if(f!=null) {
            Collections.reverse(data);

            for (int i = 0; i < data.size(); i++) {
                final int in = i;
                final LayoutInflater factory = getLayoutInflater();
                final View cellFragment = factory.inflate(R.layout.fragment_cell, null);
                RelativeLayout fl = (RelativeLayout)cellFragment.findViewById(R.id.cell_frame);

                RelativeLayout.LayoutParams flp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,(int)(75*dp));
                flp.setMargins((int)(10*dp),(int)(10*dp),(int)(10*dp),0);
                cellFragment.setElevation(20);
                cellFragment.setBackground(getDrawable(R.drawable.rounded_corner));

                ImageView iv = (ImageView) cellFragment.findViewById(R.id.cell_background_image);
                TextView ct = (TextView) cellFragment.findViewById(R.id.cell_text);
                ImageButton uploaded = (ImageButton) cellFragment.findViewById(R.id.imageButton);
                if(!data.get(i)[6].equals("1")){
                    uploaded.setVisibility(View.GONE);
                }
                if(!data.get(i)[2].equals("")){
                   ct.setText(data.get(i)[2]);
                }
                final ImageView ivFinal = iv;
                final int iFinal = i;
                final List<String[]> dataFinal = data;
                if(getImage(dataFinal.get(iFinal)[0])==null) {
                    Thread t = new Thread() {
                        public void run() {
                            final Bitmap b = getImage(dataFinal.get(iFinal)[0]);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    ivFinal.setImageBitmap(Bitmap.createScaledBitmap(b, 75, 75, false));
                                }
                            });
                        }
                    };
                    t.start();
                }else{
                    final Bitmap b = getImage(dataFinal.get(iFinal)[0]);
                    ivFinal.setImageBitmap(Bitmap.createScaledBitmap(b, 75, 75, false));
                }


                //((ViewGroup)fl.getParent()).removeView(fl);
                cellFragment.setBackgroundColor(getResources().getColor(R.color.offwhite));
                ll.addView(cellFragment);

                final String image_path=data.get(i)[0];

                cellFragment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), ViewImage.class);
                        intent.putExtra("image", image_path);
                        intent.putExtra("i",(dataFinal.size()-1)-in+"");
                        startActivityForResult(intent,2015);
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_list, menu);

        searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setQueryHint("");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                List<String[]> queryData=new ArrayList<String[]>();
                ll.removeAllViews();
                String[] query = s.split(" ");
                for(int i=0;i<data.size();i++){
                    boolean relevant=true;
                    String[] nameArray=data.get(i)[2].split(" ");
                    String[] categoryArray=data.get(i)[3].split(" ");
                    String payment=data.get(i)[4];

                    for(int x=0;x<query.length;x++){
                        boolean currentRelevant=false;
                        for(int y=0;y<nameArray.length;y++) {
                            if (nameArray[y].toLowerCase().contains(query[x].toLowerCase())) {
                                currentRelevant=true;
                            }
                        }
                        for(int y=0;y<categoryArray.length;y++) {
                            if (categoryArray[y].toLowerCase().contains(query[x].toLowerCase())) {
                                currentRelevant = true;
                            }
                        }
                        if(payment.toLowerCase().contains(query[x].toLowerCase())){
                            currentRelevant = true;
                        }

                        if(!currentRelevant){
                            relevant = false;
                            x=query.length;
                        }
                    }
                    if(relevant==true){
                        queryData.add(data.get(i));
                    }
                }
                if(s==""){
                    queryData=data;
                }
                Collections.reverse(queryData);
                setupGallery(queryData);
                return true;
            }
        });
        final FloatingActionMenu menuFinal = fam;
        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

            @Override
            public void onViewDetachedFromWindow(View arg0) {
                ll.removeAllViews();
                setupGallery(readSerialized(path));
                getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.scheme3)));
                getWindow().setStatusBarColor(getResources().getColor(R.color.scheme4));
                menuFinal.setVisibility(View.VISIBLE);
                menuFinal.startAnimation(InBottom);
            }

            @Override
            public void onViewAttachedToWindow(View arg0) {
                getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.searchPrimary)));
                getWindow().setStatusBarColor(getResources().getColor(R.color.searchPrimaryDark));
                menuFinal.startAnimation(OutBottom);
                menuFinal.setVisibility(View.GONE);
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CAMERA_INTENT){
            saveData(path,
                    //File Path, Preview File Path, Name, Category, Total, timestamp, Synced
                    new String[]{imagefilepath,previewfilepath,"","","",""+System.currentTimeMillis(),"0"});
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }else if(requestCode==2015){
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    public List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(file.getName().endsWith(".csv")){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    public static void saveData(String path, String[] data){
        List<String[]> dataArray = readSerialized(path);
        File file = new File(path);
        PrintWriter writer;
        try {
            file.createNewFile();
            writer = new PrintWriter(file);
            writer.print("");
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        ObjectOutputStream oos = null;
        FileOutputStream fout = null;
        try{
            fout = new FileOutputStream(path, true);
            oos = new ObjectOutputStream(fout);
            dataArray.add(data);
            oos.writeObject(dataArray);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(oos != null){
                try{
                    oos.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void updateData(String path, String[] data, int position){
        List<String[]> dataArray = readSerialized(path);
        File file = new File(path);
        PrintWriter writer;
        try {
            file.createNewFile();
            writer = new PrintWriter(file);
            writer.print("");
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        ObjectOutputStream oos = null;
        FileOutputStream fout = null;
        try{
            fout = new FileOutputStream(path, true);
            oos = new ObjectOutputStream(fout);
            dataArray.set(position,data);
            oos.writeObject(dataArray);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(oos != null){
                try{
                    oos.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void deleteData(String path, int position){
        List<String[]> dataArray = readSerialized(path);
        File file = new File(path);
        PrintWriter writer;
        try {
            file.createNewFile();
            writer = new PrintWriter(file);
            writer.print("");
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        ObjectOutputStream oos = null;
        FileOutputStream fout = null;
        try{
            fout = new FileOutputStream(path, true);
            oos = new ObjectOutputStream(fout);
            dataArray.remove(position);
            oos.writeObject(dataArray);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(oos != null){
                try{
                    oos.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<String[]> readSerialized(String path){
        FileInputStream fis;
        ObjectInputStream objectinputstream = null;
        List<String[]> returnArray = new ArrayList<String[]>();
        try {
            fis = new FileInputStream(path);
            objectinputstream = new ObjectInputStream(fis);
            returnArray = (ArrayList<String[]>) objectinputstream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(objectinputstream != null){
                try{
                    objectinputstream.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        return returnArray;
    }

    public static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public static Bitmap getImage(String path){
        if(image_cache.get(path)!=null){
            return image_cache.get(path);
        }else{
            final File image = new File(path);
            if(!image.exists()){
                image_cache.put(path,Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888));
                return Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), options);
            if (options.outWidth != -1 && options.outHeight != -1) {
                image_cache.put(path,bitmap);
            }
            else {
                image_cache.put(path,Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888));
                return Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
            }
            return bitmap;
        }
    }
}
