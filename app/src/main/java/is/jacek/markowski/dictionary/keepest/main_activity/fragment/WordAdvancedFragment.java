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


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.WordAdvancedTabsAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.DictionaryDialogFragment.MODE_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.ADD_MODE;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.EDIT_MODE;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.WORD_ID_KEY;


public class WordAdvancedFragment extends Fragment {
    public static final String NOTES_KEY = "notes";
    public static final String TAG = WordAdvancedFragment.class.getName();
    public ViewPager mViewPager;
    public ArrayList<WordAdvancedTabsAdapter.TabFragmentWord> mFragments;
    public TabLayout mTabLayout;
    private WordManager.Word mWord;

    public WordAdvancedFragment() {
        // Required empty public constructor
    }

    public static WordAdvancedFragment newInstance(Context context) {
        Bundle args = new Bundle();
        WordAdvancedFragment fragment = new WordAdvancedFragment();
        fragment.setArguments(args);
        WordManager.WordEdit.setId(context, -1);
        WordManager.WordEdit.setMode(context, ADD_MODE);
        return fragment;
    }

    public static WordAdvancedFragment newInstance(Context context, long wordId, int mode) {
        Bundle args = new Bundle();
        args.putLong(WORD_ID_KEY, wordId);
        args.putInt(MODE_KEY, mode);
        WordAdvancedFragment fragment = new WordAdvancedFragment();
        fragment.setArguments(args);
        // save in pref
        WordManager.WordEdit.setId(context, wordId);
        WordManager.WordEdit.setMode(context, mode);
        return fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainActivity activity = (MainActivity) getActivity();
        activity.hideKeyboard();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View root = inflater.inflate(R.layout.word_advanced_dialog, container, false);
        MainActivity activity = (MainActivity) getActivity();
        activity.setAsLastFragment(TAG);
        activity.mIsWordFragmentOpened = false; // go back to words fragment on back pressed
        activity.setAsLastFragment(WordFragment.TAG);

        // tabs
        long wordId = WordManager.WordEdit.getId(getContext());
        mViewPager = root.findViewById(R.id.pager);
        mFragments = new ArrayList<>();
        mWord = WordManager.getWordById(getContext(), wordId);
        mFragments.add(TabGifsWordFragment.newInstance(wordId));
        mFragments.add(TabNotesWordFragment.newInstance(mWord.notes));
        if (WordManager.WordEdit.getMode(getContext()) == ADD_MODE) {
            mFragments.add(TabTagsWordFragment.newInstance(0));
        } else {
            mFragments.add(TabTagsWordFragment.newInstance((int) wordId));
        }
        WordAdvancedTabsAdapter adapter = new WordAdvancedTabsAdapter(getChildFragmentManager(), mFragments);
        mViewPager.setAdapter(adapter);
        mTabLayout = root.findViewById(R.id.tab_layout_word);
        mTabLayout.post(new Runnable() {
            @Override
            public void run() {
                mTabLayout.setupWithViewPager(mViewPager);
            }
        });
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                try {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        int mode = WordManager.WordEdit.getMode(getContext());
        long wordId = WordManager.WordEdit.getId(getContext());
        WordDialogFragment f;
        if (mode == EDIT_MODE) {
            f = WordDialogFragment.newInstance(getContext(), false, EDIT_MODE, wordId);
        } else {
            f = WordDialogFragment.newInstance(false);
        }
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_word_container, f, WordDialogFragment.TAG)
                .commit();
        activity.setAsLastFragment(TAG);

    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbarFragment = getActivity().findViewById(R.id.toolbar);
        final MainActivity activity = (MainActivity) getActivity();
        if (EDIT_MODE == WordManager.WordEdit.getMode(activity.getBaseContext())) {
            activity.setToolbar(toolbarFragment, getString(R.string.edit_word));
        } else {
            activity.setToolbar(toolbarFragment, getString(R.string.add_word));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.word_add, menu);
        MainActivity activity = (MainActivity) getActivity();
        activity.setActionMenuIconColor(menu, android.R.color.white);
    }

}
