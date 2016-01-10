package com.koleshop.appkoleshop.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.koleshop.appkoleshop.model.Category;
import com.koleshop.appkoleshop.model.Product;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class ProductDatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "ProductDatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "shopnetDatabase";

    // Table Names
    private static final String TABLE_PRODUCT = "product";
    private static final String TABLE_CATEGORY = "category";
    private static final String TABLE_PRODUCT_CATEGORY = "product_category";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    // Category table create statement
    private static final String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT" + ")";
    // PRODUCT Table - column names
    private static final String KEY_PRICE = "price";
    private static final String KEY_IMAGEURL = "imageUrl";
    private static final String KEY_MEASUREMENTUNITS = "measurementUnit";
    private static final String KEY_VARIENT = "varient";
    private static final String KEY_PACKINGUNITID = "packingUnitId";
    private static final String KEY_PACKINGAMOUNT = "packingAmount";
    private static final String KEY_DESC = "description";
    // PRODUCT_CATEGORY Table - column names
    private static final String KEY_PRODUCT_ID = "productId";
    private static final String KEY_CATEGORY_ID = "categoryId";
    private static final String CREATE_TABLE_PRODUCT = "CREATE TABLE "
            + TABLE_PRODUCT + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME
            + " TEXT," + KEY_IMAGEURL + " TEXT," + KEY_PRICE + " REAL," + KEY_MEASUREMENTUNITS + " INTEGER," + KEY_VARIENT
            + " TEXT," + KEY_CATEGORY_ID + " INTEGER," + KEY_PACKINGUNITID + " INTEGER," + KEY_PACKINGAMOUNT + " REAL," + KEY_DESC + " TEXT" + ")";
    // Table Create Statements
    // Product table create statement
    int id;
    String name;
    String imageUrl;
    float price;
    int measurementUnits;
    String varient;


    public ProductDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_PRODUCT);
        db.execSQL(CREATE_TABLE_CATEGORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);

        // create new tables
        onCreate(db);
    }

    /*
     * Creating a Product
     */
    public long createProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, product.getId());
        values.put(KEY_NAME, product.getName());
        values.put(KEY_IMAGEURL, product.getImageUrl());
        values.put(KEY_PRICE, product.getPrice());
        values.put(KEY_MEASUREMENTUNITS, product.getMeasurementUnit());
        values.put(KEY_VARIENT, product.getVarient());
        values.put(KEY_CATEGORY_ID, product.getCategoryId());
        values.put(KEY_PACKINGUNITID, product.getPackingUnitId());
        values.put(KEY_PACKINGAMOUNT, product.getPackingAmount());
        values.put(KEY_DESC, product.getDesc());

        // insert row
        long product_id = db.insert(TABLE_PRODUCT, null, values);

        // assigning category to product
        //createProductCategory(product.getId(),category.getId());
        //TODO save product to DB server
        return product_id;
    }

    /*
     * get single product
     */
    public Product getProduct(long product_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCT + " WHERE "
                + KEY_ID + " = " + product_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Product product = new Product();
        product.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        product.setName(c.getString(c.getColumnIndex(KEY_NAME)));
        product.setImageUrl(c.getString(c.getColumnIndex(KEY_IMAGEURL)));
        product.setPrice(c.getFloat(c.getColumnIndex(KEY_PRICE)));
        product.setMeasurementUnit(c.getInt(c.getColumnIndex(KEY_MEASUREMENTUNITS)));
        product.setVarient(c.getString(c.getColumnIndex(KEY_VARIENT)));
        product.setCategoryId(c.getInt(c.getColumnIndex(KEY_CATEGORY_ID)));
        product.setPackingUnitId(c.getInt(c.getColumnIndex(KEY_PACKINGUNITID)));
        product.setPackingAmount(c.getFloat(c.getColumnIndex(KEY_PACKINGAMOUNT)));
        product.setDesc(c.getString(c.getColumnIndex(KEY_DESC)));

        return product;
    }

    /*
     * getting all products
     * */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<Product>();
        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCT;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                product.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                product.setImageUrl(c.getString(c.getColumnIndex(KEY_IMAGEURL)));
                product.setPrice(c.getFloat(c.getColumnIndex(KEY_PRICE)));
                product.setMeasurementUnit(c.getInt(c.getColumnIndex(KEY_MEASUREMENTUNITS)));
                product.setVarient(c.getString(c.getColumnIndex(KEY_VARIENT)));
                product.setCategoryId(c.getInt(c.getColumnIndex(KEY_CATEGORY_ID)));
                product.setPackingUnitId(c.getInt(c.getColumnIndex(KEY_PACKINGUNITID)));
                product.setPackingAmount(c.getFloat(c.getColumnIndex(KEY_PACKINGAMOUNT)));
                product.setDesc(c.getString(c.getColumnIndex(KEY_DESC)));

                products.add(product);
            } while (c.moveToNext());
        }

        return products;
    }

    /*
     * getting all todos under single tag
     * */
    public List<Product> getAllProductsByCategory(int category_id, int limit) {
        List<Product> products = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCT + " p " + " WHERE p."
                + KEY_CATEGORY_ID + " = '" + category_id + "'" + " LIMIT " + limit;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                product.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                product.setImageUrl(c.getString(c.getColumnIndex(KEY_IMAGEURL)));
                product.setPrice(c.getFloat(c.getColumnIndex(KEY_PRICE)));
                product.setMeasurementUnit(c.getInt(c.getColumnIndex(KEY_MEASUREMENTUNITS)));
                product.setVarient(c.getString(c.getColumnIndex(KEY_VARIENT)));
                product.setCategoryId(c.getInt(c.getColumnIndex(KEY_CATEGORY_ID)));
                product.setPackingUnitId(c.getInt(c.getColumnIndex(KEY_PACKINGUNITID)));
                product.setPackingAmount(c.getFloat(c.getColumnIndex(KEY_PACKINGAMOUNT)));
                product.setDesc(c.getString(c.getColumnIndex(KEY_DESC)));

                products.add(product);
            } while (c.moveToNext());
        }

        return products;
    }

    /*
     * Updating a product
     */
    public int updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, product.getId());
        values.put(KEY_NAME, product.getName());
        values.put(KEY_IMAGEURL, product.getImageUrl());
        values.put(KEY_PRICE, product.getPrice());
        values.put(KEY_MEASUREMENTUNITS, product.getMeasurementUnit());
        values.put(KEY_VARIENT, product.getVarient());
        values.put(KEY_CATEGORY_ID, product.getCategoryId());
        values.put(KEY_PACKINGUNITID, product.getPackingUnitId());
        values.put(KEY_PACKINGAMOUNT, product.getPackingAmount());
        values.put(KEY_DESC, product.getDesc());

        // updating row
        return db.update(TABLE_PRODUCT, values, KEY_ID + " = ?",
                new String[]{String.valueOf(product.getId())});
    }

    /*
     * Deleting a product
     */
    public void deleteProduct(long product_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRODUCT, KEY_ID + " = ?",
                new String[]{String.valueOf(product_id)});
    }

    /*
     * Creating a category
     */
    public boolean createCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, category.getId());
        values.put(KEY_NAME, category.getName());

        // insert row
        db.insert(TABLE_CATEGORY, null, values);

        return true;
    }

    /**
     * getting all categories
     */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<Category>();
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                category.setName(c.getString(c.getColumnIndex(KEY_NAME)));

                // adding to tags list
                categories.add(category);
            } while (c.moveToNext());
        }
        return categories;
    }

    /*
     * Updating a category
     */
    public int updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, category.getName());

        // updating row
        return db.update(TABLE_CATEGORY, values, KEY_ID + " = ?",
                new String[]{String.valueOf(category.getId())});
    }

    /*
     * Deleting a category
     */
    public void deleteCategory(long category_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORY, KEY_ID + " = ?",
                new String[]{String.valueOf(category_id)});
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

}
