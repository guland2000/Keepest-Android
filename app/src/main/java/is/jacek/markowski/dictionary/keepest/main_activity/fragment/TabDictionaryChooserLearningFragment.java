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


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.DictChooserAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.LearningSettingsTabsAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Loaders;

import static android.provider.BaseColumns._ID;


public class TabDictionaryChooserLearningFragment extends Fragment implements LearningSettingsTabsAdapter.TabFragmentTitle {
    public static final String TAG = TabDictionaryChooserLearningFragment.class.getName();
    private String mTitle;

    public TabDictionaryChooserLearningFragment() {
        // Required empty public constructor
    }

    public static TabDictionaryChooserLearningFragment newInstance() {
        Bundle args = new Bundle();
        TabDictionaryChooserLearningFragment fragment = new TabDictionaryChooserLearningFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.tab_learning_mode_dictionaries, container, false);
        mTitle = getString(R.string.dictionaries_tab);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupDictRecycler(getView());

    }

    private void setupDictRecycler(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rv_test_dict_chooser);
        final DictChooserAdapter adapter = new DictChooserAdapter(null, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // setup dict loader
        final Loaders.Dictionary.LoadAllSimilarDictionaries loader = new Loaders.Dictionary.LoadAllSimilarDictionaries(getContext(), recyclerView);
        getLoaderManager().restartLoader(222, null, loader);

        // select all
        CheckBox checkBoxSelectAll = view.findViewById(R.id.checkBox_dicts_select_all);
        checkBoxSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DictionaryManager.DictChooser.resetSet(getContext());
                if (isChecked) {
                    Cursor c = adapter.getCursor();
                    int oldPosition = c.getPosition();
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        int dictId = c.getInt(c.getColumnIndex(_ID));
                        DictionaryManager.DictChooser.addOrRemoveTagIdFromSet(getContext(), dictId);
                        c.moveToNext();
                    }
                    c.moveToPosition(oldPosition);
                }
                getLoaderManager().restartLoader(222, null, loader);
            }
        });
    }

    @Override
    public String getTitle() {
        return mTitle;
    }
}
