package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StockChart extends AppCompatActivity {

    public static final String EXTRA_SYMBOL = "symbol";
    @BindView(R.id.stock_title) TextView stockTitle;
    @BindView(R.id.chart) LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_stock_chart);
        ButterKnife.bind(this);

        String symbol = getIntent().getStringExtra(EXTRA_SYMBOL);
        stockTitle.setContentDescription(getString(R.string.stock_name, symbol));

        showHistory(symbol);
    }

    private void showHistory(String symbol) {
        String history = getHistoryString(symbol);

        List<String []> lines = readLines (history);

        if (lines != null) {
            ArrayList<Entry> entries = new ArrayList<>(lines.size());

            final ArrayList<Long> xAxisValues = new ArrayList<>();
            int xAxisPosition = 0;

            for (int i = lines.size() - 1; i >= 0; i--) {
                //for (String [] line : lines ) {
                String[] line = lines.get(i);
                xAxisValues.add(Long.valueOf(line[0]));
                xAxisPosition++;

                Entry entry = new Entry(
                        xAxisPosition, //timestamp
                        Float.valueOf(line[1]) //symbol value
                );
                entries.add(entry);
            }

            if (!xAxisValues.isEmpty()) {
                setupChart(symbol, entries, xAxisValues);
            }
            stockTitle.setText(symbol);
        }
    }

    private void setupChart(String symbol, List<Entry> entries, final List<Long> xAxisValues) {
        LineDataSet dataSet = new LineDataSet(entries,symbol); // add entries to dataset
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setCircleRadius(2f);
        dataSet.setDrawFilled(true);

        CustomMarkerView mv = new CustomMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv); // Set the marker to the chart

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(10, 10, 10, 10);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date (xAxisValues.get((int) value));
                return new SimpleDateFormat("yyy-MM-dd", Locale.ENGLISH).format(date);
            }
        });
        chart.animateX(2500, Easing.EasingOption.EaseInCubic);
        //chart.animateXY(2500, 2500);
        //chart.invalidate(); // refresh
    }

    @Nullable
    private List<String[]> readLines(String history) {
        List<String []> lines = new ArrayList<>();
        CSVReader reader = new CSVReader(new StringReader(history));
        try {
            lines = reader.readAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private String getHistoryString(String symbol) {
        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(symbol), null, null, null, null);
        String history = "";
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
                cursor.close();
            }
        }
        return history;
    }
}
