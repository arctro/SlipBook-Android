package com.arctro.slipbook;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.arctro.slipbook.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class Statistics extends Activity {
    LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setupGraphs();

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.statistics, menu);
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

    public void setupGraphs(){
        /*chart = (LineChart) findViewById(R.id.chart);
        chart.setDescription("A Chart");

        ArrayList<Entry> lineDataSetEntry = new ArrayList<Entry>();
        lineDataSetEntry.add(new Entry(100f, 0));
        lineDataSetEntry.add(new Entry(100f, 1));

        LineDataSet lineDataSet = new LineDataSet(lineDataSetEntry,"Primary");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("January"); xVals.add("Febuary"); xVals.add("March"); xVals.add("April"); xVals.add("May"); xVals.add("June"); xVals.add("July"); xVals.add("August"); xVals.add("September"); xVals.add("October"); xVals.add("November"); xVals.add("December");
        LineData data = new LineData(xVals,lineDataSet);

        chart.setData(data);*/
    }
}
