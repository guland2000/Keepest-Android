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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by jacek on 03.05.17.
 */


public class Tts {
    private static MediaPlayer mMediaPlayer;
    private static boolean mIsPlaying = false;
    private Context mContext;
    private boolean mIsConnected;

    public Tts(FragmentActivity context) {
        mContext = context;
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public void onlineTts(final String word, final String wordLang,
                          final String translation, final String translationLang, boolean isConnected) {
        SoundTask task = new SoundTask();
        String[] wordPair = new String[]{word, wordLang};
        mIsConnected = isConnected;
        String[] transPair = new String[]{translation, translationLang};
        task.execute(wordPair, transPair);
        mIsPlaying = true;
    }

    private class SoundTask extends AsyncTask<String[], Integer, Integer> {
        private final String Url = "https://translate.google.com/translate_tts?ie=UTF-8";
        private final String webClient = "&client=tw-ob";
        private boolean mShowError = false;

        @Override
        protected Integer doInBackground(String[]... params) {
            String encodedWord = "";

            for (String[] val : params) {

                String text = val[0];
                if (text.length() == 0) {
                    continue;
                }
                String textLang = val[1];


                try {
                    encodedWord = "&q=" + URLEncoder.encode(text, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String language = "&tl=" + textLang;

                String fullUrl = Url + encodedWord + language + webClient;

                Uri uri = Uri.parse(fullUrl);
                File cache = null;
                try {
                    cache = Cache.cacheOrReadSound(mContext, uri, text, textLang);
                } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                while (mMediaPlayer.isPlaying()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    mMediaPlayer.reset();
                    if (cache.length() > 0) {
                        String path = cache.getAbsolutePath();
                        mMediaPlayer.setDataSource(path);
                        play();
                    } else if (cache == null && mIsConnected) {
                        mMediaPlayer.setDataSource(mContext, uri);
                        play();
                    } else {
                        mShowError = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mIsPlaying = false;
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (mShowError) {
                //Message.makeText(mContext, mContext.getString(R.string.no_internet_connection), Message.LENGTH_LONG).show();
            }
        }

        private void play() throws IOException {
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        }
    }

}
