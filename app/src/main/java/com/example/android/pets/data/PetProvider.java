package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;

import com.example.android.pets.R;

public class PetProvider extends ContentProvider {
    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    private PetDbHelper mDbHelper;

    // Uri matcher
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final int URI_MATCHER_PETS = 100;
    public static final int URI_MATCHER_PET_ID = 101;

    static {

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, URI_MATCHER_PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", URI_MATCHER_PET_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * **************** QUERY
     *
     * @param uri
     * @param projections
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projections, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor queryCusror;

        switch (sUriMatcher.match(uri)) {

            case URI_MATCHER_PETS :
                // SELECT a DataSet
                queryCusror = db.query(PetContract.PetEntry.TABLE_NAME,
                        projections,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                        );
                break;

            case URI_MATCHER_PET_ID:
                // SELECT a specific ID
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                queryCusror = db.query(PetContract.PetEntry.TABLE_NAME,
                        projections,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                // There is no PATTERN match
                Log.e(LOG_TAG, "There is no pattern match");

                throw new IllegalArgumentException((getContext().getString(R.string.Uri_error)) + uri);
        }


        return queryCusror;
    }

    /**
     * ***************** INSERT
     *
     * @param uri
     * @param contentValues
     * @return
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        switch (sUriMatcher.match(uri)) {
            case URI_MATCHER_PETS :
                // INSERT a pet
                return insertPet(uri, contentValues);

           default:
                // There is no PATTERN match
                Log.e(LOG_TAG, "There is no pattern match");
               throw new IllegalArgumentException((getContext().getString(R.string.Uri_error)) + uri);
        }

    }

    private Uri insertPet(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        // Test the values before enter them in the db
        if (!isAllValid(contentValues)) {
            Log.e(LOG_TAG, R.string.error_insert + " - " + uri);
            return null;
        }

        // Format the values
        contentValues = formatValues(contentValues);

        // Get a SQLiteDatabase object
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(PetContract.PetEntry.TABLE_NAME, null, contentValues);

        if (id == -1){
            Log.e(LOG_TAG, R.string.error_insert + " - " + uri);
            return null;
        }
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * ***************  UPDATE
     *
     * @param uri
     * @param contentValues
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues contentValues,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_MATCHER_PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case URI_MATCHER_PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException((getContext().getString(R.string.Uri_error)) + uri);
        }
    }
    private int updatePet(@NonNull Uri uri,
                          @Nullable ContentValues contentValues,
                          @Nullable String selection,
                          @Nullable String[] selectionArgs){

        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            Log.e(LOG_TAG, "The UPDATE contentValues is empty");
            return 0;
        }

        // Test the values before enter them in the db
        if (!isAllValid(contentValues)) {
            Log.e(LOG_TAG, R.string.error_update + " - " + uri);
            return 0;
        }

        // Format the values
        contentValues = formatValues(contentValues);

        // Get a SQLiteDatabase object
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.update(PetContract.PetEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        if (id == -1){
            Log.e(LOG_TAG, R.string.error_update + " - " + uri);
        }
        return (int) id;
    }

    /**
     * ********************* DELETE
     *
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_MATCHER_PETS:
                // Delete all rows that match the selection and selection args
                return db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            case URI_MATCHER_PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException((getContext().getString(R.string.Uri_error)) + uri);
        }
    }

    /**
     * ****************** RETURN THE MIME TYPE
     * @param uri
     * @return
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case URI_MATCHER_PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case URI_MATCHER_PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /* ********************************************

               Helper methods

    ********************************************** */
    private boolean isValidName(String name){
        if (name == null)
            return false;

        if (name.trim().equals(""))
            return false;
        return true;
    }

    private boolean isValidBreed(String breed){
        return true;
    }

    private boolean isValidGender(int gender){
        switch (gender) {
            case PetEntry.GENDER_FEMALE:
            case PetEntry.GENDER_MALE:
            case PetEntry.GENDER_UNKNOWN:
                return true;
        }

        return false;
    }
    private boolean isValidWeight(int weight){
        if (weight >= 0)
            return true;
        return false;
    }

    private boolean isAllValid(ContentValues contentValues) {

        if (isValidName(contentValues.getAsString(PetEntry.COLUMN_PET_NAME)) &&
            isValidBreed(contentValues.getAsString(PetEntry.COLUMN_PET_BREED)) &&
            isValidGender(contentValues.getAsInteger(PetEntry.COLUMN_PET_GENDER)) &&
            isValidWeight(contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT))
        ) { return true;  }

        return false;
    }

    private ContentValues formatValues (ContentValues cv) {
        ContentValues aPet = new ContentValues();

        // Name
        aPet.put(PetEntry.COLUMN_PET_NAME, cv.getAsString(PetEntry.COLUMN_PET_NAME).trim() );

        // Breed
        if (cv.getAsString(PetEntry.COLUMN_PET_BREED) == null)
            aPet.put(PetEntry.COLUMN_PET_BREED, (String) null);
        else
            aPet.put(PetEntry.COLUMN_PET_BREED, cv.getAsString(PetEntry.COLUMN_PET_BREED).trim());

        // Gender
        aPet.put(PetEntry.COLUMN_PET_GENDER, cv.getAsInteger(PetEntry.COLUMN_PET_GENDER) );

        // Weight
        aPet.put(PetEntry.COLUMN_PET_WEIGHT, cv.getAsInteger(PetEntry.COLUMN_PET_WEIGHT) );

        return aPet;
    }

}
