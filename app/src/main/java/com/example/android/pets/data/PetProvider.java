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
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }


        return queryCusror;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

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
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
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
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        long id = db.insert(PetContract.PetEntry.TABLE_NAME, null, contentValues);
        if (id == -1){
            Log.e(LOG_TAG, R.string.error_insert + " - " + uri);
            return null;
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        // Test the values before enter them in the db
        if (!isAllValid(contentValues)) {
            Log.e(LOG_TAG, R.string.error_insert + " - " + uri);
            return 0;
        }

        // Format the values
        contentValues = formatValues(contentValues);

        // TODO implement this feature
        return 0;
    }

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
