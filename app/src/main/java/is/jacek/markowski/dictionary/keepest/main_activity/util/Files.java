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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import is.jacek.markowski.dictionary.keepest.main_activity.database.DatabaseHelper;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_DICTIONARY_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.ExportJsonTask.TYPE_CLOUD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.ExportJsonTask.TYPE_MAIL;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.FILE_KEEP;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.PREFERENCES_FILE;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper.Word.buildWordWithSelectionUri;

/**
 * Created by jacek on 04.07.17.
 */

public class Files {
    private static final String IMPORT_PATH = "import path";
    public static String TARGET_GDRIVE_FILENAME = "google_drive.keep";

    public static File[] listFiles(File dir) {
        return dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(FILE_KEEP)
                        || pathname.getName().endsWith(ImportExport.FILE_CSV);
            }
        });
    }

    public static File[] listDirs(File dir) {
        return dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && !pathname.getName().startsWith(".");
            }
        });
    }

    public static void saveCurrentPath(Context context, String path) {
        android.content.SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
        editor.putString(IMPORT_PATH, path);
        editor.commit();
    }


    public static String readCurrentPath(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return preferences.getString(IMPORT_PATH, ImportExport.getImportDirectory().getAbsolutePath());
    }

    @Nullable
    public static File saveJsonAsFile(Context context, String filename, String json, int type) {
        String state = Environment.getExternalStorageState();
        File dir;
        if (type == TYPE_MAIL) {
            dir = ImportExport.getImportDirectory();
        } else if (type == TYPE_CLOUD) {
            dir = new File(context.getApplicationInfo().dataDir);
        } else {
            dir = new File(readCurrentPath(context));
        }


        File file = new File(dir, filename);

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            try {
                FileOutputStream f = new FileOutputStream(file);
                f.write(json.getBytes(), 0, json.getBytes().length);
                f.flush();
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }
        return file;
    }

    public static void prepareJsonAll(Activity activity, String filename, int type) {
        ContentResolver resolver = activity.getContentResolver();
        new DatabaseHelper(activity).removeOrphans();
        Cursor wordsCursor = resolver.query(buildWordWithSelectionUri(), null, null, null, COLUMN_DICTIONARY_ID + " ASC");
        Cursor dictCursor = resolver.query(UriHelper.Dictionary.buildDictUri(), null, null, null, _ID + " ASC");
        ImportExport.exportToJson((FragmentActivity) activity, wordsCursor, dictCursor, filename, type, FILE_KEEP);
    }

    public static void prepareJsonOne(Activity activity, int dict_id, String filename, int type, String format) {
        ContentResolver resolver = activity.getContentResolver();
        new DatabaseHelper(activity).removeOrphans();
        String whereDict = _ID + "=?";
        String whereWord = COLUMN_DICTIONARY_ID + "=?";
        String[] whereArgs = new String[]{Integer.toString(dict_id)};

        Cursor wordsCursor = resolver.query(buildWordWithSelectionUri(), null, whereWord, whereArgs, COLUMN_DICTIONARY_ID + " ASC");
        Cursor dictCursor = resolver.query(UriHelper.Dictionary.buildDictUri(), null, whereDict, whereArgs, _ID + " ASC");
        ImportExport.exportToJson((FragmentActivity) activity, wordsCursor, dictCursor, filename, type, format);
    }

    public static long copy(InputStream source, OutputStream sink)
            throws IOException {
        long nread = 0L;
        byte[] buf = new byte[10240];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }
}
