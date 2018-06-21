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
import com.example.android.pets.data.PetContract.PetEntry;

public class PetProvider extends ContentProvider {

    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    private PetDbHelper dbHelper;

    private static final int PETS = 100;
    private static final int PET_ID = 101;

    private static final UriMatcher sUriMathcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMathcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMathcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor;
        int match = sUriMathcher.match(uri);

        switch (match) {
            case PETS:
                projection = new String[]{PetEntry._ID, PetEntry.COLUMN_PET_NAME, PetEntry.COLUMN_PET_BREED, PetEntry.COLUMN_PET_GENDER, PetEntry.COLUMN_PET_WEIGHT};
                cursor = db.query(PetEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);
                break;
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMathcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertPet (Uri uri, ContentValues values) {
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        String breed = values.getAsString(PetEntry.COLUMN_PET_BREED);
        int weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        int gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);

        if (name == null || breed == null || weight == 0 || gender == 0)    {
            throw new IllegalArgumentException("Please insert them all");
        }


        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(PetEntry.TABLE_NAME, null, values);
        if (id == -1) {
            return null;
        }

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
