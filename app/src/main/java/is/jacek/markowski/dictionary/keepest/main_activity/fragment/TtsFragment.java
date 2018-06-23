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
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.TtsAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Cache;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences;

import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.TextToSpeech.ENGINE_CHOOSER;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.TextToSpeech.ENGINE_DEFAULT;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.TextToSpeech.ENGINE_ONE;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.TextToSpeech.ENGINE_ONE_CODE;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.TextToSpeech.ENGINE_TWO;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.TextToSpeech.ENGINE_TWO_CODE;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.TextToSpeech.GOOGLE_TRANSLATE_CODE;


public class TtsFragment extends Fragment {
    public static final String TAG = TtsFragment.class.getName();
    private MainActivity activity;
    private Spinner defaultSpinner;
    private Spinner engineSpinner1;
    private Spinner engineSpinner2;
    public Spinner engineChooser;
    private Spinner engineLocale;
    private Button addSettingsButton;

    public TtsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbarFragment = getActivity().findViewById(R.id.toolbar);
        final MainActivity activity = (MainActivity) getActivity();
        activity.setToolbar(toolbarFragment, getString(R.string.text_to_speech));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_tts, container, false);
        Cache.clearCache(getContext());
        // recycler view
        RecyclerView rv = root.findViewById(R.id.rv_tts);
        final TtsAdapter adapter = new TtsAdapter(getActivity());
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter.reloadList();

        activity = (MainActivity) getActivity();
        activity.setAsLastFragment(TAG);
        defaultSpinner = root.findViewById(R.id.spinner_default_tts);
        defaultSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1: {
                        Preferences.TextToSpeech.write(getContext(), ENGINE_DEFAULT, ENGINE_ONE_CODE);
                        break;
                    }
                    case 2: {
                        Preferences.TextToSpeech.write(getContext(), ENGINE_DEFAULT, ENGINE_TWO_CODE);
                        break;
                    }
                    default:
                        Preferences.TextToSpeech.write(getContext(), ENGINE_DEFAULT, GOOGLE_TRANSLATE_CODE);
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        addSettingsButton = root.findViewById(R.id.bt_add_tts_lang);
        addSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String engine = Long.toString(engineChooser.getSelectedItemId() + 1);
                String lang = engineLocale.getSelectedItem().toString();
                String ttsSetting = engine + "|" + lang;
                if (!Preferences.TextToSpeech.isIdInSet(getContext(), ttsSetting)) {
                    Preferences.TextToSpeech.addOrRemoveTtsSettings(getContext(), ttsSetting);
                    adapter.reloadList();
                }
            }
        });

        engineSpinner1 = root.findViewById(R.id.spinner_engine_1);
        SpinnerEngine1Listener engine1Listener = new SpinnerEngine1Listener();
        engineSpinner1.setOnTouchListener(engine1Listener);
        engineSpinner1.setOnItemSelectedListener(engine1Listener);

        engineSpinner2 = root.findViewById(R.id.spinner_engine_2);
        SpinnerEngine2Listener engine2Listener = new SpinnerEngine2Listener();
        engineSpinner2.setOnTouchListener(engine2Listener);
        engineSpinner2.setOnItemSelectedListener(engine2Listener);

        engineChooser = root.findViewById(R.id.spinner_tts_engine);
        SpinnerChooserListener listener = new SpinnerChooserListener();
        engineChooser.setOnTouchListener(listener);
        engineChooser.setOnItemSelectedListener(listener);

        engineLocale = root.findViewById(R.id.spinner_lang);

        populateEngineSpinners();
        readValuesFromPreferences();

        return root;
    }

    private void readValuesFromPreferences() {
        String engine_one = Preferences.TextToSpeech.read(getContext(), ENGINE_ONE);
        String engine_two = Preferences.TextToSpeech.read(getContext(), ENGINE_TWO);
        String engine_default = Preferences.TextToSpeech.read(getContext(), ENGINE_DEFAULT);
        if (engine_one.equals("")) {
            engine_one = activity.mTts_one.getDefaultEngine();
            Preferences.TextToSpeech.write(getContext(), ENGINE_ONE, engine_one);
        }
        if (engine_two.equals("")) {
            engine_two = activity.mTts_one.getDefaultEngine();
            Preferences.TextToSpeech.write(getContext(), ENGINE_TWO, engine_two);
        }

        // populate locales
        setSpinText(engineSpinner1, engine_one);
        setSpinText(engineSpinner2, engine_two);
        engineChooser.setSelection(0);
        populateLocalesSpinner(1);

        // default engine chooser
        if (engine_default.equals(ENGINE_ONE_CODE)) {
            defaultSpinner.setSelection(1);
        } else if (engine_default.equals(ENGINE_TWO_CODE)) {
            defaultSpinner.setSelection(2);
        } else {
            defaultSpinner.setSelection(0);
        }
    }

    public void setSpinText(Spinner spin, String text) {
        for (int i = 0; i < spin.getAdapter().getCount(); i++) {
            if (spin.getAdapter().getItem(i).toString().contains(text)) {
                spin.setSelection(i);
                break;
            }
        }

    }

    public void populateEngineSpinners() {
        List<String> enginesArray = new ArrayList<>();
        List<TextToSpeech.EngineInfo> engines = activity.mTts_one.getEngines();

        for (TextToSpeech.EngineInfo engineInfo : engines) {
            enginesArray.add(engineInfo.name);
        }

        Collections.sort(enginesArray);

        ArrayAdapter<String> adapterEngines = new ArrayAdapter<>(
                activity, android.R.layout.simple_spinner_item, enginesArray);

        adapterEngines.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        engineSpinner1.setAdapter(adapterEngines);
        engineSpinner2.setAdapter(adapterEngines);
    }

    public void populateLocalesSpinner(int engineNumber) {
        ArrayList<String> localesArray = new ArrayList<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            int res = TextToSpeech.LANG_NOT_SUPPORTED;
            if (engineNumber == 1) {
                res = activity.mTts_one.isLanguageAvailable(locale);
            } else if (engineNumber == 2) {
                res = activity.mTts_two.isLanguageAvailable(locale);
            }
            if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                localesArray.add(locale.getDisplayName() + "|" + locale);
            }
        }
        Collections.sort(localesArray);
        ArrayAdapter<String> adapterLocales = new ArrayAdapter<>(
                activity, android.R.layout.simple_spinner_item, localesArray);
        engineLocale.setAdapter(adapterLocales);
    }

    public class SpinnerChooserListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

        boolean userSelect = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            userSelect = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (userSelect) {
                // Your selection handling code here
                int engineNumber = pos + 1;
                Preferences.TextToSpeech.write(activity, ENGINE_CHOOSER, Integer.toString(engineNumber));
                populateLocalesSpinner(engineNumber);
                userSelect = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    public class SpinnerEngine1Listener extends SpinnerChooserListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (userSelect) {
                String engine = engineSpinner1.getSelectedItem().toString();
                if (activity.mTts_one != null) {
                    activity.mTts_one.shutdown();
                }
                activity.mTts_one = new TextToSpeech(activity, activity, engine);
                Preferences.TextToSpeech.write(getContext(), ENGINE_ONE, engine);
                userSelect = false;
            }
        }
    }

    public class SpinnerEngine2Listener extends SpinnerChooserListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (userSelect) {
                String engine = engineSpinner2.getSelectedItem().toString();
                if (activity.mTts_two != null) {
                    activity.mTts_two.shutdown();
                }
                activity.mTts_two = new TextToSpeech(activity, activity, engine);
                Preferences.TextToSpeech.write(getContext(), ENGINE_TWO, engine);
                userSelect = false;
            }
        }
    }
}
