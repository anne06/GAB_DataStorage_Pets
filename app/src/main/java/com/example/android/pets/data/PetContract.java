package com.example.android.pets.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class PetContract {
    private PetContract() {}

    // Creation of the Uris constants
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PETS = "pets";

    public static final class PetEntry implements BaseColumns {

        // Creation of the Uri to interact with the PETS table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);

        // Table
        public static final String TABLE_NAME = "pets";

        // Columns
        /**
         * Unique ID number for the pet (only for use in the database table).
         *
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of the pet.
         *
         * Type: TEXT
         */
        public static final String COLUMN_PET_NAME = "name";

        /**
         * Breed of the pet.
         *
         * Type: TEXT
         */
        public static final String COLUMN_PET_BREED = "breed";

        /**
         * Gender of the pet.
         *
         * The only possible values are {@link #GENDER_UNKNOWN}, {@link #GENDER_MALE},
         * or {@link #GENDER_FEMALE}.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PET_GENDER = "gender";

        /**
         * Weight of the pet.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PET_WEIGHT = "weight";

        /**
         * Possible values for the gender of the pet.
         */
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;


    }
}
