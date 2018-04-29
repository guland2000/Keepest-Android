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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import is.jacek.markowski.dictionary.keepest.main_activity.adapter.DictChooserAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.DictionaryAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.TagAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.TagChooserAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.WordAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.database.Contract;

import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_FAVOURITE;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_WORD;

/**
 * Created by jacek on 8/10/17.
 */

public class Loaders {
    public static class Words {
        public static final int LOADER_ID = 0;
        public static final String SORT_BY_NAMES = "SORT_BY_NAMES";
        public static final String SORT_BY_STARS = "SORT_BY_STARS";


        public static class LoadAllWords implements LoaderManager.LoaderCallbacks<Cursor> {
            private static String mSortOrder;
            private Context mContext;
            private Uri mUri;
            private WordAdapter mAdapter;
            private RecyclerView mRecyclerView;

            public LoadAllWords(Context ctx,
                                RecyclerView recyclerView,
                                Uri uri,
                                String sortOrder) {
                mContext = ctx;
                mUri = uri;
                mAdapter = (WordAdapter) recyclerView.getAdapter();
                mRecyclerView = recyclerView;
                mSortOrder = sortOrder;
            }

            public static String getSortOrder() {
                if (mSortOrder != null) {
                    return mSortOrder;
                } else {
                    return SORT_BY_NAMES;
                }
            }

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String sortOrder;
                String sortAlphabetically = " COLLATE UNICODE ASC";
                if (mSortOrder != null && mSortOrder.equals(SORT_BY_NAMES)) {
                    sortOrder = COLUMN_WORD + sortAlphabetically;
                } else {
                    sortOrder = COLUMN_FAVOURITE + " DESC," + COLUMN_WORD + sortAlphabetically;
                }
                Log.d("___", "onCreateLoader: " + sortOrder);
                return new CursorLoader(mContext, mUri, null, null, null, sortOrder);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mAdapter.swapData(data);
                mAdapter.notifyDataSetChanged();
                // set recycler view position
                int position = mAdapter.findPositionOfWord(WordManager.Word.getIdOfLastAddedWord(mContext));
                mRecyclerView.getLayoutManager().scrollToPosition(position);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.swapData(null);
            }
        }
    }

    public static class Dictionary {

        public static final int LOADER_ID = 1;

        public static class LoadAllDictionaries implements LoaderManager.LoaderCallbacks<Cursor> {
            private Context mContext;
            private Uri mUri;
            private DictionaryAdapter mAdapter;

            public LoadAllDictionaries(Context ctx,
                                       RecyclerView recyclerView,
                                       Uri uri) {
                mContext = ctx;
                mUri = uri;
                mAdapter = (DictionaryAdapter) recyclerView.getAdapter();
            }

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(mContext, mUri, null, null, null, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mAdapter.swapData(data);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.swapData(null);
            }
        }

        public static class LoadAllSimilarDictionaries implements LoaderManager.LoaderCallbacks<Cursor> {
            private Context mContext;
            private DictChooserAdapter mAdapter;

            public LoadAllSimilarDictionaries(Context ctx,
                                              RecyclerView recyclerView) {
                mContext = ctx;
                mAdapter = (DictChooserAdapter) recyclerView.getAdapter();
            }

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri uri = UriHelper.Dictionary.buildDictUri();
                DictionaryManager.Dictionary dictionary = DictionaryManager.getDictData(mContext);
                String selection = Contract.Dictionary.Entry.COLUMN_DICTIONARY_FROM + "=? AND "
                        + Contract.Dictionary.Entry.COLUMN_DICTIONARY_TO + "=?";
                String[] selectionArgs = {dictionary.speak_from, dictionary.speak_to};
                return new CursorLoader(mContext, uri, null, selection, selectionArgs, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mAdapter.swapData(data);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.swapData(null);
            }
        }
    }

    public static class Tags {
        public static final int LOADER_ID = 2;
        public static final String SEARCH_KEY = "searchKey";


        public static class LoadAllTags implements LoaderManager.LoaderCallbacks<Cursor> {
            private Context mContext;
            private TagAdapter mAdapter;
            private int mWordId;

            public LoadAllTags(Context ctx,
                               RecyclerView recyclerView,
                               int wordId) {
                mContext = ctx;
                mAdapter = (TagAdapter) recyclerView.getAdapter();
                mWordId = wordId;
            }

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String searchTag = "";
                if (args != null) {
                    searchTag = args.getString(SEARCH_KEY, "");
                }
                Uri uri = UriHelper.TagsWord.buildTagsWordUri(mWordId);
                return new CursorLoader(mContext, uri, null, null, new String[]{searchTag}, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mAdapter.swapData(data);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.swapData(null);
            }
        }

        public static class TagChooserLoader implements LoaderManager.LoaderCallbacks<Cursor> {
            private Context mContext;
            private TagChooserAdapter mAdapter;

            public TagChooserLoader(Context ctx,
                                    RecyclerView recyclerView) {
                mContext = ctx;
                mAdapter = (TagChooserAdapter) recyclerView.getAdapter();
            }

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String searchTag = "";
                if (args != null) {
                    searchTag = args.getString(SEARCH_KEY, "");
                }
                Uri uri = UriHelper.TagsWord.buildTagsWordUri(0); // 0 for no word
                return new CursorLoader(mContext, uri, null, null, new String[]{searchTag}, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mAdapter.swapData(data);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.swapData(null);
            }
        }
    }
}
