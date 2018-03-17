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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.jetbrains.annotations.Contract;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Locale;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;


public class Cache {

    private static File file;

    static File cacheOrReadSound(Context context, Uri uri, String word, String langCode) throws InvalidKeySpecException, NoSuchAlgorithmException {

        String toHash = word.toLowerCase() + langCode;
        String hash = hashCode(toHash);
        file = new File(new ContextWrapper(context).getFilesDir().getAbsolutePath() + "/" + hash + ".mp3");
        if (!isSoundCached(hash)) {
            saveSoundFile(context, word, langCode, uri, file);
        }
        return file;
    }

    @NonNull
    private static String hashCode(String text) {
        long h = 0L;
        if (text.length() > 0) {
            for (int i = 0; i < text.length(); i++) {
                h = 31 * h + text.charAt(i);
            }
        }
        return String.valueOf(h);
    }

    private static boolean isSoundCached(String hash) {
        return file.length() > 0;


    }

    private static void saveSoundFile(Context context, String word, String langCode, Uri uri, File file) {
        MainActivity activity = (MainActivity) context;
        TextToSpeech tts = activity.mTts;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        Locale locale = null;
        switch (langCode) {
            case "is": {
                String engine = prefs.getString("pref_tts_icelandic", "google_translate");
                locale = new Locale(engine);
                break;
            }
            case "pl": {
                String engine = prefs.getString("pref_tts_polish", "google_translate");
                locale = new Locale(engine);
                break;
            }
            case "en": {
                String engine = prefs.getString("pref_tts_english", "google_translate");
                if (engine.length() > 2) {
                    String lang = engine.substring(0, 2);
                    String region = engine.substring(3);
                    locale = new Locale(lang, region);
                }
                break;
            }
            case "es": {
                String engine = prefs.getString("pref_tts_spanish", "google_translate");
                if (engine.length() > 2) {
                    String lang = engine.substring(0, 2);
                    String region = engine.substring(3);
                    locale = new Locale(lang, region);
                }
                break;
            }
            case "ro": {
                String engine = prefs.getString("pref_tts_romanian", "google_translate");
                locale = new Locale(engine);
                break;
            }
            case "de": {
                String engine = prefs.getString("pref_tts_german", "google_translate");
                locale = new Locale(engine);
                break;
            }
            case "fr": {
                String engine = prefs.getString("pref_tts_french", "google_translate");
                locale = new Locale(engine);
                break;
            }
            case "it": {
                String engine = prefs.getString("pref_tts_italian", "google_translate");
                locale = new Locale(engine);
                break;
            }
            case "cy": {
                String engine = prefs.getString("pref_tts_welsh", "google_translate");
                locale = new Locale(engine);
                break;
            }
        }

        if (locale != null && tts.setLanguage(locale) >= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.mTts.synthesizeToFile(word, null, file, file.getName());
            } else {
                activity.mTts.synthesizeToFile(word, null, file.getName());
            }
            int timeCounter = 0;
            int sleep = 100;
            while (tts.isSpeaking()) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timeCounter += sleep;
                // exit loop if longer than 2 seconds
                if (timeCounter > 2000) {
                    break;
                }
            }

        } else {
            try {
                System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
                URL u = new URL(uri.toString());
                DataInputStream inputStream = new DataInputStream(u.openStream());
                OutputStream outputStream = new FileOutputStream(file.getAbsolutePath());
                Files.copy(inputStream, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    @Contract(pure = true)
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

    private static void toastCacheCleared(Context context) {
        Toast.makeText(context, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
    }
}
