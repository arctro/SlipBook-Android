package com.arctro.slipbook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.github.clans.fab.FloatingActionButton;

import java.io.File;
import java.util.List;

/**
 * Created by BenM on 19/02/15.
 */
public class EditImage extends Activity {
    List<String[]> data;
    EditText name;
    EditText category;
    EditText total;
    ScrollView sv;
    FloatingActionButton saveButton;
    ImageView iv;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent in = getIntent();
        i = Integer.parseInt(getIntent().getStringExtra("i"));
        setContentView(R.layout.activity_edit_image);

        data=ImageList.readSerialized(ImageList.path);

        name = (EditText)findViewById(R.id.edit_name);
        name.setText(data.get(i)[2]);
        category = (EditText)findViewById(R.id.edit_category);
        category.setText(data.get(i)[3]);
        total = (EditText)findViewById(R.id.edit_total);
        total.setText(data.get(i)[4]);
        sv=(ScrollView)findViewById(R.id.scrollView);
        //iv=(ImageView)findViewById(R.id.receipt_preview);
        saveButton=(FloatingActionButton)findViewById(R.id.save_botton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = Integer.parseInt(getIntent().getStringExtra("i"));
                ImageList.updateData(ImageList.path,new String[]{
                        data.get(i)[0],
                        data.get(i)[1],
                        name.getText().toString(),
                        category.getText().toString(),
                        total.getText().toString(),
                        data.get(i)[5],
                        data.get(i)[6]
                },i);

                finish();
            }
        });

        saveButton.setColorNormal(Color.parseColor("#2FD32F"));
        saveButton.setColorPressed(Color.parseColor("#2FD32F"));

        setTitle("Edit " + data.get(i)[2]);
        //iv.setImageBitmap(ImageList.getImage(data.get(i)[0]));

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_image, menu);
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
