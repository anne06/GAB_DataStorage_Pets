/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EDITOR_EDIT_MODE = "Edit";
    public static final String EDITOR_INSERT_MODE = "Insert";


    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();
    private static final int PET_LOADER = 1;
    private PetDbHelper mDbHelper;
    private PetCursorAdapter mPetCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new PetDbHelper(this);

        // Find the ListView which will be populated with the pet data
        ListView petListView = (ListView) findViewById(R.id.list_view_pet);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        // Bind the Empty View to the ListView
        petListView.setEmptyView(emptyView);

        // Create the CURSOR ADAPTER and bind it to the ListView
        mPetCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mPetCursorAdapter);

        // Add an onItemClick listener on the List View
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Use a ContentResolver + ContentProvider + UriMatcher to insert a new pet
                Uri queryId = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
                Log.e(LOG_TAG, queryId.toString());

                intent.putExtra("type", EDITOR_EDIT_MODE);
                intent.setData(queryId);
                intent.putExtra("queryURI", queryId.toString());
                startActivity(intent);
            }
        });

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.putExtra("type", EDITOR_INSERT_MODE);

                startActivity(intent);
            }
        });

        // Create and initialize the CURSOR LOADER
        // to execute the query in a background thread
        getLoaderManager().initLoader(PET_LOADER, null, this);

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
                insertPet();
                return true;

                // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /***************************************
     *
     * InsertPet
     * <p>
     * return: id or -1 (error)
     *
     ****************************************/
    private long insertPet() {
        // Use of a Content Values to insert data in the DB
        // Creation of all key-values for a pet
        ContentValues insertDummyValues = new ContentValues();
        insertDummyValues.put(PetEntry.COLUMN_PET_NAME, "Toto");
        insertDummyValues.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        insertDummyValues.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        insertDummyValues.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // Use a ContentResolver + ContentProvider + UriMatcher to insert a new pet
        Uri newPetID = getContentResolver().insert(PetEntry.CONTENT_URI, insertDummyValues);

        if (newPetID == null) {
            Toast.makeText(this,
                    getString(R.string.insert_error),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    getString(R.string.ID_pet_inserted),
                    Toast.LENGTH_SHORT).show();
        }

        return ContentUris.parseId(newPetID);
    }

    /********************************************
     *
     * DELETE a PET
     *
     *********************************************/
    private void deleteAllPets() {

        int nbPetDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        if (nbPetDeleted > 0) {
            Toast.makeText(this,
                    getString(R.string.catalog_delete_pets_successful),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    getString(R.string.catalog_delete_pet_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /*************************************************************************************
     *
     *  Dialog if the DELETE menu item is selected
     *
     **************************************************************************************/
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.catalog_delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllPets();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /********************************
     *
     * ADD A CURSOR LOADER
     *
     *********************************/
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define the projection
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED
        };

        // This loader will execute the ContentProvider's query method
        // in a bacground thread
        return new CursorLoader(this,
                PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mPetCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPetCursorAdapter.swapCursor(null);

    }
}

