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
import is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.COLUMN_TAG;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TABLE_TAGS;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TABLE_WORD_TAG_RELATIONS;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TAG_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.WORD_ID;

/**
 * Created by jacek on 10.06.17.
 */

public class TagsProvider extends ContentProvider {
    private static final int ID_GET_TAGS = 100;
    private static final int ID_CREATE_TAG = 101;
    private static final int ID_DELETE_TAG = 102;
    private static final int ID_UPDATE_TAG = 103;
    private static final int ID_CONNECT_WORD_WITH_TAG = 104;
    private static final int ID_REMOVE_TAG_FROM_WORD = 105;
    private static final int ID_CONNECTED_WORD_TAGS = 106;

    private static final String TAG = TagsProvider.class.getName();
    private static UriMatcher sUriMatcher;


    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(
                Contract.Tag.URI_AUTHORITY,
                Contract.Tag.URI_WORD_TAGS + "/#",
                ID_GET_TAGS);
        sUriMatcher.addURI(
                Contract.Tag.URI_AUTHORITY,
                Contract.Tag.URI_CREATE_TAG,
                ID_CREATE_TAG);
        sUriMatcher.addURI(
                Contract.Tag.URI_AUTHORITY,
                Contract.Tag.URI_DELETE_TAG,
                ID_DELETE_TAG);
        sUriMatcher.addURI(
                Contract.Tag.URI_AUTHORITY,
                Contract.Tag.URI_UPDATE_TAG,
                ID_UPDATE_TAG);
        sUriMatcher.addURI(
                Contract.Tag.URI_AUTHORITY,
                Contract.Tag.URI_CONNECT_WORD_WITH_TAG,
                ID_CONNECT_WORD_WITH_TAG);
        sUriMatcher.addURI(
                Contract.Tag.URI_AUTHORITY,
                Contract.Tag.URI_REMOVE_TAG_FROM_WORD,
                ID_REMOVE_TAG_FROM_WORD);
        sUriMatcher.addURI(
                Contract.Tag.URI_AUTHORITY,
                Contract.Tag.URI_WORD_TAG_CONNECTED,
                ID_CONNECTED_WORD_TAGS);
    }

    private DatabaseHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        int wordId = -1;
        switch (sUriMatcher.match(uri)) {
            case ID_GET_TAGS: {
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                String sql;
                wordId = Integer.valueOf(uri.getLastPathSegment());
                // select columns
                String selectFrom = "SELECT * FROM ";
                // search
                String where = " WHERE " + TABLE_TAGS + "." + COLUMN_TAG + " LIKE ? ";
                String wordStart = "%" + selectionArgs[0];
                String like = "%" + selectionArgs[0] + "%";
                String orderBy = " ORDER BY (CASE WHEN " + TABLE_TAGS + "." + COLUMN_TAG + " LIKE \"" + wordStart + "\" THEN 1 ELSE 2 END)";
                if (wordStart.trim().equals("%")) {
                    orderBy = " ORDER BY " + TABLE_TAGS + "." + COLUMN_TAG + " COLLATE UNICODE ASC ";
                }

                // word id, 0 if new word
                // join tables
                String tags_of_word = " (SELECT " + TAG_ID + " ," + WORD_ID + " FROM " + TABLE_WORD_TAG_RELATIONS + " WHERE " + WORD_ID + "=?) ";
                String joinedTable = TABLE_TAGS + " LEFT JOIN " + tags_of_word + " ON " + TABLE_TAGS + "." + _ID + "=" + TAG_ID;

                sql = selectFrom
                        + joinedTable
                        + where // search term
                        + orderBy;
                cursor = db.rawQuery(sql, new String[]{Integer.toString(wordId), like});

                break;
            }
            case ID_CONNECTED_WORD_TAGS: {
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                cursor = db.query(TABLE_WORD_TAG_RELATIONS, null, selection, selectionArgs, null, null, null);
                break;
            }
        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), UriHelper.TagsWord.buildTagsWordUri(wordId));
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
        boolean result = false;
        switch (sUriMatcher.match(uri)) {
            case ID_CREATE_TAG: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                String tag = values.getAsString(COLUMN_TAG);
                if (tag.length() > 0) {
                    String sql = "INSERT OR IGNORE INTO "
                            + TABLE_TAGS
                            + "(" + COLUMN_TAG + ")"
                            + " VALUES(\"" + tag + "\");";
                    db.execSQL(sql);
                    // create to if not exists
                    result = true;
                }
                break;
            }
            case ID_CONNECT_WORD_WITH_TAG: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                // create to if not exists
                String tagId = "\"" + values.getAsInteger(TAG_ID) + "\"";
                String wordId = "\"" + values.getAsInteger(WORD_ID) + "\"";
                String sql = "INSERT OR IGNORE INTO "
                        + TABLE_WORD_TAG_RELATIONS
                        + "(" + TAG_ID + "," + WORD_ID + ")"
                        + " VALUES(" + tagId + "," + wordId + ");";
                db.execSQL(sql);
                result = true;
                break;
            }

        }
        getContext().getContentResolver().notifyChange(UriHelper.TagsWord.buildTagsWordUri(-1), null);
        return Uri.withAppendedPath(uri, Contract.Tag.URI_WORD_TAGS + "/" + result);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case ID_REMOVE_TAG_FROM_WORD: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                db.delete(TABLE_WORD_TAG_RELATIONS, selection, selectionArgs);
                break;
            }
            case ID_DELETE_TAG: {
                // delete tag from tags table
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                db.delete(TABLE_TAGS, selection, selectionArgs);
                // delete from ralations table
                db.delete(TABLE_WORD_TAG_RELATIONS, TAG_ID + "=?", selectionArgs);
                getContext().getContentResolver().notifyChange(UriHelper.TagsWord.buildTagsWordUri(-1), null);
                break;
            }
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int itemsChanged = 0;
        switch (sUriMatcher.match(uri)) {
            case ID_UPDATE_TAG: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                try {
                    itemsChanged = db.update(TABLE_TAGS, values, selection, selectionArgs);
                } catch (Exception e) {
                } finally {
                }
            }
        }
        // communicate change to loader manager
        getContext().getContentResolver().notifyChange(uri, null);
        return itemsChanged;
    }
}
