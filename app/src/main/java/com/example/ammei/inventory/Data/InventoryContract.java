package com.example.ammei.inventory.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ammei on 2/20/2017.
 */

public class InventoryContract {

    /**
     * Empty Constructor to prevent instantiating the contract class.
     */
    private InventoryContract() {
    }

    /**
     * Name for the Content Provider.
     */
    public static final String CONTENT_AUTHORITY = "com.example.ammei.inventory";

    /**
     * Creating CONTENT_AUTHORITY in order to provide the base for the URI's,
     * which will be used to contact the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path for the content provider.
     */
    public static final String PATH_INVENTORY = "inventory";

    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_INVENTORY;

        /**
         * Name of database for table inventory
         */
        public static final String TABLE_NAME = "inventory";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_BEER_NAME = "name";
        public static final String COLUMN_COLOR = "color";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_ABV = "abv";
        public static final String COLUMN_TYPE_BEER = "type";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_DESCRIPTION = "description";
        /*
         * Possible beer colors for "expected color" column displayed within a spinner menu.
         */
        public static final int COLOR_UNKNOWN = 0;
        public static final int COLOR_YELLOW = 1;
        public static final int COLOR_AMBER = 2;
        public static final int COLOR_BROWN = 3;
        public static final int COLOR_BLACK = 4;

        /*
         * Returns whichever color value the user selects.
         */
        public static boolean isValidColor(int beerColor) {
            if (beerColor == COLOR_UNKNOWN ||
                    beerColor == COLOR_YELLOW ||
                    beerColor == COLOR_AMBER ||
                    beerColor == COLOR_BROWN ||
                    beerColor == COLOR_BLACK) {
                return true;
            }

            return false;
        }

        /*
         * Note: "BT" stands for "Beer Type"
         * Possible beer types user may select and is displayed within a spinner menu.
         */
        public static final int BT_UNKNOWN = 0;
        public static final int BT_ALE = 1;
        public static final int BT_PALE_ALE = 2;
        public static final int BT_LAGER = 3;
        public static final int BT_STOUT = 4;
        public static final int BT_INDIAPALE_ALE = 5;
        public static final int BT_PORTER = 6;

        /*
         * Returns whichever beer type value the user chooses.
         */
        public static boolean isValidBeerType(int beerType) {
            if (beerType == BT_UNKNOWN ||
                    beerType == BT_ALE ||
                    beerType == BT_PALE_ALE ||
                    beerType == BT_LAGER ||
                    beerType == BT_STOUT ||
                    beerType == BT_INDIAPALE_ALE ||
                    beerType == BT_PORTER) {
                return true;
            }

            return false;
        }
    }
}