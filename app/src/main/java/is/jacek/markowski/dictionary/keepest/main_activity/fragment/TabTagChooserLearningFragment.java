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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.LearningSettingsTabsAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.TagChooserAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Loaders;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static android.provider.BaseColumns._ID;


public class TabTagChooserLearningFragment extends Fragment implements LearningSettingsTabsAdapter.TabFragmentTitle {
    public static final String TAG = TabTagChooserLearningFragment.class.getName();
    private String mTitle;

    public TabTagChooserLearningFragment() {
        // Required empty public constructor
    }

    public static TabTagChooserLearningFragment newInstance() {
        Bundle args = new Bundle();
        TabTagChooserLearningFragment fragment = new TabTagChooserLearningFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.tab_learning_mode_tags, container, false);
        mTitle = getString(R.string.tags_tab);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupTagRecycler(getView());

    }

    private void setupTagRecycler(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rv_test_tags);
        final EditText searchEditText = view.findViewById(R.id.tv_test_tag_search);

        ImageButton clearSearch = view.findViewById(R.id.bt_test_tag_search_clear);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchEditText.getText().toString().length() == 0) {
                    ((MainActivity) getActivity()).hideKeyboard();
                } else {
                    searchEditText.setText("");
                }
            }
        });
        final TagChooserAdapter adapter = new TagChooserAdapter(null, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // setup tag loader
        final Loaders.Tags.TagChooserLoader loader = new Loaders.Tags.TagChooserLoader(getContext(), recyclerView);
        getLoaderManager().restartLoader(111, null, loader);

        // search tag
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Bundle args = new Bundle();
                args.putString(Loaders.Tags.SEARCH_KEY, s.toString().trim());
                getLoaderManager().restartLoader(Loaders.Tags.LOADER_ID, args, loader);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // select all
        CheckBox checkBoxSelectAll = view.findViewById(R.id.checkBox_tags_select_all);
        checkBoxSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WordManager.TagChooser.resetSet(getContext());
                if (isChecked) {
                    Cursor c = adapter.getCursor();
                    int oldPosition = c.getPosition();
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        int dictId = c.getInt(c.getColumnIndex(_ID));
                        WordManager.TagChooser.addOrRemoveTagIdFromSet(getContext(), dictId);
                        c.moveToNext();
                    }
                    c.moveToPosition(oldPosition);
                }
                getLoaderManager().restartLoader(111, null, loader);
            }
        });
    }

    @Override
    public String getTitle() {
        return mTitle;
    }
}
