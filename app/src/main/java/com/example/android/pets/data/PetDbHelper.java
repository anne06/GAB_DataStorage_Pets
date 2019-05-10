package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;

public class PetDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = PetDbHelper.class.getSimpleName();

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dbshelter.db";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PetEntry.TABLE_NAME + "("
            + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
            + PetEntry.COLUMN_PET_BREED + " TEXT, "
            + PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
            + PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

    public PetDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);

        Log.e(LOG_TAG, "Table creation: " + SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
