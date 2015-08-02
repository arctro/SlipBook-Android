package com.arctro.slipbook;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.util.List;

public class ViewImage extends Activity {

    ImageView editImage;
    FloatingActionButton delete;
    FloatingActionButton edit;
    FloatingActionMenu menu;
    RelativeLayout baseLayout;

    List<String[]> data;

    static Thread t;
    int counter=0;
    boolean run=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        t = new Thread(){
            public void run(){
                while(run) {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    counter++;
                    if(counter>=3000) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if(menu.getVisibility()==View.VISIBLE&&edit.getVisibility()!=View.VISIBLE) {
                                    menu.startAnimation(ImageList.OutBottom);
                                    menu.setVisibility(View.GONE);
                                }
                            }
                        });
                        counter=0;
                    }
                }
            }
        };
        t.start();

        final int i = Integer.parseInt(getIntent().getStringExtra("i"));

        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
        getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        getActionBar().setDisplayHomeAsUpEnabled(true);

        data=ImageList.readSerialized(ImageList.path);

        editImage = (ImageView)findViewById(R.id.edit_image);

        delete = (FloatingActionButton)findViewById(R.id.menu_item_delete);
        delete.setButtonSize(FloatingActionButton.SIZE_MINI);
        delete.setColorNormal(getResources().getColor(R.color.miniButton));
        delete.setColorPressed(getResources().getColor(R.color.miniButton));

        edit = (FloatingActionButton)findViewById(R.id.menu_item_edit);
        edit.setButtonSize(FloatingActionButton.SIZE_MINI);
        edit.setColorNormal(getResources().getColor(R.color.miniButton));
        edit.setColorPressed(getResources().getColor(R.color.miniButton));

        menu = (FloatingActionMenu)findViewById(R.id.menu);
        menu.setMenuButtonColorNormal(getResources().getColor(R.color.menuButton));
        menu.setMenuButtonColorPressed(getResources().getColor(R.color.menuButton));

        baseLayout = (RelativeLayout)findViewById(R.id.base_view);

        setTitle(data.get(i)[2]+"");

        final String image = getIntent().getStringExtra("image");
        editImage.setImageBitmap(ImageList.getImage(image));

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageList.deleteData(ImageList.path,i);
                File f = new File(data.get(i)[0]);
                f.delete();
                finish();
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditImage.class);
                intent.putExtra("image", getIntent().getStringExtra("image"));
                intent.putExtra("i",getIntent().getStringExtra("i"));
                startActivityForResult(intent,2015);
            }
        });
        baseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(menu.getVisibility()==View.GONE) {
                    menu.setVisibility(View.VISIBLE);
                    menu.startAnimation(ImageList.InBottom);
                }
                counter=0;
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter=0;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_image, menu);
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
        }else if(id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
