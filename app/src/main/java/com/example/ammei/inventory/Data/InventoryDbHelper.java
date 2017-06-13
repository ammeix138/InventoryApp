package com.example.ammei.inventory.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.ammei.inventory.Data.InventoryContract.*;

/**
 * Created by ammei on 2/21/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyInventory.db";

    /**
     * Database version.
     */
    private static final int DATABASE_VERSION = 1;


    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This will be called whenever an entry is made within the app.
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create a String that contains the SQL statement to create the inventory table
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME +
                " ("

                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_BEER_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_COLOR + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_ABV + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_TYPE_BEER + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL); ";

        //Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    /**
     * This will be called whenever the database needs to be updated
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //The database is still at version 1, so there's nothing to be done here.
    }
}
