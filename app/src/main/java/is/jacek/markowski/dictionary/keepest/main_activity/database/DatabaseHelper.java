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

package is.jacek.markowski.dictionary.keepest.main_activity.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_FROM;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_NAME;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_TO;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.TABLE_DICTIONARY;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.COLUMN_TAG;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.DICTIONARY_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TABLE_DICTIONARY_TAGS;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TABLE_DICTIONARY_TAGS_RELATIONS;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TABLE_TAGS;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TABLE_WORD_TAG_RELATIONS;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TAG_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.WORD_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_BAD;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_BAD_IN_ROW;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_DICTIONARY_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_FAVOURITE;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_GOOD;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_GOOD_IN_ROW;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_IMAGE;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_LEVEL;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_NEXT_REVIEW;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_NOTES;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_TRANSLATION;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_WORD;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.TABLE_WORD;

/**
 * Created by jacek on 24.04.17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "keepest.db";
    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // words
        final String sqlWord = "CREATE TABLE IF NOT EXISTS " + TABLE_WORD
                + " ( "
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_WORD + " TEXT NOT NULL, "
                + COLUMN_TRANSLATION + " TEXT NOT NULL, "
                + COLUMN_DICTIONARY_ID + " INTEGER NOT NULL, "
                + COLUMN_FAVOURITE + " INTEGER DEFAULT 0, "
                + COLUMN_NOTES + " TEXT DEFAULT '', "
                + COLUMN_IMAGE + " TEXT DEFAULT '', "
                + COLUMN_LEVEL + " INTEGER DEFAULT 0, "
                + COLUMN_NEXT_REVIEW + " INTEGER DEFAULT 0, "
                + COLUMN_GOOD + " INTEGER DEFAULT 0, "
                + COLUMN_BAD + " INTEGER DEFAULT 0, "
                + COLUMN_GOOD_IN_ROW + " INTEGER DEFAULT 0, "
                + COLUMN_BAD_IN_ROW + " INTEGER DEFAULT 0 "
                + " );";
        db.execSQL(sqlWord);
        // dictionaries
        final String sqlDictionary = "CREATE TABLE IF NOT EXISTS " + TABLE_DICTIONARY
                + " ( "
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DICTIONARY_NAME + " TEXT NOT NULL, "
                + COLUMN_DICTIONARY_FROM + " TEXT NOT NULL, "
                + COLUMN_DICTIONARY_TO + " TEXT NOT NULL "
                + " );";
        db.execSQL(sqlDictionary);
        // tags
        final String table_word_tags = "CREATE TABLE IF NOT EXISTS " + TABLE_TAGS
                + " ( "
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TAG + " TEXT NOT NULL, "
                + "UNIQUE(" + COLUMN_TAG + ")"
                + " );";
        final String table_dictionary_tags = "CREATE TABLE IF NOT EXISTS " + TABLE_DICTIONARY_TAGS
                + " ( "
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TAG + " TEXT NOT NULL, "
                + "UNIQUE(" + COLUMN_TAG + ")"
                + " );";
        final String tag_word = "CREATE TABLE IF NOT EXISTS " + TABLE_WORD_TAG_RELATIONS
                + " ( "
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TAG_ID + " INTEGER, "
                + WORD_ID + " INTEGER, "
                + "UNIQUE(" + TAG_ID + "," + WORD_ID + ")"
                + " );";
        final String tag_dictionary = "CREATE TABLE IF NOT EXISTS " + TABLE_DICTIONARY_TAGS_RELATIONS
                + " ( "
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TAG_ID + " INTEGER,"
                + DICTIONARY_ID + " INTEGER,"
                + "UNIQUE(" + TAG_ID + "," + DICTIONARY_ID + ")"
                + " );";
        db.execSQL(table_word_tags);
        db.execSQL(table_dictionary_tags);
        db.execSQL(tag_word);
        db.execSQL(tag_dictionary);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mContext.deleteDatabase(DB_NAME);
        onCreate(db);
    }

}
