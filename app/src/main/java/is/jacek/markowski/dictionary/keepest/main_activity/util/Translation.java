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
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;

/**
 * Created by jacek on 03.07.17.
 */

public class Translation {
    public static void translate(String from, String to, String text, EditText edTrans, ImageButton imgTrans,
                                 ProgressBar progressBar, MainActivity activity) {
        Context context = edTrans.getContext();
        activity.hideKeyboard();
        if (Connection.isConnected(context)) {
            new TranslationTask(edTrans, imgTrans, progressBar, null).execute(from, to, text);
            if (imgTrans != null && edTrans != null) {
                EditText ed = activity.findViewById(R.id.ed_search_gif);
                ImageButton bt = activity.findViewById(R.id.bt_gif_search);
                new TranslationTask(ed, imgTrans, progressBar, bt).execute(from, "en", text); // translate to english - giphy search
            }
        } else {
            Connection.toastNoConnection(context);
        }
    }

    private static class TranslationTask extends AsyncTask<String, Integer, String> {
        private final String URL_String = "https://translate.googleapis.com/translate_a/single?";
        private final String CLIENT_KEY = "client=gtx";
        private final String SOURCE_LANG_KEY = "&sl=";
        private final String TARGET_LANG_KEY = "&tl=";
        private final String QUERY_KEY = "&dt=t&q=";

        private EditText mEdTrans;
        private ImageButton mImageButton;
        private ProgressBar mProgressBar;
        private ImageButton mGiphySearchBt;

        TranslationTask(EditText editText, ImageButton imgButton, ProgressBar pb, ImageButton giphySearchBt) {
            mEdTrans = editText;
            mImageButton = imgButton;
            mProgressBar = pb;
            mGiphySearchBt = giphySearchBt;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (mEdTrans != null) {
                mEdTrans.setText(s);
            }
            if (mProgressBar != null) {
                mImageButton.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                if (mGiphySearchBt != null) {
                    mGiphySearchBt.callOnClick();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mImageButton != null) {
                mImageButton.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected String doInBackground(String... params) {

            publishProgress(1);
            String from = params[0];
            String to = params[1];
            String text = "";
            try {
                text = URLEncoder.encode(params[2], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            text = Text.shrinkText(text);


            String fullUrl = URL_String + CLIENT_KEY + SOURCE_LANG_KEY + from
                    + TARGET_LANG_KEY + to
                    + QUERY_KEY + text;

            URL url = null;
            String translated = "";
            try {
                url = new URL(fullUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
                URLConnection cc = url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(cc.getInputStream()));
                JSONArray jsonArray = new JSONArray(br.readLine());
                br.close();
                translated = jsonArray.getJSONArray(0).getJSONArray(0).getString(0);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return translated;
        }
    }
}
