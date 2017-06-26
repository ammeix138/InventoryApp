package com.example.ammei.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ammei.inventory.Data.InventoryContract.InventoryEntry;
import com.example.ammei.inventory.Data.InventoryCursorAdapter;

public class Inventory_Activity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the inventory data loader */
    private static final int INVENTORY_LOADER = 0;

    /** Adapter for the ListView */
    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_catalog);

        // Listens for button click on "Add Product" TextView and sends Intent
        // to display Inventory_Detail Activity to the user.
        TextView product = (TextView) findViewById(R.id.addProduct);
        product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inventoryIntent = new Intent(Inventory_Activity.this, Inventory_Detail.class);
                startActivity(inventoryIntent);
            }
        });

        // Find the ListView which will be populated with the inventory data.
        ListView productListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView,
        // so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.emptyView);
        productListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of inventory data in the Cursor.
        // There is no inventory data yet.
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        //Calls the EditorActivity from the CatalogActivity when clicked by the user to edit,
        //an existing product from the list.
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent myIntent = new Intent(Inventory_Activity.this, Inventory_Detail.class);

                Uri currentInventoryUri = ContentUris.withAppendedId
                        (InventoryEntry.CONTENT_URI, id);

                myIntent.setData(currentInventoryUri);

                startActivity(myIntent);
            }
        });

        // Starts the Loader.
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    private void insertProduct() {
        // Create a ContentValues object where column names are the keys,
        // and the beer product fields are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_BEER_NAME, "Red Hook");
        values.put(InventoryEntry.COLUMN_COLOR, InventoryEntry.COLOR_UNKNOWN);
        values.put(InventoryEntry.COLUMN_ABV, 6);
        values.put(InventoryEntry.COLUMN_PRICE, 15);
        values.put(InventoryEntry.COLUMN_QUANTITY, 8);
        values.put(InventoryEntry.COLUMN_TYPE_BEER, InventoryEntry.BT_UNKNOWN);
        values.put(InventoryEntry.COLUMN_DESCRIPTION, "My Description");
        values.put(InventoryEntry.COLUMN_IMAGE, "image");

        // Insert a new row for every Beer entry into the provider using the ContentResolver.
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from inventory database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            // Responds to a click on the "Delete All Entries" menu option.
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_BEER_NAME,
                InventoryEntry.COLUMN_COLOR,
                InventoryEntry.COLUMN_ABV,
                InventoryEntry.COLUMN_TYPE_BEER,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_DESCRIPTION,
                InventoryEntry.COLUMN_IMAGE
        };

        // This loader will execute the ContentProviders query method on a background thread.
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the InventoryCursorAdapter with this new cursor containing updated inventory data.
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted.
        mCursorAdapter.swapCursor(null);
    }
}
