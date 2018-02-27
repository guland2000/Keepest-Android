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
import android.content.ContextWrapper;
import android.net.Uri;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import is.jacek.markowski.dictionary.keepest.R;

/**
 * Created by jacek on 13.07.17.
 */

public class Cache {

    private static File file;

    public static File cacheOrReadSound(Context context, Uri uri, String word, String langCode) throws InvalidKeySpecException, NoSuchAlgorithmException {

        String toHash = word.toLowerCase() + langCode;
        String hash = hashCode(toHash);
        file = new File(new ContextWrapper(context).getFilesDir().getAbsolutePath() + "/" + hash + ".mp3");
        if (!isSoundCached(hash)) {
            saveSoundFile(uri, file);
        }
        return file;
    }

    public static String hashCode(String text) {
        long h = 0L;
        if (h == 0 && text.length() > 0) {
            for (int i = 0; i < text.length(); i++) {
                h = 31 * h + text.charAt(i);
            }
        }
        return String.valueOf(h);
    }

    private static boolean isSoundCached(String hash) {
        return file.length() > 0;


    }

    private static void saveSoundFile(Uri uri, File file) {
        try {
            System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            URL u = new URL(uri.toString());
            DataInputStream inputStream = new DataInputStream(u.openStream());
            OutputStream outputStream = new FileOutputStream(file.getAbsolutePath());
            Files.copy(inputStream, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static FileFilter mp3FileFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".mp3");
            }
        };
    }

    public static float getCacheTotalSize(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File dir = contextWrapper.getFilesDir();
        int total = 0;
        File[] mp3Files = dir.listFiles(mp3FileFilter());
        System.out.print("");
        for (int i = 0; i < mp3Files.length; i++) {
            total += mp3Files[i].length();
        }
        return total / 1024 / 1024f; // megabytes
    }

    public static void clearCache(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File dir = contextWrapper.getFilesDir();
        File[] mp3Files = dir.listFiles(mp3FileFilter());
        System.out.print("");
        for (int i = 0; i < mp3Files.length; i++) {
            mp3Files[i].delete();
        }
        toastCacheCleared(context);

    }

    public static void toastCacheCleared(Context context) {
        Toast.makeText(context, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
    }
}
