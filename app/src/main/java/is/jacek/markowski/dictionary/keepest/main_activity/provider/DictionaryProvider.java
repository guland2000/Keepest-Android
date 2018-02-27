/*
 * Copyright 2018 Jacek Markowski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense,  and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package is.jacek.markowski.dictionary.keepest.main_activity.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import is.jacek.markowski.dictionary.keepest.main_activity.database.Contract;
import is.jacek.markowski.dictionary.keepest.main_activity.database.DatabaseHelper;

import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.TABLE_DICTIONARY;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.URI_AUTHORITY;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.URI_PATH_DICTS_ALL;

/**
 * Created by jacek on 10.06.17.
 */

public class DictionaryProvider extends ContentProvider {
    private static final int ID_DICT_ALL = 100;
    private static UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(
                URI_AUTHORITY,
                URI_PATH_DICTS_ALL,
                ID_DICT_ALL);
    }

    private DatabaseHelper mDbHelper;

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        long rowId = -1;
        switch (sUriMatcher.match(uri)) {
            case ID_DICT_ALL: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                db.beginTransaction();
                try {
                    for (int i = 0; i < values.length; i++) {
                        rowId = db.insert(Contract.Dictionary.Entry.TABLE_DICTIONARY, null, values[i]);
                    }
                    db.setTransactionSuccessful();
                    getContext().getContentResolver().notifyChange(uri, null);
                } finally {
                    db.endTransaction();
                }
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return (int) rowId;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        switch (sUriMatcher.match(uri)) {
            case ID_DICT_ALL: {
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                cursor = db.query(TABLE_DICTIONARY, null, selection, selectionArgs, null, null, sortOrder);
                break;
            }
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
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
        long rowId = -1;
        switch (sUriMatcher.match(uri)) {
            case ID_DICT_ALL: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                try {
                    rowId = db.insert(TABLE_DICTIONARY, null, values);
                } finally {
                    //db.close();
                }
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(uri, URI_PATH_DICTS_ALL + "/" + rowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rows_deleted = 0;
        switch (sUriMatcher.match(uri)) {
            case ID_DICT_ALL: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                try {
                    rows_deleted = db.delete(TABLE_DICTIONARY, selection, selectionArgs);
                } finally {
                }
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows_deleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int itemsChanged = 0;
        switch (sUriMatcher.match(uri)) {
            case ID_DICT_ALL: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                try {
                    itemsChanged = db.update(TABLE_DICTIONARY, values, selection, selectionArgs);
                } finally {
                }
            }
        }
        // communicate change to loader manager
        getContext().getContentResolver().notifyChange(uri, null);
        return itemsChanged;
    }
}
