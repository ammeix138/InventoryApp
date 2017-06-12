package com.example.ammei.inventory.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.example.ammei.inventory.Data.InventoryContract.CONTENT_AUTHORITY;
import static com.example.ammei.inventory.Data.InventoryContract.PATH_INVENTORY;
import static com.example.ammei.inventory.Data.InventoryContract.InventoryEntry;

/**
 * Created by ammei on 6/4/2017.
 */

public class InventoryProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the inventory table
     */
    private static final int PRODUCT = 100;

    /**
     * URI matcher code for the content URI for a single beer product in the inventory table.
     */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher which matches a content URI to a corresponding code.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_INVENTORY, PRODUCT);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_INVENTORY + "/#", PRODUCT_ID);
    }

    /**
     * Database helper object
     */
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get readable database.
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Cursor will hold the result of the query
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                cursor = database.query(InventoryEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Performs actual query on the inventory table wher the _id equals 3 to
                // return a cursor containing that row of the data.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //Set Notification uri on the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        //Return the cursor.
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a single beer recipe into the database with the given content values. Return the new
     * content URI for that specific row in the database.
     *
     * @param uri
     * @param values
     * @return
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        // Check the name is not null.
        String name = values.getAsString(InventoryEntry.COLUMN_BEER_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Beer product requires a name");
        }

        // Check that the beer color is valid.
        Integer beerColor = values.getAsInteger(InventoryEntry.COLUMN_COLOR);
        if (beerColor == null || !InventoryEntry.isValidColor(beerColor)) {
            throw new IllegalArgumentException("Beer product must have a valid expected color");
        }

        // Check to see if the beerABV is not null,
        // and the integer provided is not less than 0.
        Integer beerABV = values.getAsInteger(InventoryEntry.COLUMN_ABV);
        if (beerABV == null && beerABV < 0) {
            throw new IllegalArgumentException("Beer product requires valid ABV amount");
        }

        // Check that the beerType is not null, and a valid beer type is provided.
        Integer beerType = values.getAsInteger(InventoryEntry.COLUMN_TYPE_BEER);
        if (beerType == null || !InventoryEntry.isValidBeerType(beerType)) {
            throw new IllegalArgumentException("Beer product requires a valid type");
        }

        // Check that the beer price provided is not null,
        // and the integer provided is not less than 0.
        Integer beerPrice = values.getAsInteger(InventoryEntry.COLUMN_PRICE);
        if (beerPrice != null && beerPrice < 0) {
            throw new IllegalArgumentException("Beer product requires a valid price");
        }

        // Check that the beer quantity is not null.
        Integer beerQuantity = values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
        if (beerQuantity == null && beerQuantity < 0) {
            throw new IllegalArgumentException("Beer product requires an amount brewed");
        }

        // Check the product description is not null.
        String productDescription = values.getAsString(InventoryEntry.COLUMN_DESCRIPTION);
        if (productDescription == null) {
            throw new IllegalArgumentException("Entry must include product description");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new beer recipe with the given values
        long recipeID = database.insert(InventoryEntry.TABLE_NAME, null, values);

        if (recipeID == -2) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, recipeID);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return updateRecipe(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "-?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateRecipe(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    private int updateRecipe(Uri uri, ContentValues values,
                             String selection, String[] selectionArgs) {
        if (values.containsKey(InventoryEntry.COLUMN_BEER_NAME)) {
            String beerName = values.getAsString(InventoryEntry.COLUMN_BEER_NAME);
            if (beerName == null) {
                throw new IllegalArgumentException("Product requires name");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_COLOR)) {
            Integer beerColor = values.getAsInteger(InventoryEntry.COLUMN_COLOR);
            if (beerColor == null || !InventoryEntry.isValidColor(beerColor)) {
                throw new IllegalArgumentException("Product requires valid beer color");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_QUANTITY)) {
            Integer beerAmount = values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
            if (beerAmount == null) {
                throw new IllegalArgumentException("Product requires a brewed amount");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_ABV)) {
            Integer beerABV = values.getAsInteger(InventoryEntry.COLUMN_ABV);
            if (beerABV == null) {
                throw new IllegalArgumentException("Product requires a measured ABV");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_TYPE_BEER)) {
            Integer beerType = values.getAsInteger(InventoryEntry.COLUMN_TYPE_BEER);
            if (beerType == null || !InventoryEntry.isValidBeerType(beerType)) {
                throw new IllegalArgumentException("Product requires a valid beer type");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_PRICE)) {
            Integer beerPrice = values.getAsInteger(InventoryEntry.COLUMN_PRICE);
            if (beerPrice == null) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_DESCRIPTION)) {
            String beerRecipe = values.getAsString(InventoryEntry.COLUMN_DESCRIPTION);
            if (beerRecipe == null) {
                throw new IllegalArgumentException("Product requires a description");
            }
        }

        // If there are no values to update, then don't try to update the database.
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, retrieve writable database inorder to update the data.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdate = database.update
                (InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdate != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdate;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database.

        int rowsDeleted;

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                // Delete all rows that match the selection and selection args.
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI.
                selection = InventoryEntry._ID + "-?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }
}
