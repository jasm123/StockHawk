package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;


public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor data;

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        if (data != null)
            data.close();
        data = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{
                        QuoteColumns._ID,
                        QuoteColumns.SYMBOL,
                        QuoteColumns.NAME,
                        QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE,
                        QuoteColumns.CHANGE,
                        QuoteColumns.ISUP
                },
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onDataSetChanged() {
        if (data != null)
            data.close();
        // This method is called by the app hosting the widget (e.g., the launcher)
        // However, our ContentProvider is not exported so it doesn't have access to the
        // data. Therefore we need to clear (and finally restore) the calling identity so
        // that calls use our process and permission
        final long identityToken = Binder.clearCallingIdentity();
        //The same query we write in MyStocksActivity.class
        data = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{
                        QuoteColumns._ID,
                        QuoteColumns.SYMBOL,
                        QuoteColumns.NAME,
                        QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE,
                        QuoteColumns.CHANGE,
                        QuoteColumns.ISUP
                },
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                data == null || !data.moveToPosition(position)) {
            return null;
        }

        // Get the layout
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);

        // Bind data to the views
        views.setTextViewText(R.id.stock_symbol, data.getString(data.getColumnIndex("symbol")));
        views.setTextViewText(R.id.bid_price, data.getString(data.getColumnIndex("bid_price")));
        if (data.getInt(data.getColumnIndex("is_up")) == 1) {
            views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            // views.setImageViewResource(R.id.arrow,R.drawable.green);
        } else {
            views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            //views.setImageViewResource(R.id.arrow, R.drawable.red);
        }

        if (Utils.showPercent) {
            views.setTextViewText(R.id.change, data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
        } else {
            views.setTextViewText(R.id.change, data.getString(data.getColumnIndex(QuoteColumns.CHANGE)));
        }

//        final Intent fillInIntent = new Intent();
//        fillInIntent.putExtra("symbol", data.getString(data.getColumnIndex(QuoteColumns.SYMBOL)));
//        views.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        // Get the row ID for the view at the specified position
        if (data != null && data.moveToPosition(position)) {
            return data.getLong(data.getColumnIndexOrThrow(QuoteColumns._ID));
        }
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
