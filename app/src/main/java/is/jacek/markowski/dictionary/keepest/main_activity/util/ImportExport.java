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

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.madrapps.asyncquery.AsyncQueryHandler;
import com.opencsv.CSVWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.database.Contract;
import is.jacek.markowski.dictionary.keepest.main_activity.database.DatabaseHelper;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_FROM;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_NAME;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_TO;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.TABLE_DICTIONARY;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_DICTIONARY_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_FAVOURITE;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_IMAGE;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_LEVEL;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_NEXT_REVIEW;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_NOTES;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_TRANSLATION;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_WORD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.CsvConverter.convertToJson;

/**
 * Created by jacek on 29.06.17.
 */

public class ImportExport {
    public static final String FILE_KEEP = ".keep";
    public static final String FILE_CSV = ".csv";
    public static final String TAGS_WORD_KEY = "tags";
    public static final String JSON_FILENAME = "data.json";
    static final int VERSION = 1;
    // json fields
    static final String VERSION_KEY = "version";
    // json dictionary fields
    static final String DICT_KEY = "name";
    static final String FROM_KEY = "from";
    static final String TO_KEY = "to";
    static final String WORD_ARRAY_KEY = "words";
    static final String DICT_ARRAY_KEY = "entries";
    static final String TAGS_DICT_KEY = "tags";
    // json word fields
    static final String WORD_KEY = "word";
    static final String TRANS_KEY = "trans";
    static final String STAR_KEY = "fav";
    static final String NOTES_KEY = "notes";
    static final String IMAGE_KEY = "image";
    static final String LEVEL_KEY = "level";
    static final String NEXT_REVIEW_KEY = "next_review";
    public static int STORAGE_REQUEST_CODE = 0;
    private static File dir;

    public static void createDirectories() {
        File root = android.os.Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
        root.mkdirs();
        dir = new File(root.getAbsolutePath() + "/Keepest");
        dir.mkdirs();
    }

    public static File getNewDownloadFile(Context context, String fileName) {
        if (fileName == null) {
            fileName = Files.TARGET_GDRIVE_FILENAME;
        }
        return new File(context.getApplicationInfo().dataDir, fileName);
    }

    // cursor words have to be ordered ascending by DictionaryId
    // cursor dicts have to be ordered ascending by ID
    public static void exportToJson(FragmentActivity activity, Cursor cursorWords, Cursor cursorDicts, String filename, int type, String format) {
        ExportJsonTask task = new ExportJsonTask(activity, cursorWords, cursorDicts, filename, type, format);
        task.execute();
    }

    public static void importJson(FragmentActivity activity, String filename, boolean fromCloud) {
        new ImportJsonTask(activity, filename, fromCloud).execute();
    }

    public static File getImportDirectory() {
        createDirectories();
        return dir;
    }

    public static class ExportJsonTask extends AsyncTask<Void, Void, Void> {
        public static final int TYPE_FILE = 0;
        public static final int TYPE_MAIL = 1;
        public static final int TYPE_CLOUD = 2;
        private final String mFormat;
        Cursor mCursorWords;
        Cursor mCursorDicts;
        MainActivity mActivity;
        String mFilename;
        int mType;
        ProgressDialog mProgressDialog;

        ExportJsonTask(FragmentActivity activity, Cursor words, Cursor dicts, String filename, int type, String format) {
            mCursorDicts = dicts;
            mCursorWords = words;
            mActivity = (MainActivity) activity;
            mFilename = filename;
            mType = type;
            mFormat = format;
            if (mFormat.equals(FILE_KEEP)) {
                mFilename += FILE_KEEP;
            } else if (mFormat.equals(FILE_CSV)) {
                mFilename += FILE_CSV;
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();

            if (mType == TYPE_MAIL) {
                // add file as mail attachment
                File file = new File(ImportExport.getImportDirectory(), mFilename);
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_SUBJECT, mActivity.getString(R.string.app_name) + "_" + mFilename);
                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file)); // attachment
                try {
                    mActivity.startActivity(Intent.createChooser(i, mActivity.getString(R.string.export_dictionary)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(mActivity, R.string.there_are_no_email_clients, Toast.LENGTH_SHORT).show();
                }
            } else if (mType == TYPE_CLOUD) {
                Toast.makeText(mActivity, mActivity.getString(R.string.connecting_to_gdrive), Toast.LENGTH_SHORT).show();
                GDriveV3 gDrive = mActivity.mGdriveV3;
                DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
                Date date = new Date();
                gDrive.uploadFile(mActivity.getString(R.string.backup_keep) + dateFormat.format(date).toString() + FILE_KEEP);

            } else if (mType == TYPE_FILE) {


            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(mActivity, "", mActivity.getString(R.string.exporting_dictionaries_progress));
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            JSONObject jsonRoot = new JSONObject();
            JSONArray entries = new JSONArray();
            FileWriter writer = null;
            try {
                writer = new FileWriter(new File(dir, mFilename));
            } catch (IOException e) {
                e.printStackTrace();
            }
            CSVWriter csvWriter = new CSVWriter(writer);

            try {
                jsonRoot.put(VERSION_KEY, VERSION);
                jsonRoot.put(DICT_ARRAY_KEY, entries);

                if (mCursorWords != null && mCursorDicts != null) {
                    mCursorWords.moveToFirst();
                    mCursorDicts.moveToFirst();
                } else {
                    return null;
                }
                int j = 0;
                dictionaries_loop:
                for (int i = 0; i < mCursorDicts.getCount(); i++) {
                    int dictId = mCursorDicts.getInt(mCursorDicts.getColumnIndex(_ID));

                    JSONObject dictObj = new JSONObject();
                    JSONArray words = new JSONArray();

                    String name = mCursorDicts.getString(mCursorDicts.getColumnIndex(COLUMN_DICTIONARY_NAME));
                    String from = mCursorDicts.getString(mCursorDicts.getColumnIndex(COLUMN_DICTIONARY_FROM));
                    String to = mCursorDicts.getString(mCursorDicts.getColumnIndex(COLUMN_DICTIONARY_TO));
                    // todo: load tags to string
                    String dictTags = "";

                    if (mFormat.equals(FILE_KEEP)) {
                        dictObj.put(DICT_KEY, name);
                        dictObj.put(FROM_KEY, from);
                        dictObj.put(TO_KEY, to);
                        dictObj.put(TAGS_DICT_KEY, dictTags);
                        dictObj.put(WORD_ARRAY_KEY, words);
                        entries.put(dictObj);
                    }

                    words_loop:
                    for (; j < mCursorWords.getCount(); j++) {
                        int dictIdOfWord = mCursorWords.getInt(mCursorWords.getColumnIndex(COLUMN_DICTIONARY_ID));
                        int wordId = mCursorWords.getInt(mCursorWords.getColumnIndex(_ID));

                        if (dictId != dictIdOfWord) {
                            break words_loop;
                        }
                        // prepare tags
                        String wordTags = WordManager.Tags.prepareStringWithAllTags(mActivity, wordId);
                        String word = mCursorWords.getString(mCursorWords.getColumnIndex(COLUMN_WORD));
                        String translation = mCursorWords.getString(mCursorWords.getColumnIndex(COLUMN_TRANSLATION));
                        String notes = mCursorWords.getString(mCursorWords.getColumnIndex(COLUMN_NOTES));
                        String image = mCursorWords.getString(mCursorWords.getColumnIndex(COLUMN_IMAGE));
                        int level = mCursorWords.getInt(mCursorWords.getColumnIndex(COLUMN_LEVEL));
                        int nextReview = mCursorWords.getInt(mCursorWords.getColumnIndex(COLUMN_NEXT_REVIEW));
                        int isFavourite = mCursorWords.getInt(mCursorWords.getColumnIndex(COLUMN_FAVOURITE));
                        if (mFormat.equals(FILE_KEEP)) {
                            JSONObject wordObj = new JSONObject();
                            wordObj.put(WORD_KEY, word);
                            wordObj.put(TRANS_KEY, translation);
                            wordObj.put(STAR_KEY, isFavourite);
                            wordObj.put(NOTES_KEY, notes);
                            wordObj.put(TAGS_WORD_KEY, wordTags);
                            wordObj.put(IMAGE_KEY, image);
                            // learning level
                            wordObj.put(LEVEL_KEY, level);
                            wordObj.put(NEXT_REVIEW_KEY, nextReview);
                            words.put(wordObj);
                        } else if (mFormat.equals(FILE_CSV)) {
                            csvWriter.writeNext(new String[]{word, translation, wordTags, notes, image});
                        }
                        mCursorWords.moveToNext();
                    }
                    mCursorDicts.moveToNext();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                mCursorWords.close();
                mCursorDicts.close();
                try {
                    csvWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // add json file to zip archive
            if (mFormat.equals(FILE_KEEP)) {
                Files.saveJsonAsFile(mActivity, JSON_FILENAME, jsonRoot.toString(), mType);
                File importDir = ImportExport.getImportDirectory();
                if (mType == TYPE_CLOUD) {
                    importDir = new File(mActivity.getApplicationInfo().dataDir);
                    mFilename = "google_drive.keep";
                }
                File zipFile = new File(importDir, "zipfile");
                File fileToCompress = new File(importDir, JSON_FILENAME);
                ZipCompression.compress(zipFile, fileToCompress);
                zipFile.renameTo(new File(importDir, mFilename));
                fileToCompress.delete();
            }
            return null;
        }
    }

    private static class ImportJsonTask extends AsyncTask<Void, Void, Void> {
        private MainActivity mActivity;
        private String mFilename;
        private ProgressDialog mProgressDialog;
        private ContentResolver mContentResolver;
        private AsyncQueryHandler mAsyncInsertDicts;
        private AsyncInsertWords mAsyncInsertWords;
        private boolean mFromCloud;

        public ImportJsonTask(FragmentActivity activity, String filename, boolean fromCloud) {
            mFilename = filename;
            mActivity = (MainActivity) activity;
            mFromCloud = fromCloud;
            mContentResolver = mActivity.getContentResolver();
            mProgressDialog = ProgressDialog.show(mActivity, "", mActivity.getString(R.string.importing_dictionaries_progress));
            mAsyncInsertWords = new AsyncInsertWords(mContentResolver, mActivity, mProgressDialog);
            mAsyncInsertDicts = new AsyncInsertDicts(mContentResolver);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            DictionaryManager.openRandomDictionaryIfNotOpened(mActivity);
            Toast.makeText(mActivity, mActivity.getString(R.string.backup_file_downloaded), Toast.LENGTH_LONG).show();
            if (mFromCloud) { // delete temporary file if downloaded from cloud
                File dir = new File(mActivity.getApplicationInfo().dataDir);
                File file = new File(dir, mFilename);
                file.delete();
            }
            mProgressDialog.dismiss();
            mActivity.restoreLastFragment();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            File dir = new File(Files.readCurrentPath(mActivity));
            if (mFromCloud) {
                dir = new File(mActivity.getApplicationInfo().dataDir);
            }
            File fileZipped = new File(dir, mFilename);
            // unzip archive
            String jsonString = "";
            File fileUnzipped = new File(dir, JSON_FILENAME);
            if (mFilename.endsWith(".keep")) {
                ZipCompression.extract(fileZipped, dir.getAbsolutePath());
            } else if (mFilename.endsWith(".csv")) {
                jsonString = convertToJson(new File(dir, mFilename)).toString();
                Log.d("CSV", "doInBackground: " + jsonString);
            }

            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                List<DictObject> dictsList = new ArrayList<>();
                try {
                    if (mFilename.endsWith(".keep")) {
                        FileInputStream f = new FileInputStream(fileUnzipped);
                        byte[] buffer = new byte[f.available()];
                        f.read(buffer);
                        jsonString = new String(buffer, "UTF-8");
                        f.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    JSONObject importFile = new JSONObject(jsonString);
                    // todo check version of file
                    int version = importFile.getInt(VERSION_KEY);
                    JSONArray dicts = importFile.getJSONArray(DICT_ARRAY_KEY);
                    //List<ContentValues> dictValues = new ArrayList<>();
                    List<ContentValues> wordValues = new ArrayList<>();
                    DictObject idObject = new DictObject();

                    dict_loop:
                    for (int i = 0; i < dicts.length(); i++) {
                        JSONObject dict = dicts.getJSONObject(i);
                        String from = dict.getString(FROM_KEY);
                        String to = dict.getString(TO_KEY);
                        String dictName = dict.getString(DICT_KEY);
                        String dictTags = dict.getString(TAGS_DICT_KEY);
                        DictObject dictObj = new DictObject();
                        dictObj.name = dictName;
                        dictObj.from = from;
                        dictObj.to = to;
                        dictsList.add(dictObj);
                        JSONArray words = dict.getJSONArray(WORD_ARRAY_KEY);
                        // create dictionary
                        ContentValues values = new ContentValues();
                        values.put(Contract.Dictionary.Entry.COLUMN_DICTIONARY_NAME, dictName);
                        values.put(COLUMN_DICTIONARY_FROM, from);
                        values.put(COLUMN_DICTIONARY_TO, to);
                        //dictValues.add(values);
                        mAsyncInsertDicts.startInsert(0, idObject, UriHelper.Dictionary.buildDictUri(), values);
                        int counter = 0;
                        int counter_limit = 100;
                        while (!idObject.is_valid) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            counter++;
                            if (counter > counter_limit) {
                                Toast.makeText(mActivity, mActivity.getString(R.string.error_importing_dictionary_timeout), Toast.LENGTH_LONG).show();
                                break dict_loop;
                            }

                        }

                        // insert all words to db
                        for (int j = 0; j < words.length(); j++) {
                            JSONObject item = (JSONObject) words.get(j);
                            String word = item.getString(WORD_KEY);
                            String trans = item.getString(TRANS_KEY);
                            String notes = item.getString(NOTES_KEY);
                            String image = item.optString(IMAGE_KEY);
                            int star = item.getInt(STAR_KEY);
                            int level = item.optInt(LEVEL_KEY);
                            int nextReview = item.optInt(NEXT_REVIEW_KEY);
                            String wordTags = item.getString(TAGS_WORD_KEY);
                            values = new ContentValues();
                            values.put(COLUMN_WORD, word);
                            values.put(COLUMN_TRANSLATION, trans);
                            values.put(COLUMN_FAVOURITE, star);
                            values.put(COLUMN_DICTIONARY_ID, idObject.id);
                            values.put(COLUMN_NOTES, notes);
                            values.put(COLUMN_IMAGE, image);
                            //star
                            values.put(COLUMN_FAVOURITE, star);
                            // learning level
                            values.put(COLUMN_LEVEL, level);
                            values.put(COLUMN_NEXT_REVIEW, nextReview);

                            values.put(TAGS_WORD_KEY, wordTags);
                            wordValues.add(values);
                        }
                        idObject.is_valid = false; // id was used, wait for another one
                    }

                    // bulk insert to db
                    mAsyncInsertWords.startBulkInsert(1, null, UriHelper.Word.buildWordsAllUri(), wordValues.toArray(new ContentValues[wordValues.size()]));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // remove duplicated dictionaries:
                for (DictObject item : dictsList) {
                    removeDuplicatedDictionaries(item);
                }
                // remove orphaned words
                new DatabaseHelper(mActivity).removeOrphans();
            }

            return null;
        }

        private void removeDuplicatedDictionaries(DictObject item) {
            DatabaseHelper dbHelper = new DatabaseHelper(mActivity);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String selection = COLUMN_DICTIONARY_NAME + "=? AND " + COLUMN_DICTIONARY_FROM + "=? AND " + COLUMN_DICTIONARY_TO + "=?";
            String[] selectionArgs = new String[]{item.name, item.from, item.to};
            Cursor cursor = db.query(TABLE_DICTIONARY, new String[]{_ID}, selection, selectionArgs, null, null, _ID + " ASC");
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount() - 1; i++) {
                cursor.move(i);
                String dId = Integer.toString(cursor.getInt(cursor.getColumnIndex(_ID)));
                mContentResolver.delete(UriHelper.Dictionary.buildDictUri(), _ID + "=?", new String[]{dId});
            }
            cursor.close();
        }
    }

    private static class DictObject {
        int id;
        boolean is_valid;
        String name;
        String from;
        String to;
    }


    private static class AsyncInsertDicts extends AsyncQueryHandler {
        public AsyncInsertDicts(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            super.onInsertComplete(token, cookie, uri);
            DictObject idObject = (DictObject) cookie;
            idObject.id = Integer.valueOf(uri.getLastPathSegment());
            idObject.is_valid = true;
        }
    }

    private static class AsyncInsertWords extends AsyncQueryHandler {

        FragmentActivity mActivity;
        ProgressDialog mProgressDialog;

        public AsyncInsertWords(ContentResolver cr, FragmentActivity activity, ProgressDialog dialog) {
            super(cr);
            mActivity = activity;
            mProgressDialog = dialog;
        }

        @Override
        protected void onBulkInsertComplete(int token, Object cookie, int result) {
            super.onBulkInsertComplete(token, cookie, result);
            if (token == 1) {
                mProgressDialog.dismiss();
            }
        }
    }
}
