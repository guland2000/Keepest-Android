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

package is.jacek.markowski.dictionary.keepest.main_activity.fragment;


import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.WordAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Tts;
import is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper;

import static is.jacek.markowski.dictionary.keepest.main_activity.util.Loaders.Words.LOADER_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Loaders.Words.LoadAllWords;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Loaders.Words.SORT_BY_NAMES;


public class WordFragment extends Fragment {
    public static final String TAG = "WORDS_FRAGMENT";
    public RecyclerView mRecyclerView;
    public Tts ttsManager;
    public String sortMode = SORT_BY_NAMES;
    public FloatingActionButton mFloatingActionButton;
    private Toolbar toolbarFragment;
    private MainActivity mActivity;

    public WordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ttsManager = new Tts(getActivity()); // initialize text to speech service
        setHasOptionsMenu(true);

        final View root = inflater.inflate(R.layout.fragment_words, container, false);
        mFloatingActionButton = root.findViewById(R.id.fab_add);
        mRecyclerView = root.findViewById(R.id.rv_words);
        WordAdapter adapter = new WordAdapter(null, getActivity(), sortMode);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Uri uri = UriHelper.Word.buildWordsAllUri();
        // communicates with content provider to update recycler view
        sortMode = LoadAllWords.getSortOrder();
        getLoaderManager().restartLoader(LOADER_ID, null,
                new LoadAllWords(getContext(), mRecyclerView, uri, sortMode));
        toolbarFragment = getActivity().findViewById(R.id.toolbar);
        mActivity = (MainActivity) getActivity();
        //toolbarFragment.setBackgroundDrawable(getResources().getDrawable(R.color.colorPrimary));
        mActivity.setToolbar(toolbarFragment, getString(R.string.words_title));
        mActivity.setAsLastFragment(TAG);


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivity.hideKeyboard();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.word, menu);
        mActivity.setActionMenuIconColor(menu, android.R.color.white);
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.displayDictionaryDetails();
    }
}
