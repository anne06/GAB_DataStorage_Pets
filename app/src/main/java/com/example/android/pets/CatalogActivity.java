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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;


/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {
    private PetDbHelper mDbHelper;

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new PetDbHelper(this);
        // Called after, in the onStart() method
        // displayDatabaseInfo();
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
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * InsertPet
     *
     * return: id or -1 (error)
     */
    private long insertPet(){
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
                    getString(R.string.error_insert),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    getString(R.string.ID_pet_inserted),
                    Toast.LENGTH_SHORT).show();
        }

        return ContentUris.parseId(newPetID);
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // Display the number of rows in the Cursor (which reflects the number of rows in the
        // pets table in the database).
        TextView allValues = (TextView) findViewById(R.id.text_view_pet);

        // Define the projection
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        // Execute the query by using the ContentResolver - ContentProvider and UriMatcher
        Cursor cursor = (Cursor) getContentResolver().query(PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
        int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
        int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
        int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

        try {

            allValues.setText("Number of rows in pets database table: " + cursor.getCount());
            allValues.append("\n\n_id - name - breed - gender - weight\n");

            while(cursor.moveToNext()){
                allValues.append("\n" + cursor.getInt(idColumnIndex) +
                        " - " + cursor.getString(nameColumnIndex) +
                        " - " + cursor.getString(breedColumnIndex) +
                        " - ");

                int gender = cursor.getInt(genderColumnIndex);
                if (gender == PetEntry.GENDER_MALE){
                    allValues.append(getString(R.string.gender_male));
                } else if (gender == PetEntry.GENDER_FEMALE){
                    allValues.append(getString(R.string.gender_female));
                } else {
                    allValues.append(getString(R.string.gender_unknown));
                }

                allValues.append(" - " + cursor.getInt(weightColumnIndex));
            }

            //displayView.setText(allValues);


        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

}

