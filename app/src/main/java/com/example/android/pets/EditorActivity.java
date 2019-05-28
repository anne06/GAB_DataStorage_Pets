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
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * PetContract.PetEntry.GENDER_UNKNOWN for unknown gender,
     * PetContract.PetEntry.GENDER_MALE for male,
     * PetContract.PetEntry.GENDER_FEMALE for female.
     */
    private int mGender = PetEntry.GENDER_UNKNOWN;

    private PetDbHelper mDbHelper;

    // Store if we are in EDIT or INSERT mode
    private boolean isEdit = false;

    // Give a default value to the loader
    private static final int PET_LOADER = 3;

    // Store the current Pet Uri when we are in EDIT mode
    private Uri mCurrentPetUri;

    // Track if something has changed or not
    private boolean mPetHasChanged = false;
    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mPetHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mNameEditText.setOnTouchListener(mTouchListener);

        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mBreedEditText.setOnTouchListener(mTouchListener);

        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mWeightEditText.setOnTouchListener(mTouchListener);

        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        mDbHelper = new PetDbHelper(this);

        // Indicate if we are in EDIT or INSERT mode
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();

        if(bd != null)
        {
            String getType = (String) bd.get("type");
            if (getType.equalsIgnoreCase(CatalogActivity.EDITOR_EDIT_MODE)){
                // EDITOR MODE
                isEdit = true;
                this.setTitle(R.string.editor_activity_title_edit_pet);
                mCurrentPetUri = intent.getData();
                getLoaderManager().initLoader(PET_LOADER, null, this);

            } else {
                // INSERT MODE
                isEdit = false;
                this.setTitle(R.string.editor_activity_title_new_pet);

                // in INSERT mode, the pet does not already exist so it cannot be deleted
                // Invalidate the options menu, so the "Delete" menu option can be hidden.
                // (It doesn't make sense to delete a pet that hasn't been created yet.)
                invalidateOptionsMenu();
            }
        } else {
            Log.e(LOG_TAG, "Intent type not provided");
            return;
        }

        //

    }

    /********************************************
     * INSERT or UPDATE a PET
     *********************************************/
    private void savePet() {

        String petName = ((EditText) findViewById(R.id.edit_pet_name)).getText().toString();
        String petBreed = ((EditText) findViewById(R.id.edit_pet_breed)).getText().toString();
        String petWeightString = ((EditText) findViewById(R.id.edit_pet_weight)).getText().toString();

        int petWeight = 0;

        if (petWeightString == null || petWeightString.trim().length() == 0) {
            petWeight = 0;
        } else {
            try {
                petWeight = Integer.parseInt(petWeightString);
            } catch (NumberFormatException nfe) {
                Log.e(LOG_TAG, "Catch Pet weight");
                Toast.makeText(this, R.string.insert_error, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, petName);
        values.put(PetEntry.COLUMN_PET_BREED, petBreed);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, petWeight);

        if (isEdit){
            // We are in UPDATE mode
            long petID = ContentUris.parseId(mCurrentPetUri);

            int nbPetUpdated = getContentResolver().update(mCurrentPetUri, values, null, null);

            if (nbPetUpdated == 1) {
                Toast.makeText(this,
                        getString(R.string.update_ok),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        getString(R.string.update_error),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // We are in INSERT mode

            // Use of a Content Values to insert data in the DB
            // Creation of all key-values for a pet
            Uri uriPetId = getContentResolver().insert(PetEntry.CONTENT_URI, values);

            if (uriPetId == null) {
                Toast.makeText(this,
                        getString(R.string.insert_error),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        getString(R.string.ID_pet_inserted),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    /********************************************************
     *
     * Display a Dialog if we live without saving data
     *
     *********************************************************/
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }




    /***************************************************************************************
     *
     * SPINNER
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     *
    ****************************************************************************************/
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetEntry.GENDER_UNKNOWN; // Unknown
            }
        });
    }

    /****************************************************
     *
     *  MENU - MENU ITEMS
     *
     *****************************************************/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (!isEdit) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();

                // Don't forget it !!!
                // Means that we return to the Catalog Activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)

                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**************************************
     *
     * LOADER
     *
     **************************************/

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define the projection
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        // This loader will execute the ContentProvider's query method
        // in a bacground thread
        return new CursorLoader(this,
                mCurrentPetUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            mNameEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME)));
            mBreedEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED)));
            mWeightEditText.setText(Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_WEIGHT))));

            mGender = cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER));
            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
 /*           switch (mGender) {
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
            */
            mGenderSpinner.setSelection(mGender);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mGenderSpinner.setSelection(PetEntry.GENDER_UNKNOWN);
        mWeightEditText.setText("");
    }
}