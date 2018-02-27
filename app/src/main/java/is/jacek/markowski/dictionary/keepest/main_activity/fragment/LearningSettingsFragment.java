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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.LearningSettingsTabsAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.database.DatabaseHelper;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Language;
import is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.LearningModeWritingFragment.TYPED_ANSWER;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager.getDictData;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager.MODE_TRANSLATION_TAG;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager.MODE_TRANSLATION_WORD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager.MODE_WORD_TAG;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager.MODE_WORD_TRANSLATION;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager.MODE_WRITING_TRANSLATION;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager.MODE_WRITING_WORD;


public class LearningSettingsFragment extends Fragment {
    public static final String TAG = LearningSettingsFragment.class.getName();
    public ViewPager mViewPager;
    public ArrayList<LearningSettingsTabsAdapter.TabFragmentTitle> mFragments;
    public TabLayout mTabLayout;
    private Spinner mSpinnerModes;
    private CheckBox mShowGif;
    private CheckBox mReadAnswers;

    public LearningSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_learning_settings, container, false);
        final MainActivity activity = (MainActivity) getActivity();
        mShowGif = root.findViewById(R.id.checkBox_learning_show_gif);
        mShowGif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.setShowGif(getContext(), isChecked);
            }
        });
        mReadAnswers = root.findViewById(R.id.checkBox_read_answers);
        mReadAnswers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.setReadAnswers(getContext(), isChecked);
            }
        });
        activity.setAsLastFragment(TAG);
        Button btStartSession = root.findViewById(R.id.bt_start_test);
        final EditText edQuestionsNumber = root.findViewById(R.id.ed_number_of_questions);
        btStartSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prepare new set of questions
                int questionsCount = 0;
                try {
                    questionsCount = Integer.valueOf(edQuestionsNumber.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                String error = "";
                int tagsSelected = WordManager.TagChooser.getSetOfTagId(getContext()).size();
                int dictsSelected = DictionaryManager.DictChooser.getSetOfId(getContext()).size();
                switch (getLearningMode()) {
                    case MODE_TRANSLATION_TAG: {
                        if (tagsSelected < 1) {
                            error = getString(R.string.choose_tags);
                        }
                        break;
                    }
                    case MODE_WORD_TAG: {
                        if (tagsSelected < 1) {
                            error = getString(R.string.choose_tags);
                        }
                        break;
                    }
                    case MODE_WORD_TRANSLATION: {
                        if (dictsSelected < 1) {
                            error = getString(R.string.choose_dictionaries);
                        }
                        break;
                    }
                    case MODE_TRANSLATION_WORD: {
                        if (dictsSelected < 1) {
                            error = getString(R.string.choose_dictionaries);
                        }
                        break;
                    }
                }
                if (!LearningManager.createNewSession(getContext().getContentResolver(), new DatabaseHelper(getContext()), questionsCount, getLearningMode(), WordManager.TagChooser.getSetOfTagId(getContext()))) {
                    error = getString(R.string.zero_words_found);
                }

                if (questionsCount <= 0) {
                    error = getString(R.string.choose_number_of_questions);
                }


                if (error.length() > 0) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    LearningManager.stopSession();
                } else {
                    // reset typed answer in writing mode
                    WordManager.WordEdit.saveTextItem(getContext(), TYPED_ANSWER, "");
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new LearningSessionFragment(), LearningSessionFragment.TAG)
                            .commit();
                }
            }
        });

        // learning modes
        mSpinnerModes = root.findViewById(R.id.spinner_test_mode);
        List<String> spinnerArray = new ArrayList<>();
        String from = Language.getCountryName(getContext(), getDictData(getActivity()).speak_from);
        String to = Language.getCountryName(getContext(), getDictData(getActivity()).speak_to);

        String modeWordTranslation = getString(R.string.test_mode) + ": " + from + " -> " + to;
        String modeTranslationWord = getString(R.string.test_mode) + ": " + to + " -> " + from;
        String modeWordTag = getString(R.string.test_mode) + ": " + from + " -> #TAG";
        String modeTranslationTag = getString(R.string.test_mode) + ": " + to + " -> #TAG";
        String modeWritingWord = getString(R.string.writing_mode) + ": " + to;
        String modeWritingTranslation = getString(R.string.writing_mode) + ": " + from;

        spinnerArray.add(modeWordTranslation);
        spinnerArray.add(modeTranslationWord);
        spinnerArray.add(modeWordTag);
        spinnerArray.add(modeTranslationTag);
        spinnerArray.add(modeWritingWord);
        spinnerArray.add(modeWritingTranslation);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerModes.setAdapter(adapter);
        mSpinnerModes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                saveInPreferences();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // load setting from preferences
        mSpinnerModes.setSelection(LearningManager.getPreferencesLearningMode(getContext()));
        edQuestionsNumber.setText(Integer.toString(LearningManager.getPreferencesQuestionCount(getContext())));

        edQuestionsNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveInPreferences();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // tabs
        mViewPager = root.findViewById(R.id.tabs_learning_settings_pager);
        mFragments = new ArrayList<>();
        mFragments.add(TabDictionaryChooserLearningFragment.newInstance());
        mFragments.add(TabTagChooserLearningFragment.newInstance());
        LearningSettingsTabsAdapter tabAdapter = new LearningSettingsTabsAdapter(getChildFragmentManager(), mFragments);
        mViewPager.setAdapter(tabAdapter);
        mTabLayout = root.findViewById(R.id.tab_layout_learning_settings);
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

    private void saveInPreferences() {
        EditText ed = getView().findViewById(R.id.ed_number_of_questions);
        Spinner sp = getView().findViewById(R.id.spinner_test_mode);
        int mode = sp.getSelectedItemPosition();
        try {
            int questions = Integer.valueOf(ed.getText().toString());
            LearningManager.saveSettingsInPreferences(getContext(), mode, questions);
        } catch (Exception e) {
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mShowGif.setChecked(Preferences.isShowGif(getContext()));
        mReadAnswers.setChecked(Preferences.isReadAnswers(getContext()));

    }


    private int getLearningMode() {
        switch (mSpinnerModes.getSelectedItemPosition()) {
            case 0: {
                return MODE_WORD_TRANSLATION;
            }
            case 1: {
                return MODE_TRANSLATION_WORD;
            }
            case 2: {
                return MODE_WORD_TAG;
            }
            case 3: {
                return MODE_TRANSLATION_TAG;
            }
            case 4: {
                return MODE_WRITING_WORD;
            }
            case 5: {
                return MODE_WRITING_TRANSLATION;
            }
            default: {
                return MODE_WORD_TRANSLATION;
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbarFragment = getActivity().findViewById(R.id.toolbar);
        final MainActivity activity = (MainActivity) getActivity();
        activity.setToolbar(toolbarFragment, getString(R.string.learning_mode));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.learning, menu);
    }
}
