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


import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.TagAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.WordAdvancedTabsAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.database.DatabaseHelper;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Loaders;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Text;
import is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.COLUMN_TAG;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TABLE_TAGS;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.WORD_ID_KEY;


public class TabTagsWordFragment extends Fragment implements WordAdvancedTabsAdapter.TabFragmentWord {
    public static final String TAG = "tagsTab";
    private Loaders.Tags.LoadAllTags mLoader;
    private String mTitle = "TAG";

    public TabTagsWordFragment() {
        // Required empty public constructor
    }

    public static TabTagsWordFragment newInstance(int wordID) {
        Bundle args = new Bundle();
        args.putInt(WORD_ID_KEY, wordID);
        TabTagsWordFragment fragment = new TabTagsWordFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.tab_tag_editor, container, false);
        final RecyclerView recyclerView = root.findViewById(R.id.rv_tags);
        ImageButton addTagButton = root.findViewById(R.id.bt_add_tag);
        final EditText searchEditText = root.findViewById(R.id.ed_search_tag);


        TagAdapter adapter = new TagAdapter(null, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // setup tag loader
        Bundle args = getArguments();
        final int wordId = args.getInt(WORD_ID_KEY, -1);
        final Loaders.Tags.LoadAllTags loader = new Loaders.Tags.LoadAllTags(getContext(), recyclerView, wordId);
        mLoader = loader;
        getLoaderManager().restartLoader(Loaders.Tags.LOADER_ID, null, loader);
        ImageButton clearSearch = root.findViewById(R.id.bt_tag_search_clear);
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

        // search tag
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Bundle args = new Bundle();
                String search = s.toString().trim();
                if (search.length() == 0) {
                    args = null;
                } else {
                    args.putString(Loaders.Tags.SEARCH_KEY, search);
                }
                getLoaderManager().restartLoader(Loaders.Tags.LOADER_ID, args, loader);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // add tag button
        addTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tagName = searchEditText.getText().toString().replaceAll("\\s+", "");
                if (Text.validate(getContext(), tagName)) {
                    ContentResolver resolver = getContext().getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_TAG, tagName);
                    resolver.insert(UriHelper.TagsWord.buildInsertTagUri(), values);
                    SQLiteDatabase db = new DatabaseHelper(getContext()).getReadableDatabase();
                    Cursor cursor = db.query(TABLE_TAGS, null, COLUMN_TAG + "=?", new String[]{tagName}, null, null, null);
                    int idOfNewTag = 0;
                    if (cursor != null && cursor.getCount() == 1) {
                        cursor.moveToFirst();
                        idOfNewTag = cursor.getInt(cursor.getColumnIndex(_ID));
                    }
                    if (idOfNewTag > 0) {
                        WordManager.WordEdit.addOrRemoveTagIdFromSet(getContext(), idOfNewTag);
                    }
                    Bundle args = new Bundle();
                    args.putString(Loaders.Tags.SEARCH_KEY, tagName);
                    getLoaderManager().restartLoader(Loaders.Tags.LOADER_ID, args, loader);
                    MainActivity activity = (MainActivity) getActivity();
                    activity.hideKeyboard();
                }
            }
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mTitle = getString(R.string.tags_tab);
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public void restartLoader() {
        final EditText searchEditText = getView().findViewById(R.id.ed_search_tag);
        Bundle args = new Bundle();
        String tagName = searchEditText.getText().toString();
        args.putString(Loaders.Tags.SEARCH_KEY, tagName);
        getLoaderManager().restartLoader(Loaders.Tags.LOADER_ID, null, mLoader);
    }
}
