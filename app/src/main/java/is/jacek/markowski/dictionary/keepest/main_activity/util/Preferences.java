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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;

import java.util.HashSet;
import java.util.Set;

import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordAdvancedFragment.NOTES_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.ADD_MODE;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.IMAGE_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.MODE_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.TRANSLATION_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.WORD_ID_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.WORD_KEY;

/**
 * Created by jacek on 21.07.17.
 */

public class Preferences {
    public static final String PREFERENCES_FILE = "is.jacek.markowski.dictionary.keepest";
    public static final String GIF_SHOW = "gifShow";
    private static final String READ_LANG_ONE = "readLangOne";
    private static final String READ_LANG_TWO = "readLangTwo";

    public static void setShowGif(Context context, boolean isShow) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(GIF_SHOW, isShow);
        editor.apply();
    }

    public static boolean isShowGif(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return pref.getBoolean(GIF_SHOW, true);
    }

    public static void setReadLangOne(Context context, boolean isReadAnswers) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(READ_LANG_ONE, isReadAnswers);
        editor.apply();
    }

    public static void setReadLangTwo(Context context, boolean isReadAnswers) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(READ_LANG_TWO, isReadAnswers);
        editor.apply();
    }

    public static boolean isReadLangOne(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return pref.getBoolean(READ_LANG_ONE, true);
    }

    public static boolean isReadLangTwo(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return pref.getBoolean(READ_LANG_TWO, true);
    }

    public static class Dictionary {

        public String preferencesFile = PREFERENCES_FILE + "dictionary";
        private String setKey = "set_of_new"; // imported but not opened yet

        // set imported dictionary as new
        public void removeFromSetOfOpenedDictionaries(Context context, long dictId) {
            Set<String> stringSet = getSetOfOpenedEntries(context);
            // add current id to set
            Set<String> newSet = new ArraySet<>();
            newSet.addAll(stringSet);
            newSet.remove(Long.toString(dictId));
            // save set in pref
            saveSetOfOpenedEntriesInPref(context, newSet);
        }

        @NonNull
        private Set<String> getSetOfOpenedEntries(Context context) {
            SharedPreferences pref = context.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
            return pref.getStringSet(setKey, new ArraySet<String>());
        }

        private void saveSetOfOpenedEntriesInPref(Context context, Set<String> stringSet) {
            SharedPreferences.Editor editor = context.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE).edit();
            editor.putStringSet(setKey, stringSet);
            editor.commit();
        }

        // check if dictionary is imported but not opened yet
        public boolean isInSetOfOpenedEntries(Context context, long dictId) {
            Set<String> stringSet = getSetOfOpenedEntries(context);
            return stringSet.contains(Long.toString(dictId));
        }

        // add dictionary to SetOfNewEntries
        public void setDictionaryAsOpened(Context context, long dictId) {
            Set<String> stringSet = getSetOfOpenedEntries(context);
            // add current id to set
            Set<String> newSet = new ArraySet<>();
            newSet.addAll(stringSet);
            newSet.add(Long.toString(dictId));
            // save set in pref
            saveSetOfOpenedEntriesInPref(context, newSet);
        }
    }

    public static class Word {

        public static final String LAST_WORD_ID_KEY = "last_word_id";
        public static final String WORD_TO_PASTE_ID_KEY = "paste_word_id";
        public static final String OPERATION_TYPE_KEY = "operation_type";
        public static final String COPY_WORD = "copy";
        public static final String CUT_WORD = "cut";
        public static final String EMPTY = "empty";

        public void saveIdOfWord(Context context, int wordId, String key) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
            editor.putInt(key, wordId);
            editor.apply();
        }

        public void clearIdOfWord(Context context, String key) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
            editor.putInt(key, 0);
            editor.apply();
        }

        public int getIdOfWord(Context context, String key) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
            return pref.getInt(key, 0);

        }

        public void setWordOperationType(Context context, String type) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
            editor.putString(OPERATION_TYPE_KEY, type);
            editor.apply();
        }

        public String getWordOperationType(Context context) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
            return pref.getString(OPERATION_TYPE_KEY, EMPTY);
        }
    }

    public static class NightMode {
        private static final String NIGHT_MODE = "nightMode";

        public static void setNightMode(Context context, boolean isNight) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
            editor.putBoolean(NIGHT_MODE, isNight);
            editor.apply();
        }

        public static boolean isNightMode(Context context) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
            return pref.getBoolean(NIGHT_MODE, false);
        }
    }

    public static class WordEdit extends PrefStringSet {
        public static final String EDITED_KEY = "isEdited";
        public static final String PREFERENCES_FILE = "wordEdit";
        public static final String SET_NAME = "setWord";

        public WordEdit() {
            super(SET_NAME, PREFERENCES_FILE);
        }

        public void resetValues(Context context) {
            SharedPreferences.Editor editor = context.getSharedPreferences(preferences_file, Context.MODE_PRIVATE).edit();
            editor.putStringSet(set_name, new HashSet<String>());
            editor.putString(WORD_KEY, "");
            editor.putString(TRANSLATION_KEY, "");
            editor.putBoolean(EDITED_KEY, false);
            editor.putString(IMAGE_KEY, "");
            editor.putLong(WORD_ID_KEY, 0);
            editor.putString(NOTES_KEY, "");
            editor.apply();
        }

        public void saveTextItem(Context context, String key, String text) {
            SharedPreferences.Editor editor = null;
            if (context != null) {
                editor = context.getSharedPreferences(preferences_file, Context.MODE_PRIVATE).edit();
            }
            if (editor != null) {
                editor.putString(key, text);
                editor.putBoolean(EDITED_KEY, true);
                editor.apply();
            }
        }

        public String getTextItem(Context context, String key) {
            SharedPreferences pref = context.getSharedPreferences(preferences_file, Context.MODE_PRIVATE);
            return pref.getString(key, "");
        }

        public boolean isEdited(Context context) {
            SharedPreferences pref = context.getSharedPreferences(preferences_file, Context.MODE_PRIVATE);
            return pref.getBoolean(EDITED_KEY, false);
        }

        public long getId(Context context) {
            SharedPreferences pref = context.getSharedPreferences(preferences_file, Context.MODE_PRIVATE);
            return pref.getLong(WORD_ID_KEY, 0);
        }

        public void setId(Context context, long id) {
            SharedPreferences pref = context.getSharedPreferences(preferences_file, Context.MODE_PRIVATE);
            pref.edit().putLong(WORD_ID_KEY, id).apply();
        }

        public int getMode(Context context) {
            SharedPreferences pref = context.getSharedPreferences(preferences_file, Context.MODE_PRIVATE);
            return pref.getInt(MODE_KEY, ADD_MODE);
        }

        public void setMode(Context context, int mode) {
            SharedPreferences pref = context.getSharedPreferences(preferences_file, Context.MODE_PRIVATE);
            pref.edit().putInt(MODE_KEY, mode).apply();

        }
    }

    public static class DictChooser extends PrefStringSet {
        public static final String PREFERENCES_FILE = "dictEdit";
        public static final String SET_NAME = "setDict";

        public DictChooser() {
            super(SET_NAME, PREFERENCES_FILE);
        }
    }

    public static class TagChooser extends PrefStringSet {
        public static final String PREFERENCES_FILE = "tagChooser";
        public static final String SET_NAME = "setDict";

        public TagChooser() {
            super(SET_NAME, PREFERENCES_FILE);
        }
    }

    private abstract static class PrefStringSet {
        String set_name = "set";
        String preferences_file = "preferences";

        PrefStringSet(String setName, String preferencesFile) {
            set_name = setName;
            preferences_file = preferencesFile;
        }

        // which tags add or remove from word after save button is pressed
        public void addOrRemoveIdFromSet(Context context, int id) {
            SharedPreferences.Editor editor = context.getSharedPreferences(preferences_file, Context.MODE_PRIVATE).edit();
            Set<String> stringSet = getSetOfId(context);
            Set<String> newSet = new ArraySet<>();
            newSet.addAll(stringSet);
            // add or remove current id to set
            if (isIdInSet(context, id)) {
                newSet.remove(Integer.toString(id));
            } else {
                newSet.add(Integer.toString(id));
            }
            // save set in pref
            editor.putStringSet(set_name, newSet);
            editor.apply();
        }

        public Set<String> getSetOfId(Context context) {
            SharedPreferences pref = context.getSharedPreferences(preferences_file, Context.MODE_PRIVATE);
            return pref.getStringSet(set_name, new HashSet<String>());
        }

        public boolean isIdInSet(Context context, int id) {
            Set<String> stringSet = getSetOfId(context);
            return stringSet.contains(Integer.toString(id));
        }

        public void resetSet(Context context) {
            SharedPreferences.Editor editor = context.getSharedPreferences(preferences_file, Context.MODE_PRIVATE).edit();
            editor.putStringSet(set_name, new ArraySet<String>());
            editor.apply();

        }

    }

    public static class LearningSummary {
        static final String PREFERENCES_FILE = Preferences.PREFERENCES_FILE + "learning";
        static final String SET_NAME = "name";

        public static void resetSet(Context context) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
            editor.putStringSet(SET_NAME, new ArraySet<String>());
            editor.apply();
        }

        public static void addIdToSet(Context context, int id) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
            Set<String> stringSet = getSetOfId(context);
            Set<String> newSet = new ArraySet<>();
            newSet.addAll(stringSet);

            if (!isIdInSet(context, id)) {
                newSet.add(Integer.toString(id));
            }

            editor.putStringSet(SET_NAME, newSet);
            editor.apply();
        }

        public static Set<String> getSetOfId(Context context) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
            return pref.getStringSet(SET_NAME, new HashSet<String>());
        }

        static boolean isIdInSet(Context context, int id) {
            Set<String> stringSet = getSetOfId(context);
            return stringSet.contains(Integer.toString(id));
        }
    }

    public static class TextToSpeech {
        static final String PREFERENCES_FILE = Preferences.PREFERENCES_FILE + "tts";
        static final String SET_NAME = "tts_settings";
        public static final String ENGINE_ONE = "one";
        public static final String ENGINE_TWO = "two";
        public static final String ENGINE_DEFAULT = "default";


        public static void resetTtsSettings(Context context) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
            editor.putStringSet(SET_NAME, new ArraySet<String>());
            editor.apply();
        }

        public static void addOrRemoveTtsSettings(Context context, String value) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
            Set<String> stringSet = getSetOfTtsSettings(context);
            Set<String> newSet = new ArraySet<>();
            newSet.addAll(stringSet);

            if (!isIdInSet(context, value)) {
                newSet.add(value);
            } else {
                newSet.remove(value);
            }

            editor.putStringSet(SET_NAME, newSet);
            editor.commit();
        }

        public static Set<String> getSetOfTtsSettings(Context context) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
            return pref.getStringSet(SET_NAME, new HashSet<String>());
        }

        public static boolean isIdInSet(Context context, String value) {
            Set<String> stringSet = getSetOfTtsSettings(context);
            return stringSet.contains(value);
        }

        public static void write(Context context, String key, String value) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
            editor.putString(key, value);
            editor.apply();
        }

        public static String read(Context context, String key) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
            return pref.getString(key, "");
        }
    }

}
