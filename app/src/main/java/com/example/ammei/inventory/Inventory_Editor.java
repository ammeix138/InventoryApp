package com.example.ammei.inventory;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.ammei.inventory.Data.InventoryDbHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.ammei.inventory.Data.InventoryContract.InventoryEntry;

public class Inventory_Editor extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = InventoryEntry.class.getSimpleName();

    private static final int PICK_IMAGE_REQUEST = 0;
    /**
     * Identifier for the inventory data loader
     */
    private static final int CURRENT_PRODUCT_LOADER = 0;
    /**
     * Intent to send order/restock request to a supplier
     */
    Intent emailSupplierIntent;
    Button restockButton;
    Button soldButton;
    Button mOrderButton;
    InventoryDbHelper mDbHelper;
    long itemId;
    /**
     * Global variables to increment the current quantity of beer by either
     * adding(+) or subtracting(-) by 1.
     */
    int addQuantity = +1;
    int soldQuantity = -1;
    private Cursor mCursor;
    /**
     * Content URI for the existing inventory
     */
    private Uri mCurrentProductUri;
    /**
     * EditText field to enter Beer Name
     */
    private EditText mNameOfBeer;
    /**
     * EditText to enter the quantity of beer available
     */
    private EditText mQuantityOfBeer;
    /**
     * Spinner to enter a valid color option when prompted
     */
    private Spinner mColorSpinner;
    /**
     * EditText field to enter the ABV for the beer product
     */
    private EditText mAlcoholByVolume;
    /**
     * EditText field to enter the price of the beer product
     */
    private EditText mPriceOfBeer;
    /**
     * Spinner to choose a valid type of beer when prompted
     */
    private Spinner mTypeOfBeerSpinner;
    /**
     * EditText field to enter the description of the beer product
     */
    private EditText mEnterDescription;

    private ImageView mImageView;
    /**
     * Color of the beer product. The possible valid values are in the InventoryContract.java file.
     */
    private int mBeerColor = InventoryEntry.COLOR_UNKNOWN;
    /**
     * Type of the beer product. The possible valid values are in the InventoryContract.java file.
     */
    private int mBeerType = InventoryEntry.BT_UNKNOWN;
    /**
     * Boolean flag which keeps track of whether or not the product has been edited or not
     */
    private boolean mProductHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that the user is modifying
     * this view, and we changes the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        itemId = getIntent().getLongExtra("item_id", 0);

        // If the Intent does not contain a product content URI, then we know that we are
        // creating a new inventory entry or editing an existing product.
        if (mCurrentProductUri == null) {
            // This is a new beer product entry, so change the app bar to read "Add a Product"
            setTitle(getString(R.string.app_bar_title_add_product));

            // Allows the "Delete" option menu to be hidden when creating a new entry.
            invalidateOptionsMenu();
        } else {
            // You are editing an existing inventory entry,
            // so change the app bar to read "Edit Product"
            setTitle(getString(R.string.app_bar_edit_product));

            // Initialize a loader to read the inventory data from the database,
            // and display the current values in the editor.
            getLoaderManager().initLoader(CURRENT_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from.
        mNameOfBeer = (EditText) findViewById(R.id.beerName);
        mColorSpinner = (Spinner) findViewById(R.id.Color_spinner);
        mAlcoholByVolume = (EditText) findViewById(R.id.abv_editText);
        mTypeOfBeerSpinner = (Spinner) findViewById(R.id.beerType_spinner);
        mPriceOfBeer = (EditText) findViewById(R.id.priceEditText);
        mQuantityOfBeer = (EditText) findViewById(R.id.quantityEditText);
        mEnterDescription = (EditText) findViewById(R.id.enterDescription);
        mImageView = (ImageView) findViewById(R.id.product_imageButton);

        restockButton = (Button) findViewById(R.id.restockButton);
        soldButton = (Button) findViewById(R.id.soldButton);
        mOrderButton = (Button) findViewById(R.id.orderButton);


        // OnTouchListeners set up for all input fields within the inventory editor activity.
        // So we can determine if the user clicked on or has modified them.
        mNameOfBeer.setOnTouchListener(mTouchListener);
        mColorSpinner.setOnTouchListener(mTouchListener);
        mAlcoholByVolume.setOnTouchListener(mTouchListener);
        mTypeOfBeerSpinner.setOnTouchListener(mTouchListener);
        mPriceOfBeer.setOnTouchListener(mTouchListener);
        mQuantityOfBeer.setOnTouchListener(mTouchListener);
        mEnterDescription.setOnTouchListener(mTouchListener);

        setupSpinnerColor();
        setUpSpinnerType();

        // Open Image Selector to the User when clicked.
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });

        // Initializing the order button to read the click from the user.
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send email intent when order button is clicked to the users email.
                emailSupplierIntent = new Intent(Intent.ACTION_SENDTO);
                // Only email apps should handle this.
                emailSupplierIntent.setData(Uri.parse("mailto:"));
                emailSupplierIntent.putExtra(Intent.EXTRA_EMAIL, new String[]
                        {getString(R.string.supplier_email)});
                emailSupplierIntent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.email_subject_line));
                emailSupplierIntent.putExtra(Intent.EXTRA_TEXT,
                        getString(R.string.email_text_product_order_with_name) + "\n" +
                                mNameOfBeer.getText().toString().trim() + "\n\n"
                                + getString(R.string.email_text_quantity) + "\n"
                                + mQuantityOfBeer.getText().toString().trim() + "\n\n"
                                + getString(R.string.email_text_body) + "\n\n\n"
                                + getString(R.string.email_text_ending_signature) + "\n"
                                + getString(R.string.email_text_signature_name) + "\n\n\n"
                                + getString(R.string.email_contact_phone) + "\n"
                                + getString(R.string.email_contact_email));
                // Check to ensure user operating the app as a viable email client
                if (emailSupplierIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailSupplierIntent);
                }

                Log.i(LOG_TAG, "Order button click successful");
            }
        });

    }

    /**
     * Setup the dropdown spinner that allows the user to select the color of the beer product.
     */
    private void setupSpinnerColor() {
        // Creates the adapter for the spinner. The list options are from the string array it will
        // use, while using the default spinner layout.
        ArrayAdapter beerColorSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.beer_color,
                android.R.layout.simple_spinner_item);

        // Specifies dropdown layout style.
        beerColorSpinnerAdapter.setDropDownViewResource
                (android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mColorSpinner.setAdapter(beerColorSpinnerAdapter);

        // Set the integer mSelected to the constant values.
        mColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectionColor = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selectionColor)) {
                    if (selectionColor.equals(getString(R.string.color_yellow))) {
                        mBeerColor = 1; //Beer Color = Yellow
                    } else if (selectionColor.equals(getString(R.string.color_amber))) {
                        mBeerColor = 2; //Beer Color = Amber
                    } else if (selectionColor.equals(getString(R.string.color_brown))) {
                        mBeerColor = 3; //Beer Color = Brown
                    } else if (selectionColor.equals(getString(R.string.color_black))) {
                        mBeerColor = 4; //Beer Color = Black
                    } else {
                        mBeerColor = 0; //Beer Color = Unknown
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mBeerColor = InventoryEntry.COLOR_UNKNOWN;
            }
        });

    }

    private void setUpSpinnerType() {
        ArrayAdapter beerTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_beer_types,
                android.R.layout.simple_spinner_item);

        beerTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mTypeOfBeerSpinner.setAdapter(beerTypeSpinnerAdapter);

        mTypeOfBeerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectionType = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selectionType)) {
                    if (selectionType.equals(getString(R.string.beer_type_ale))) {
                        mBeerType = 1; //Beer Type = Ale
                    } else if (selectionType.equals(getString(R.string.beer_type_pale_ale))) {
                        mBeerType = 2; //Beer Type = Pale Ale
                    } else if (selectionType.equals(getString(R.string.beer_type_lager))) {
                        mBeerType = 3; //Beer Type = Lager
                    } else if (selectionType.equals(getString(R.string.beer_type_Stout))) {
                        mBeerType = 4; //Beer Type = Stout
                    } else if (selectionType.equals(getString(R.string.beer_type_indianpale_ale))) {
                        mBeerType = 5; //Beer Type = Indian Pale Ale
                    } else if (selectionType.equals(getString(R.string.beer_type_porter))) {
                        mBeerType = 6; //Beer Type = Porter
                    } else {
                        mBeerType = 0; //Beer Type = Unknown
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mBeerType = InventoryEntry.BT_UNKNOWN;
            }
        });
    }

    /**
     * Get user input from editor and save new pet into database.
     */
    private void saveProduct() {

        // Read from the input fields
        // Use trim to get rid of trailing white space.
        String nameString = mNameOfBeer.getText().toString().trim();
        String abvString = mAlcoholByVolume.getText().toString().trim();
        String quantityString = mQuantityOfBeer.getText().toString().trim();
        String priceString = mPriceOfBeer.getText().toString().trim();
        String descriptionString = mEnterDescription.getText().toString().trim();
        String imageString = mImageView.toString();

        // Create a ContentValues object where column names are the keys,
        // and recipe input fields from the editor activity are the values.
        ContentValues productValues = new ContentValues();
        productValues.put(InventoryEntry.COLUMN_BEER_NAME, nameString);
        productValues.put(InventoryEntry.COLUMN_COLOR, mBeerColor);
        productValues.put(InventoryEntry.COLUMN_ABV, abvString);
        productValues.put(InventoryEntry.COLUMN_TYPE_BEER, mBeerType);
        productValues.put(InventoryEntry.COLUMN_PRICE, priceString);
        productValues.put(InventoryEntry.COLUMN_QUANTITY, quantityString);
        productValues.put(InventoryEntry.COLUMN_DESCRIPTION, descriptionString);
        productValues.put(InventoryEntry.COLUMN_IMAGE, imageString);

        // Will check to see if there is supposed to be a new beer product
        // and check if all the input fields in the editor are blank.
        if (mCurrentProductUri == null
                && TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty(abvString)
                && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString)
                && TextUtils.isEmpty(descriptionString)
                && TextUtils.isEmpty(imageString)
                && mBeerColor == InventoryEntry.COLOR_UNKNOWN
                && mBeerType == InventoryEntry.BT_UNKNOWN) {
            return;
        }

        // If the ABV of a particular beer product is not inputted by the user,
        // don't try to parse the string into an integer value. Use 0 as a default.
        int productABV = 0;
        if (!TextUtils.isEmpty((abvString))) {
            productABV = Integer.parseInt(abvString);
        }

        productValues.put(InventoryEntry.COLUMN_ABV, productABV);

        // If the price of a particular product is not inputted by the user, don't try to parse
        // the string into an integer value. Use 0 as a default.
        int productPrice = 0;
        if (!TextUtils.isEmpty(priceString)) {
            productPrice = Integer.parseInt(priceString);
        }

        productValues.put(InventoryEntry.COLUMN_PRICE, productPrice);

        // If the quantity available is not provided by the user, don't try to parse the string into
        // an integer value. Use 0 as a default.
        int productQuantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            productQuantity = Integer.parseInt(quantityString);
        }

        productValues.put(InventoryEntry.COLUMN_QUANTITY, productQuantity);

        // If the beer ABV or "Alcohol by volume," is not provided by the user, don't try to
        // parse the string into an integer value. Use 0 as a default.

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, productValues);

            if (newUri == null) {
                Toast.makeText(this, R.string.failed_to_insert,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.successful_insert, Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri,
                    productValues, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // if no rows were affected , then there was an error with an update.
                Toast.makeText(this, R.string.error_occured_updating,
                        Toast.LENGTH_LONG).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, R.string.successful_update, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save beer product to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            case R.id.restockButton:
                restockQuantity((int) itemId);
                break;
            case R.id.soldButton:
                decrementQuantity((int) itemId);
                int currentQuantity = Integer.parseInt(mQuantityOfBeer.getText().toString());
                if (currentQuantity >= 1) {
                    decrementQuantity((int) itemId);
                }
                // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop-up confirmation dialog for deletion.
                showDeleteConfirmationDialog(itemId);
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (mProductHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(Inventory_Editor.this);
                    return true;

                }

                // Warns user of unsaved changes by displaying dialogue.
                // OnClickListener handles user confirmation on whether or not changes should be
                // discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                NavUtils.navigateUpFromSameTask(Inventory_Editor.this);
                            }
                        };

                // Show dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product doesn't have any changes made,
        // continue with handling back button press.
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // If product shows changes made, setup dialog warning user.
        // OnClickListener handles the users confirmation that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes.
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all beer product attributes, define a projection that contains
        // all columns from the inventory table.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_BEER_NAME,
                InventoryEntry.COLUMN_COLOR,
                InventoryEntry.COLUMN_ABV,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_TYPE_BEER,
                InventoryEntry.COLUMN_DESCRIPTION,
                InventoryEntry.COLUMN_IMAGE
        };

        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Exit code early if the cursor is null or there is less than 1 row in the cursor.
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of recipe attribute that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_BEER_NAME);
            int colorColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_COLOR);
            int abvColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ABV);
            int typeColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_TYPE_BEER);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int descriptionColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_DESCRIPTION);
            int pictureColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);


            // Extract out the value from the Cursor for the given column index.
            String beerName = cursor.getString(nameColumnIndex);
            int beerColor = cursor.getInt(colorColumnIndex);
            int beerABV = cursor.getInt(abvColumnIndex);
            int beerType = cursor.getInt(typeColumnIndex);
            int beerPrice = cursor.getInt(priceColumnIndex);
            int beerQuantity = cursor.getInt(quantityColumnIndex);
            String beerDescription = cursor.getString(descriptionColumnIndex);
            String beerImage = cursor.getString(pictureColumnIndex);

            //Update the views on the screen with the values from the database.
            mNameOfBeer.setText(beerName);
            mAlcoholByVolume.setText(Integer.toString(beerABV));
            mPriceOfBeer.setText(Integer.toString(beerPrice));
            mQuantityOfBeer.setText(Integer.toString(beerQuantity));
            mEnterDescription.setText(beerDescription);
            mImageView.setImageURI(Uri.parse(beerImage));

            // Beer Color is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown)
            switch (beerColor) {
                case InventoryEntry.COLOR_YELLOW:
                    mColorSpinner.setSelection(1);
                    break;
                case InventoryEntry.COLOR_AMBER:
                    mColorSpinner.setSelection(2);
                    break;
                case InventoryEntry.COLOR_BROWN:
                    mColorSpinner.setSelection(3);
                    break;
                case InventoryEntry.COLOR_BLACK:
                    mColorSpinner.setSelection(4);
                    break;
                default:
                    mColorSpinner.setSelection(0);
                    break;
            }

            // Beer Type is a dropdown spinner, so map that constant value from the database
            // into one of the dropdown options (0 is Unknown)
            switch (beerType) {
                case InventoryEntry.BT_ALE:
                    mTypeOfBeerSpinner.setSelection(1);
                    break;
                case InventoryEntry.BT_PALE_ALE:
                    mTypeOfBeerSpinner.setSelection(2);
                    break;
                case InventoryEntry.BT_LAGER:
                    mTypeOfBeerSpinner.setSelection(3);
                    break;
                case InventoryEntry.BT_STOUT:
                    mTypeOfBeerSpinner.setSelection(4);
                    break;
                case InventoryEntry.BT_INDIANPALE_ALE:
                    mTypeOfBeerSpinner.setSelection(5);
                    break;
                case InventoryEntry.BT_PORTER:
                    mTypeOfBeerSpinner.setSelection(6);
                    break;
                default:
                    mTypeOfBeerSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameOfBeer.setText("");
        mColorSpinner.setSelection(0); // Select "Unknown" BeerColor
        mAlcoholByVolume.setText("");
        mTypeOfBeerSpinner.setSelection(0); // Select "Unknown" BeerType
        mPriceOfBeer.setText("");
        mQuantityOfBeer.setText("");
        mEnterDescription.setText("");

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_changes);
        builder.setPositiveButton(R.string.discard_option, discardButtonClickListener);
        builder.setNegativeButton(R.string.continuing_edit_option,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked "Continue Editing" button, so dismiss the dialog
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /**
     * Prompt the user to confirm that they would like to delete this product entry.
     *
     * @param itemId
     */
    private void showDeleteConfirmationDialog(long itemId) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative button on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.are_you_sure_delete_message);
        builder.setPositiveButton(R.string.delete_option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });

        builder.setNegativeButton(R.string.cancel_delete_option,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        // Create and show the AlertDialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Perform the deletion of a Product Entry from the database.
     */
    private void deleteProduct() {
        // Only performs the delete action if there is an existing beer product.
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, R.string.error_deleting_product, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful.
                Toast.makeText(this, R.string.product_deleted_toast, Toast.LENGTH_SHORT).show();
            }
        }

        // Close Activity
        finish();
    }


    private void openImageSelector() {
        mDbHelper = new InventoryDbHelper(this);
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        Intent imageIntent;

        if (Build.VERSION.SDK_INT < 19) {
            imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            imageIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            imageIntent.addCategory(Intent.CATEGORY_OPENABLE);

        }

        imageIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(imageIntent, "Select Image"),
                PICK_IMAGE_REQUEST);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mCurrentProductUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mCurrentProductUri.toString());

                mImageView.setImageBitmap(getBitmapFromUri(mCurrentProductUri));

            }
        } else if (requestCode == Activity.RESULT_OK) ;
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the view
        int targetWidth = mImageView.getWidth();
        int targetHeight = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap.
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bitmapOptions);
            input.close();

            int photoWidth = bitmapOptions.outWidth;
            int photoHeigth = bitmapOptions.outHeight;

            // Determine how much to scale down the image.
            int scaleFactor = Math.min(photoWidth / targetWidth, photoHeigth / targetHeight);

            // Decode the image file into a Bitmap sized to fill the View
            bitmapOptions.inJustDecodeBounds = false;
            bitmapOptions.inSampleSize = scaleFactor;
            bitmapOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "FAILED to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "FAILED to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    /**
     * Method to increase Quantity when "restock" button has been clicked by the user.
     * Increment should increase Quantity input field by 1 per button click.
     *
     * @param rowId
     */
    private void restockQuantity(int rowId) {
        mDbHelper = new InventoryDbHelper(this);
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        String filter = "_ID=" + rowId;
        int currentQuantity = Integer.parseInt(mQuantityOfBeer.getText().toString());
        int newQuantity = currentQuantity + addQuantity;
        ContentValues restockValues = new ContentValues();
        restockValues.put(InventoryEntry.COLUMN_QUANTITY, newQuantity);
        database.update(InventoryEntry.TABLE_NAME, restockValues, filter, null);
        database.close();
        saveProduct();

    }

    /**
     * Method to decrease Quantity when "sold" button has been clicked by the user.
     * Decrement should decrease Quantity input field by 1 per button click.
     *
     * @param rowId
     */
    private void decrementQuantity(int rowId) {
        mDbHelper = new InventoryDbHelper(this);
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        String filter = "_ID=" + rowId;
        int currentQuantity = Integer.parseInt(mQuantityOfBeer.getText().toString());
        int newQuantity = 0;

        if ((currentQuantity - soldQuantity) >= 0) {
            newQuantity = (currentQuantity - soldQuantity);
        }

        ContentValues soldValues = new ContentValues();
        soldValues.put(InventoryEntry.COLUMN_QUANTITY, newQuantity);
        database.update(InventoryEntry.TABLE_NAME, soldValues, filter, null);
        database.close();
        saveProduct();
    }

}
