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


import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.database.Contract;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Text;

import static android.provider.BaseColumns._ID;


public class DictionaryDialogFragment extends DialogFragment {
    public static final String MODE_KEY = "MODE";
    public static final String DICT_KEY = "DICTIONARY";
    public static final String DICT_ID_KEY = "DICTIONARY_ID";
    public static final String FROM_KEY = "SPEAK_FROM";
    public static final String TO_KEY = "SPEAK_TO";

    public static final int ADD_MODE = 0;
    public static final int EDIT_MODE = 1;
    public static final int UNKNOWN_MODE = -1;
    public static final String TAG = DictionaryDialogFragment.class.getName();


    private EditText mDictionaryEditText;
    private Spinner mSpinnerFrom;
    private Spinner mSpinnerTo;

    public static DictionaryDialogFragment newInstance(int mode,
                                                       Long dictId,
                                                       String dictionary,
                                                       String from,
                                                       String to) {
        Bundle args = new Bundle();
        args.putInt(MODE_KEY, mode);
        args.putString(DICT_KEY, dictionary);
        args.putString(FROM_KEY, from);
        args.putString(TO_KEY, to);
        args.putLong(DICT_ID_KEY, dictId);
        DictionaryDialogFragment fragment = new DictionaryDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onStart() {
        super.onStart();
        mDictionaryEditText = getDialog().findViewById(R.id.ed_tag_edit_name);
        mSpinnerFrom = getDialog().findViewById(R.id.spinner_from);
        mSpinnerTo = getDialog().findViewById(R.id.spinner_to);
        // show keyboard
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        Bundle bundle = getArguments();
        mDictionaryEditText.setText(bundle.getString(DICT_KEY, ""));
        List<String> langValues = Arrays.asList(getResources().getStringArray(R.array.selectValues));
        int speakFromId = langValues.indexOf(bundle.getString(FROM_KEY, ""));
        int speakToId = langValues.indexOf(bundle.getString(TO_KEY, ""));
        if (speakToId == -1) {
            speakToId = 0;
        }
        if (speakFromId == -1) {
            int systemLang = langValues.indexOf(Locale.getDefault().getLanguage());
            speakToId = systemLang == speakToId ? 1 : speakToId;
            speakFromId = systemLang == -1 ? 1 : systemLang;
        }
        mSpinnerFrom.setSelection(speakFromId);
        mSpinnerTo.setSelection(speakToId);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        int mode = args.getInt(MODE_KEY, UNKNOWN_MODE);
        final long dictId = args.getLong(DICT_ID_KEY, -1);

        final Uri uri = Contract.Dictionary.URI_FULL.buildUpon()
                .appendPath(Contract.Dictionary.URI_PATH_DICTS_ALL).build();
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(R.layout.dictionary_dialog);
        final MainActivity activity = (MainActivity) getActivity();
        dialog.setPositiveButton(R.string.save, null);
        dialog.setNegativeButton(R.string.cancel, null);
        final AlertDialog d = dialog.create();

        if (mode == ADD_MODE) {
            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String name = mDictionaryEditText.getText().toString();
                            if (Text.validate(getContext(), name)) {
                                ContentResolver resolver = getContext().getContentResolver();
                                ContentValues values = prepareContentValues();
                                Uri dictIdUri = resolver.insert(uri, values);
                                long dictId = Long.valueOf(dictIdUri.getLastPathSegment());
                                //DictionaryManager.openFirstCreatedDictionary(activity);
                                DictionaryManager.saveDictData(activity, dictId);
                                DictionaryManager.setDictionaryAsOpened(getContext(), dictId);
                                activity.commitWordsFragment();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            });
        } else if (mode == EDIT_MODE) {
            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String name = mDictionaryEditText.getText().toString();
                            if (Text.validate(getContext(), name)) {
                                ContentResolver resolver = getContext().getContentResolver();
                                ContentValues values = prepareContentValues();
                                resolver.update(uri, values, _ID + "=?", new String[]{Long.toString(dictId)});
                                dialog.dismiss();
                                try {
                                    activity.displayDictionaryDetails();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            });
        }
        return d;
    }

    private ContentValues prepareContentValues() {
        String dict = Text.shrinkText(mDictionaryEditText.getText().toString());
        int speakFromId = (int) mSpinnerFrom.getSelectedItemId();
        int speakToId = (int) mSpinnerTo.getSelectedItemId();
        List<String> langValues = Arrays.asList(getResources().getStringArray(R.array.selectValues));
        ContentValues values = new ContentValues();
        values.put(Contract.Dictionary.Entry.COLUMN_DICTIONARY_NAME, dict);
        values.put(Contract.Dictionary.Entry.COLUMN_DICTIONARY_FROM, langValues.get(speakFromId));
        values.put(Contract.Dictionary.Entry.COLUMN_DICTIONARY_TO, langValues.get(speakToId));
        return values;
    }
}

