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
import android.util.Log;

import java.util.Set;

import is.jacek.markowski.dictionary.keepest.main_activity.database.Contract;
import is.jacek.markowski.dictionary.keepest.main_activity.database.DatabaseHelper;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.COLUMN_TAG;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TABLE_TAGS;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TABLE_WORD_TAG_RELATIONS;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TAG_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.WORD_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_DICTIONARY_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_LEVEL;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_NEXT_REVIEW;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_TRANSLATION;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_WORD;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.TABLE_WORD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.TAGS_WORD_KEY;


public class WordProvider extends ContentProvider {
    private static final int ID_WORDS_IN_DICTIONARY = 100;
    private static final int ID_WORDS_ALL_WITH_SELECTION = 101;
    private static final int ID_WORDS_SEARCH = 102;
    private static final int ID_LEARNING_MODE = 200;
    private static final String TAG = WordProvider.class.getName();
    private static UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // all words
        sUriMatcher.addURI(
                Contract.Word.URI_AUTHORITY,
                Contract.Word.URI_PATH_WORDS,
                ID_WORDS_IN_DICTIONARY);
        sUriMatcher.addURI(
                Contract.Word.URI_AUTHORITY,
                Contract.Word.URI_PATH_WORDS + "/LEARNING",
                ID_LEARNING_MODE);
        sUriMatcher.addURI(
                Contract.Word.URI_AUTHORITY,
                Contract.Word.URI_PATH_WORDS + "_ALL",
                ID_WORDS_ALL_WITH_SELECTION);
        sUriMatcher.addURI(
                Contract.Word.URI_AUTHORITY,
                Contract.Word.URI_PATH_WORDS + "/SEARCH/*",
                ID_WORDS_SEARCH);
    }

    private DatabaseHelper mDbHelper;

    public WordProvider() {
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        long wordId = -1;
        switch (sUriMatcher.match(uri)) {
            case ID_WORDS_IN_DICTIONARY: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                db.beginTransaction();
                try {
                    for (int i = 0; i < values.length; i++) {
                        String[] tags = values[i].getAsString(TAGS_WORD_KEY).split("\\s+");
                        values[i].remove(TAGS_WORD_KEY);
                        // insert word
                        wordId = db.insert(TABLE_WORD, null, values[i]);
                        // create tags
                        for (String tag : tags) {
                            if (tag.trim().length() > 0) {
                                ContentValues tagValue = new ContentValues();
                                tagValue.put(COLUMN_TAG, tag);
                                db.insertWithOnConflict(TABLE_TAGS, null, tagValue, SQLiteDatabase.CONFLICT_IGNORE);
                                Cursor cTag = db.query(TABLE_TAGS, null, COLUMN_TAG + "=?", new String[]{tag}, null, null, null);
                                cTag.moveToFirst();
                                int tagId = cTag.getInt(cTag.getColumnIndex(_ID));
                                cTag.close();
                                // connect tag with word
                                ContentValues taggedWord = new ContentValues();
                                taggedWord.put(TAG_ID, tagId);
                                taggedWord.put(WORD_ID, wordId);
                                db.insertWithOnConflict(TABLE_WORD_TAG_RELATIONS, null, taggedWord, SQLiteDatabase.CONFLICT_IGNORE);
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                    getContext().getContentResolver().notifyChange(uri, null);
                } finally {
                    db.endTransaction();
                }
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return (int) wordId;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rows_deleted = 0;
        switch (sUriMatcher.match(uri)) {
            case ID_WORDS_IN_DICTIONARY: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                try {
                    rows_deleted = db.delete(TABLE_WORD, selection, selectionArgs);
                } finally {
                }
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows_deleted;
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId = -1;
        switch (sUriMatcher.match(uri)) {
            case ID_WORDS_IN_DICTIONARY: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                try {
                    rowId = db.insert(TABLE_WORD, null, values);
                } finally {
                    //db.close();
                }
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(uri, Contract.Word.URI_PATH_WORDS + "/" + rowId);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        switch (sUriMatcher.match(uri)) {
            case ID_WORDS_IN_DICTIONARY: {
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                long dictId = DictionaryManager.getDictData(getContext()).dictId;
                cursor = db.query(TABLE_WORD, null, COLUMN_DICTIONARY_ID + "=?", new String[]{Long.toString(dictId)}, null, null, sortOrder);
                break;
            }
            case ID_WORDS_ALL_WITH_SELECTION: {
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                cursor = db.query(TABLE_WORD, null, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case ID_WORDS_SEARCH: {
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                long dictId = DictionaryManager.getDictData(getContext()).dictId;
                String wordEvery = "%" + uri.getLastPathSegment() + "%";
                String wordStart = "%" + uri.getLastPathSegment();
                selection = COLUMN_DICTIONARY_ID + "=? " + " AND (" + COLUMN_WORD + " LIKE ? OR " + COLUMN_TRANSLATION + " LIKE ?)";
                selectionArgs = new String[]{Long.toString(dictId), wordEvery, wordEvery};
                sortOrder = "(CASE WHEN " + COLUMN_WORD + " LIKE \"" + wordStart + "\" THEN 1 ELSE 2 END)";
                cursor = db.query(TABLE_WORD, null, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case ID_LEARNING_MODE: {
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                Set<String> tagIds = WordManager.TagChooser.getSetOfTagId(getContext());
                Set<String> dictIds = DictionaryManager.DictChooser.getSetOfId(getContext());

                String selectWords = buildSqlQuery(dictIds, TABLE_WORD, COLUMN_DICTIONARY_ID);
                String selectTags = buildSqlQuery(tagIds, TABLE_WORD_TAG_RELATIONS, TAG_ID);

                String raw_sql;
                if (tagIds.size() == 0) {
                    raw_sql = selectWords; // no tags, select all words from selected dictionaries
                } else {
                    // select only words with chosen tags
                    raw_sql = "SELECT * FROM ( " + selectTags + " ) AS T JOIN ( " + selectWords + " ) AS W ON T.WORD_ID=W._ID;";
                }
                Log.d(TAG, "query: " + raw_sql);
                cursor = db.rawQuery(raw_sql, new String[]{});
                break;
            }
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private String buildSqlQuery(Set<String> setIds, String table, String column) {
        String selectFrom = "SELECT * FROM " + table + " ";
        if (setIds.size() == 0) {
            return selectFrom;
        } else {
            selectFrom = selectFrom + "WHERE ";
        }
        String where = "";
        int counter = 0;
        for (String idString : setIds) {
            counter++;
            String args = column + "=" + idString + " ";
            if (counter < setIds.size()) {
                args = args + " OR ";
            }
            where = where + args;
        }
        String order = " ORDER BY " + COLUMN_LEVEL + ", " + COLUMN_NEXT_REVIEW;
        return selectFrom + where + order;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int itemsChanged = 0;
        switch (sUriMatcher.match(uri)) {
            case ID_WORDS_IN_DICTIONARY: {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                try {
                    itemsChanged = db.update(TABLE_WORD, values, selection, selectionArgs);
                } finally {
                }
            }
        }
        // communicate change to loader manager
        getContext().getContentResolver().notifyChange(uri, null);
        return itemsChanged;
    }
}
