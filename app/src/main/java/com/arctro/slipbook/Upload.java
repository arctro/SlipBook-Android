package com.arctro.slipbook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by BenM on 22/03/15.
 */
public class Upload extends AsyncTask<String,String,String> {
    public String[] data;
    File dest;

    protected void onPreExecute(){

    }

    protected String doInBackground(String... data) {
        try {
            this.data =data;
            String url = "http://i.imageourworld.com/api/mobile_upload.php";

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);

            // add header
            post.setHeader("User-Agent", ImageList.USER_AGENT);

            File no = new File(data[0]);
            dest = new File(data[0]+"-copy.jpg");
            dest.createNewFile();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(no.getAbsolutePath(), options);

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(data[0]+"-copy.jpg");
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //ImageList.copyFileUsingStream(notCopied,dest);

            OutputStream os;
            os = new FileOutputStream(dest);
            //MyActivity.bp.compress(Bitmap.CompressFormat.JPEG, 75, os);
            os.flush();
            os.close();

            MultipartEntityBuilder httpEntity = MultipartEntityBuilder.create();
            httpEntity.addPart("fileToUpload", new FileBody(no));
            httpEntity.addPart("title", new StringBody(data[1]+"", ContentType.TEXT_PLAIN));
            httpEntity.addPart("desc", new StringBody(data[2]+"", ContentType.TEXT_PLAIN));
            httpEntity.addPart("tags", new StringBody(data[3]+"", ContentType.TEXT_PLAIN));
            httpEntity.addPart("lat", new StringBody(data[4] + "", ContentType.TEXT_PLAIN));
            httpEntity.addPart("lon", new StringBody(data[5]+"", ContentType.TEXT_PLAIN));
            httpEntity.addPart("direction", new StringBody(data[6]+"", ContentType.TEXT_PLAIN));
            httpEntity.addPart("user", new StringBody("", ContentType.TEXT_PLAIN));
            httpEntity.addPart("explicit", new StringBody(data[7], ContentType.TEXT_PLAIN));


            post.setEntity(httpEntity.build());

            HttpResponse response = client.execute(post);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Log.i("POST",result.toString()+"");
        }catch(Exception e){
            Log.e(e.getClass().getName()+"", e.getMessage()+"");
        }
        return "";
    }

    protected void onProgressUpdate(String... strings) {

    }

    @Override
    protected void onPostExecute(String result) {
        Log.i("I think its done","");
        for(int i=0;i<data.length;i++){
            Log.i("Data " + i,data[i]+"");
        }
        dest.delete();
    }
}
