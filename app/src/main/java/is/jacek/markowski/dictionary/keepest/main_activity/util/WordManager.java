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
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.EditText;

import java.util.HashSet;
import java.util.Set;

import is.jacek.markowski.dictionary.keepest.main_activity.database.Contract;

import static android.provider.UserDictionary.Words._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.COLUMN_TAG;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TAG_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.WORD_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_BAD_IN_ROW;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_DICTIONARY_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_FAVOURITE;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_GOOD_IN_ROW;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_IMAGE;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_LEVEL;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_NEXT_REVIEW;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_NOTES;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_TRANSLATION;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_WORD;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordAdvancedFragment.NOTES_KEY;

/**
 * Created by jacek on 8/5/17.
 */

public class WordManager {
    public static Word getWordById(Context context, long wordId) {
        ContentResolver resolver = context.getContentResolver();
        String selection = _ID + "=?";
        String[] selectionArgs = new String[]{Long.toString(wordId)};
        Cursor c = resolver.query(UriHelper.Word.buildWordWithSelectionUri(), null, selection, selectionArgs, null);
        Word entry = new Word();
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            int favourite = c.getInt(c.getColumnIndex(COLUMN_FAVOURITE));
            String tags = Tags.prepareStringWithAllTags(context, (int) wordId);
            String notes = c.getString(c.getColumnIndex(COLUMN_NOTES));
            String word = c.getString(c.getColumnIndex(COLUMN_WORD));
            String trans = c.getString(c.getColumnIndex(COLUMN_TRANSLATION));
            String imageUrl = c.getString(c.getColumnIndex(COLUMN_IMAGE));
            int level = c.getInt(c.getColumnIndex(COLUMN_LEVEL));
            int nextReview = c.getInt(c.getColumnIndex(COLUMN_NEXT_REVIEW));
            int correctInRow = c.getInt(c.getColumnIndex(COLUMN_GOOD_IN_ROW));
            int wrongInRow = c.getInt(c.getColumnIndex(COLUMN_BAD_IN_ROW));
            entry.id = (int) wordId;
            entry.word = word.trim();
            entry.translation = trans.trim();
            entry.favourite = favourite;
            entry.notes = notes.trim();
            entry.tags = tags.trim();
            entry.imageUrl = imageUrl;
            entry.level = level;
            entry.nextReview = nextReview;
            entry.correctInRow = correctInRow;
            entry.wrongInRow = wrongInRow;

        }
        if (c != null) {
            c.close();
        }
        return entry;
    }

    public static class WordEdit {


        public static Word getWordObjectForSave(Context context, long wordId, EditText wordEdit, EditText translationEdit, boolean advanced) {

            Word entry;
            if (wordId >= 0) {
                entry = WordManager.getWordById(context, wordId);
            } else {
                entry = new Word();
            }

            if (advanced) {
                entry.notes = WordEdit.getTextItem(context, NOTES_KEY).trim();
                entry.imageUrl = WordEdit.getTextItem(context, "image").trim();
            }

            if (wordEdit != null) {
                entry.word = Text.shrinkText(wordEdit.getText().toString());
            }
            if (translationEdit != null) {
                entry.translation = Text.shrinkText(translationEdit.getText().toString());
            }
            return entry;
        }

        public static String getTextItem(Context context, String key) {
            Preferences.WordEdit pref = new Preferences.WordEdit();
            return pref.getTextItem(context, key);
        }

        public static ContentValues prepareContentValues(Context context, WordManager.Word entry) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_WORD, entry.word);
            values.put(COLUMN_TRANSLATION, entry.translation);
            values.put(COLUMN_DICTIONARY_ID, DictionaryManager.getDictData(context).dictId);
            values.put(COLUMN_FAVOURITE, entry.favourite);
            values.put(COLUMN_NOTES, entry.notes);
            values.put(COLUMN_IMAGE, entry.imageUrl);
            values.put(COLUMN_LEVEL, entry.level);
            values.put(COLUMN_NEXT_REVIEW, entry.nextReview);
            values.put(COLUMN_GOOD_IN_ROW, entry.correctInRow);
            values.put(COLUMN_BAD_IN_ROW, entry.wrongInRow);
            return values;
        }

        public static ContentValues prepareContentValuesForUpdate(WordManager.Word entry) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_WORD, entry.word);
            values.put(COLUMN_TRANSLATION, entry.translation);
            values.put(COLUMN_FAVOURITE, entry.favourite);
            values.put(COLUMN_NOTES, entry.notes);
            values.put(COLUMN_IMAGE, entry.imageUrl);
            values.put(COLUMN_LEVEL, entry.level);
            values.put(COLUMN_NEXT_REVIEW, entry.nextReview);
            values.put(COLUMN_GOOD_IN_ROW, entry.correctInRow);
            values.put(COLUMN_BAD_IN_ROW, entry.wrongInRow);
            return values;
        }

        public static void saveTextItem(Context context, String key, String text) {
            Preferences.WordEdit pref = new Preferences.WordEdit();
            pref.saveTextItem(context, key, text);
        }

        public static boolean isEdited(Context context) {
            Preferences.WordEdit pref = new Preferences.WordEdit();
            return pref.isEdited(context);
        }

        public static void resetValues(Context context) {
            Preferences.WordEdit pref = new Preferences.WordEdit();
            pref.resetValues(context);
        }

        public static Set<String> getSetOfTagId(Context context) {
            Preferences.WordEdit pref = new Preferences.WordEdit();
            return pref.getSetOfId(context);
        }

        public static boolean isTagIdInSet(Context context, int idInDb) {
            Preferences.WordEdit pref = new Preferences.WordEdit();
            return pref.isIdInSet(context, idInDb);
        }

        public static void addOrRemoveTagIdFromSet(Context context, int idInDb) {
            Preferences.WordEdit pref = new Preferences.WordEdit();
            pref.addOrRemoveIdFromSet(context, idInDb);
        }

        public static long getId(Context context) {
            Preferences.WordEdit pref = new Preferences.WordEdit();
            return pref.getId(context);
        }

        public static void setId(Context context, long id) {
            Preferences.WordEdit pref = new Preferences.WordEdit();
            pref.setId(context, id);
        }

        public static void setMode(Context context, int mode) {
            Preferences.WordEdit pref = new Preferences.WordEdit();
            pref.setMode(context, mode);
        }

        public static int getMode(Context context) {
            Preferences.WordEdit pref = new Preferences.WordEdit();
            return pref.getMode(context);
        }
    }

    public static class Tags {
        public static void addTagsToWord(Context context, int wordId) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            HashSet<String> tagsToProcess = (HashSet<String>) WordManager.WordEdit.getSetOfTagId(context);
            for (String t : tagsToProcess) {
                int tagId = Integer.valueOf(t);
                values.put(TAG_ID, tagId);
                values.put(Contract.Tag.Entry.WORD_ID, wordId);
                if (isWordAndTagConnected(context, tagId, wordId)) {
                    String selection = TAG_ID + "=? AND " + WORD_ID + "=?";
                    String[] selectionArgs = new String[]{t, Integer.toString(wordId)};
                    resolver.delete(UriHelper.TagsWord.buildDeleteTagFromWordUri(), selection, selectionArgs);
                } else {
                    resolver.insert(UriHelper.TagsWord.buildAddTagToWordUri(), values);
                }
            }
        }

        private static boolean isWordAndTagConnected(Context context, int tagId, int wordId) {
            ContentResolver resolver = context.getContentResolver();
            String selection = TAG_ID + "=? AND " + WORD_ID + "=?";
            String[] selectionArgs = new String[]{Integer.toString(tagId), Integer.toString(wordId)};
            Cursor c = resolver.query(UriHelper.TagsWord.buildTagsForWords(), null, selection, selectionArgs, null);
            return c != null && c.getCount() > 0;
        }

        public static String prepareStringWithAllTags(Context context, int wordId) {
            ContentResolver resolver = context.getContentResolver();
            Cursor c = resolver.query(UriHelper.TagsWord.buildTagsWordUri(wordId), null, null, new String[]{""}, null);
            StringBuilder tags = new StringBuilder();
            if (c != null && c.getCount() > 0) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    if (!c.isNull(c.getColumnIndex(WORD_ID))) {
                        String tagName = c.getString(c.getColumnIndex(COLUMN_TAG));
                        tags.append(tagName);
                        tags.append(" ");
                    }
                }
                c.close();
            }
            if (c != null) {
                c.close();
            }
            return tags.toString();
        }
    }

    public static class Word {
        public int id = -1;
        private final int MAX_LEVEL = 20;
        private final int MIN_LEVEL = 0;
        static final int LEVEL_UP_LIMIT = 1;
        static final int LEVEL_DOWN_LIMIT = 1;
        public String word = "";
        public String translation = "";
        public int favourite = 0; // 0-1
        public String tags = "";
        public String notes = "";
        public String imageUrl = "";
        public int nextReview = 0;
        public int level = 0;
        public int correctInRow = 0;
        public int wrongInRow = 0;

        void increaseLevel() {
            level++;
            if (level > MAX_LEVEL) {
                level = MAX_LEVEL;
            }
            correctInRow = 0;
            wrongInRow = 0;
            nextReview = getHoursForReview() + (int) ((Math.pow(level, 3)));
        }

        void decreaseLevel() {
            level--;
            if (level < MIN_LEVEL) {
                level = MIN_LEVEL;
            }
            correctInRow = 0;
            wrongInRow = 0;
            nextReview = getHoursForReview();
        }

        public void increaseCorrect() {
            correctInRow++;
            wrongInRow = 0;
            if (correctInRow >= LEVEL_UP_LIMIT) {
                increaseLevel();
            }
        }

        int getHoursForReview() {
            return (int) (System.currentTimeMillis() / 1000L / 60 / 60);
        }

        public void increaseWrong() {
            wrongInRow++;
            correctInRow = 0;
            if (wrongInRow >= LEVEL_DOWN_LIMIT) {
                decreaseLevel();
            }
        }

        public static void saveIdOfLastAddedWord(Context context, int wordId) {
            Preferences.Word pref = new Preferences.Word();
            pref.saveIdOfWord(context, wordId, Preferences.Word.LAST_WORD_ID_KEY);
        }

        public static long getIdOfLastAddedWord(Context context) {
            Preferences.Word pref = new Preferences.Word();
            return pref.getIdOfWord(context, Preferences.Word.LAST_WORD_ID_KEY);
        }

        public static void clearIdOfLastAddedWord(Context context) {
            Preferences.Word pref = new Preferences.Word();
            pref.clearIdOfWord(context, Preferences.Word.LAST_WORD_ID_KEY);
        }

        public static void saveIdOfWordToPaste(Context context, int wordId) {
            Preferences.Word pref = new Preferences.Word();
            pref.saveIdOfWord(context, wordId, Preferences.Word.WORD_TO_PASTE_ID_KEY);
        }

        public static void setWordOperationType(Context context, String type) {
            Preferences.Word pref = new Preferences.Word();
            pref.setWordOperationType(context, type);
        }

        public static String getWordOperationType(Context context) {
            Preferences.Word pref = new Preferences.Word();
            return pref.getWordOperationType(context);
        }

        public static long getIdOfWordToPaste(Context context) {
            Preferences.Word pref = new Preferences.Word();
            return pref.getIdOfWord(context, Preferences.Word.WORD_TO_PASTE_ID_KEY);
        }

        public static void clearIdOfWordToPaste(Context context) {
            Preferences.Word pref = new Preferences.Word();
            pref.clearIdOfWord(context, Preferences.Word.WORD_TO_PASTE_ID_KEY);
        }


    }

    public static class TagChooser {
        public static boolean isTagIdInSet(Context context, int idInDb) {
            Preferences.TagChooser pref = new Preferences.TagChooser();
            return pref.isIdInSet(context, idInDb);
        }

        public static void addOrRemoveTagIdFromSet(Context context, int idInDb) {
            Preferences.TagChooser pref = new Preferences.TagChooser();
            pref.addOrRemoveIdFromSet(context, idInDb);
        }

        public static Set<String> getSetOfTagId(Context context) {
            Preferences.TagChooser pref = new Preferences.TagChooser();
            return pref.getSetOfId(context);
        }

        public static void resetSet(Context context) {
            Preferences.TagChooser pref = new Preferences.TagChooser();
            pref.resetSet(context);
        }
    }
}
