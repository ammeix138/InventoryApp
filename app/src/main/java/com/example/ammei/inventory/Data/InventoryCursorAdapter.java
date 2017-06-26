package com.example.ammei.inventory.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.ammei.inventory.R;

import static com.example.ammei.inventory.Data.InventoryContract.InventoryEntry;

/**
 * Created by ammei on 6/4/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = InventoryEntry.class.getSimpleName();
    InventoryDbHelper mDbHelper;

    Button mRestock;

    Button mSold;
    private LayoutInflater cursorInflater;

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /*flags*/);
        cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        mSold = (Button) view.findViewById(R.id.soldButton);
        mRestock = (Button) view.findViewById(R.id.restockButton);


        TextView nameView = (TextView) view.findViewById(R.id.productName);
        TextView priceView = (TextView) view.findViewById(R.id.productPrice);
        final TextView quantityView = (TextView) view.findViewById(R.id.productQuantity);
        TextView summaryView = (TextView) view.findViewById(R.id.productDescription);

        String productName = cursor.getString(cursor.getColumnIndex
                (InventoryEntry.COLUMN_BEER_NAME));
        int productPrice = cursor.getInt(cursor.getColumnIndex
                (InventoryEntry.COLUMN_PRICE));
        final int productQuantity = cursor.getInt(cursor.getColumnIndex
                (InventoryEntry.COLUMN_QUANTITY));
        quantityView.setTag(cursor.getInt
                (cursor.getColumnIndex(InventoryEntry._ID)));
        String productDescription = cursor.getString(cursor.getColumnIndex
                (InventoryEntry.COLUMN_DESCRIPTION));

        nameView.setText(productName);
        priceView.setText("$" + Integer.toString(productPrice));
        quantityView.setText(Integer.toString(productQuantity));
        summaryView.setText(productDescription);

        mRestock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long rowId = Long.valueOf(quantityView.getTag().toString());
                String filter = "_ID=" + rowId;
                int currentQuantity = Integer.valueOf(quantityView.getText().toString());
                if (currentQuantity > 0) {
                    mDbHelper = new InventoryDbHelper(context);
                    SQLiteDatabase database = mDbHelper.getWritableDatabase();
                    int restockNewQuantity = currentQuantity + 1;

                    ContentValues newRestockValue = new ContentValues();
                    newRestockValue.put(InventoryEntry.COLUMN_QUANTITY, restockNewQuantity);
                    database.update(InventoryEntry.TABLE_NAME, newRestockValue, filter, null);
                    quantityView.setText(String.valueOf(restockNewQuantity));
                    database.close();
                }
            }
        });

        mSold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long rowId = Long.valueOf(quantityView.getTag().toString());
                String filter = "_ID=" + rowId;
                int saleCurrentQuantity = Integer.valueOf(quantityView.getText().toString());
                if (saleCurrentQuantity > 1) {
                    mDbHelper = new InventoryDbHelper(context);
                    SQLiteDatabase database = mDbHelper.getWritableDatabase();
                    int saleNewQuantity = saleCurrentQuantity - 1;

                    ContentValues newSaleValues = new ContentValues();
                    newSaleValues.put(InventoryEntry.COLUMN_QUANTITY, saleNewQuantity);
                    database.update(InventoryEntry.TABLE_NAME, newSaleValues, filter, null);
                    quantityView.setText(String.valueOf(saleNewQuantity));
                    database.close();
                }else if (saleCurrentQuantity == 1){
                    Log.i(LOG_TAG, "Button click works at 1");
                }
            }
        });

    }
}
