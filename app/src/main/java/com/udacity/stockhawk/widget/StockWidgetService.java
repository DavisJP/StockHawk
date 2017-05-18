package com.udacity.stockhawk.widget;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.StockChart;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Service that binds the data from the ContentProvider to the RemoteViews list
 *
 * @Author Davis Miyashiro
 */

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockWidgetViewFactory(getApplicationContext());
    }

    /**
     * Adapter for Widget View List
     */
    private class StockWidgetViewFactory implements RemoteViewsFactory {
        private final Context applicationContext;
        private final DecimalFormat dollarFormat;
        private final DecimalFormat dollarFormatWithPlus;
        private final DecimalFormat percentageFormat;
        List<ContentValues> cvList = new ArrayList<>();

        public StockWidgetViewFactory(Context context) {
            applicationContext = context;

            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");
        }

        @Override
        public void onCreate() {
            getData();
        }

        private void getData() {
            cvList.clear();

            //Clear and restores permission later
            long identity = Binder.clearCallingIdentity();

            try {
                ContentResolver contentResolver = applicationContext.getContentResolver();

                Cursor cursor = contentResolver.query(
                        Contract.Quote.URI,
                        null,
                        null,
                        null,
                        null
                );
                if (cursor != null) {
                    while (cursor.moveToNext()) {

                        //int id = cursor.getInt(cursor.getColumnIndex(Contract.Quote._ID));
                        String symbol = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                        float price = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                        float absChange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
                        float percentChange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));

                        ContentValues cv = new ContentValues();
                        //cv.put(Contract.Quote._ID, id);
                        cv.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                        cv.put(Contract.Quote.COLUMN_PRICE, price);
                        cv.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, absChange);
                        cv.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);

                        cvList.add(cv);
                    }
                    cursor.close();
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        @Override
        public void onDataSetChanged() {
            getData();
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return cvList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            ContentValues cv = cvList.get(position);
            RemoteViews remoteViews = new RemoteViews(
                    applicationContext.getPackageName(),
                    R.layout.list_item_quote);

            String symbol = cv.getAsString(Contract.Quote.COLUMN_SYMBOL);
            remoteViews.setTextViewText(R.id.symbol, symbol);
            remoteViews.setTextViewText(R.id.price, dollarFormat.format(cv.getAsFloat(Contract.Quote.COLUMN_PRICE)));

            float absChange = cv.getAsFloat(Contract.Quote.COLUMN_ABSOLUTE_CHANGE);
            float perChange = cv.getAsFloat(Contract.Quote.COLUMN_PERCENTAGE_CHANGE);

            if (absChange>0){
                remoteViews.setInt(R.id.change,
                        "setBackgroundResource",
                        R.drawable.percent_change_pill_green);
            } else {
                remoteViews.setInt(R.id.change,
                        "setBackgroundResource",
                        R.drawable.percent_change_pill_red);
            }

            remoteViews.setTextViewText(R.id.change, percentageFormat.format(perChange / 100));

            Intent fillIntent = new Intent ();
            fillIntent.putExtra(StockChart.EXTRA_SYMBOL, symbol);
            remoteViews.setOnClickFillInIntent(R.id.stock_item, fillIntent);

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            //return null;
            return new RemoteViews(getPackageName(), R.layout.list_item_quote);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
//            if ( !cvList.isEmpty()) {
//                ContentValues cv = cvList.get(position);
//                return (Long) cv.get(Contract.Quote._ID);
//            }
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
