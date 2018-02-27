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

package is.jacek.markowski.dictionary.keepest.main_activity.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import java.util.Set;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_FROM;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_NAME;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_TO;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_DICTIONARY_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.PREFERENCES_FILE;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper.Dictionary.buildDictUri;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper.Word.buildWordWithSelectionUri;

/**
 * Created by jacek on 26.06.17.
 */

public class DictionaryManager {
    public static final String TAG = "dictionaryManager";
    public static final String DICT_ID = "dict_id";

    public static int getWordCount(Context context, long dictId) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = buildWordWithSelectionUri();
        Cursor cursor = resolver.query(uri, null, COLUMN_DICTIONARY_ID + "=?", new String[]{Long.toString(dictId)}, null);
        int count = 0;
        if (cursor != null) {
            while (cursor.moveToNext()) {
            }
            count = cursor.getCount();
            cursor.close();
        }
        return count;

    }

    public static Dictionary getDictData(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = buildDictUri();
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        long dictID = preferences.getLong(DICT_ID, -1);
        Cursor cursor = resolver.query(uri, null, _ID + "=?", new String[]{Long.toString(dictID)}, null);
        String name = "";
        String from = "";
        String to = "";
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            name = cursor.getString(cursor.getColumnIndex(COLUMN_DICTIONARY_NAME));
            from = cursor.getString(cursor.getColumnIndex(COLUMN_DICTIONARY_FROM));
            to = cursor.getString(cursor.getColumnIndex(COLUMN_DICTIONARY_TO));
            cursor.close();
            return new Dictionary(name, from, to, dictID);
        }
        if (cursor != null) {
            cursor.close();
        }
        return new Dictionary("", "", "", -1);
    }

    public static long getDictId(Context context) {
        return getDictData(context).dictId;
    }

    public static void saveDictData(Context context, Long dictId) {
        android.content.SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
        editor.putLong(DICT_ID, dictId);
        editor.commit();

    }

    public static void openRandomDictionary(Context context) {
        Cursor cursor = context.getContentResolver().query(buildDictUri(), null, null, null, null);
        long dictId;
        if (cursor == null) {
            return;
        }
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            dictId = cursor.getLong(cursor.getColumnIndex(_ID));
            saveDictData(context.getApplicationContext(), dictId);
            Preferences.Dictionary pref = new Preferences.Dictionary();
            pref.setDictionaryAsOpened(context, dictId);
        }
        cursor.close();
    }

    public static void openRandomDictionaryIfNotOpened(Context context) {
        long dictId = getDictData(context).dictId;
        if (dictId == -1) {
            openRandomDictionary(context);
        }
    }

    public static void setDictionaryAsOpened(Context context, long dictId) {
        Preferences.Dictionary pref = new Preferences.Dictionary();
        pref.setDictionaryAsOpened(context, dictId);
    }

    public static boolean isInSetOfOpenedEntries(Context context, long idInDatabase) {
        Preferences.Dictionary pref = new Preferences.Dictionary();
        return pref.isInSetOfOpenedEntries(context, idInDatabase);
    }

    public static void removeFromSetOfOpenedDictionaries(Context context, long idInDatabase) {
        Preferences.Dictionary pref = new Preferences.Dictionary();
        pref.removeFromSetOfOpenedDictionaries(context, idInDatabase);
    }

    /**
     * Created by jacek on 26.06.17.
     */

    public static class Dictionary {
        public long dictId;
        public String name;
        public String speak_from; // country code: pl, en ...
        public String speak_to;

        public Dictionary(String name, String from, String to, long id) {
            this.dictId = id;
            this.name = name;
            speak_from = from;
            speak_to = to;
        }
    }

    public static class DictChooser {
        public static boolean isTagIdInSet(Context context, int idInDatabase) {
            Preferences.DictChooser pref = new Preferences.DictChooser();
            return pref.isIdInSet(context, idInDatabase);
        }

        public static void addOrRemoveTagIdFromSet(Context context, int idInDb) {
            Preferences.DictChooser pref = new Preferences.DictChooser();
            pref.addOrRemoveIdFromSet(context, idInDb);
        }

        public static Set<String> getSetOfId(Context context) {
            Preferences.DictChooser pref = new Preferences.DictChooser();
            return pref.getSetOfId(context);
        }

        public static void resetSet(Context context) {
            Preferences.DictChooser pref = new Preferences.DictChooser();
            pref.resetSet(context);

        }
    }
}
