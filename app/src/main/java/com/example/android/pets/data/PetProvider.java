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
        return 0;
    }
}
