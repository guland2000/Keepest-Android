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


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.WordAdvancedTabsAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordAdvancedFragment.NOTES_KEY;


public class TabNotesWordFragment extends Fragment implements WordAdvancedTabsAdapter.TabFragmentWord {
    public static final String TAG = "notesTab";
    private String mTitle;

    public TabNotesWordFragment() {
        // Required empty public constructor
    }

    public static TabNotesWordFragment newInstance(String notes) {
        Bundle args = new Bundle();
        TabNotesWordFragment fragment = new TabNotesWordFragment();
        args.putString(NOTES_KEY, notes);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.tab_notes_word, container, false);
        EditText editTextNotes = root.findViewById(R.id.ed_adv_notes);
        editTextNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                WordManager.WordEdit.saveTextItem(getContext(), NOTES_KEY, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mTitle = getString(R.string.notes_tab);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        String savedNotes = WordManager.WordEdit.getTextItem(getContext(), NOTES_KEY);
        EditText ed = getView().findViewById(R.id.ed_adv_notes);
        if (savedNotes.length() > 0) {
            ed.setText(savedNotes);
        } else {
            String notes = getArguments().getString(NOTES_KEY, "");
            ed.setText(notes);
        }
    }

    @Override
    public String getTitle() {
        return mTitle;
    }
}
