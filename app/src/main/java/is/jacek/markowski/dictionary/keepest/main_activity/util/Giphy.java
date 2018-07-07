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

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.GifChooserAdapter;

/**
 * Created by jacek on 10/9/17.
 */

public class Giphy {

    public static void setHorizontalLogo(String url, ImageView view) {
        if (url.contains("giphy")) {
            view.setImageResource(R.drawable.static_logo);
        } else {
            view.setImageResource(R.drawable.static_logo_google);
        }
    }

    public static void setVerticalLogo(String url, ImageView view) {
        if (url.contains("giphy")) {
            view.setImageResource(R.drawable.static_logo_2);
        } else {
            view.setImageResource(R.drawable.static_logo_google_2);
        }
    }
    public static void displayGif(FragmentActivity activity, String source, ImageView destination) {
        if (destination != null && source != null) {
            if (source.contains("giphy")) {
                GlideApp.with(activity)
                        .asGif()
                        .load(source)
                        .placeholder(R.drawable.ic_timer)
                        .error(R.drawable.ic_cancel)
                        .centerCrop()
                        .into(destination);
            } else {
                GlideApp.with(activity)
                        .load(source)
                        .placeholder(R.drawable.ic_timer)
                        .error(R.drawable.ic_cancel)
                        .centerInside()
                        .into(destination);
            }
        }
    }

    public static void queryGifs(String apiKey, GifChooserAdapter adapter, String searchTerm, int limit, int offset) {
        new QueryGifsTask(apiKey, adapter, searchTerm, limit, offset).execute();
    }

    private static ArrayList<String> getJsonSearch(String giphyApi, String searchTerm, int limit, int offset) {
        String url = "https://api.giphy.com/v1/gifs/search?" +
                "api_key=" + giphyApi +
                "&q=" + searchTerm.replace(" ", "+") +
                "&limit=" + limit +
                "&offset=" + offset +
                "&lang=en";
        ArrayList<String> urlListForAdapter = new ArrayList<>();
        try {
            JSONArray gifArray = getJson(url).getJSONArray("data");
            for (int i = 0; i < gifArray.length(); i++) {
                JSONObject gif = gifArray.getJSONObject(i);
                String gifUrl = gif.getJSONObject("images").getJSONObject("fixed_width").getString("url");
                urlListForAdapter.add(gifUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return urlListForAdapter;
    }

    private static JSONObject getJson(String url) {
        try {
            return new JSONObject(readUrl(url));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                reader.close();
        }
        return null;
    }

    private static class QueryGifsTask extends AsyncTask<Void, Void, ArrayList<String>> {
        GifChooserAdapter mGifChooserAdapter;
        String mSearchTerm;
        int mLimit;
        int mOffset;
        private String mGiphyAPI;

        public QueryGifsTask(String apiKey, GifChooserAdapter adapter, String searchTerm, int limit, int offset) {
            mGifChooserAdapter = adapter;
            this.mSearchTerm = searchTerm;
            this.mLimit = limit;
            this.mOffset = offset;
            mGiphyAPI = apiKey;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            mGifChooserAdapter.swapData(strings);
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            return getJsonSearch(mGiphyAPI, mSearchTerm, mLimit, mOffset);
        }
    }
}
