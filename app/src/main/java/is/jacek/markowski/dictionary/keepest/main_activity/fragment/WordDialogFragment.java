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
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appolica.flubber.Flubber;

import java.util.ArrayList;
import java.util.List;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.database.DatabaseHelper;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Connection;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Language;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Text;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Tts;
import is.jacek.markowski.dictionary.keepest.main_activity.util.TutorialManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static android.provider.UserDictionary.Words._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_DICTIONARY_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_TRANSLATION;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_WORD;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.TABLE_WORD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Translation.translate;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager.WordEdit.getWordObjectForSave;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager.WordEdit.prepareContentValues;


public class WordDialogFragment extends DialogFragment {
    public static final int ADD_MODE = 0;
    public static final int EDIT_MODE = 1;
    public static final String WORD_ID_KEY = "wordId";
    public static final String TAG = WordDialogFragment.class.getName();
    public static final String WORD_KEY = "word";
    public static final String TRANSLATION_KEY = "translation";
    public static final String IMAGE_KEY = "image";
    public static final String MODE_KEY = "mode";
    private static final String DIALOG_KEY = "dialog";
    public Button mButtonAdvanced;
    public EditText mEditTextWord;
    private EditText mEditTextTranslation;
    private ImageButton mImageButtonTrans;
    private ImageButton mPlayWord;
    private ImageButton mPlayTranslation;

    public static WordDialogFragment newInstance(boolean isDialog) {
        Bundle args = new Bundle();
        args.putBoolean(DIALOG_KEY, isDialog);
        WordDialogFragment fragment = new WordDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static WordDialogFragment newInstance(Context context, boolean isDialog, int mode, long wordId) {
        Bundle args = new Bundle();
        WordDialogFragment fragment = new WordDialogFragment();
        WordManager.Word entry = getWordObjectForSave(context, wordId, null, null, !isDialog);
        args.putBoolean(DIALOG_KEY, isDialog);
        args.putInt(MODE_KEY, mode);
        args.putLong(WORD_ID_KEY, wordId);
        args.putString(WORD_KEY, entry.word);
        args.putString(TRANSLATION_KEY, entry.translation);


        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView tvWordExists = view.findViewById(R.id.tv_exists_word);
        final TextView tvTransExists = view.findViewById(R.id.tv_exists_translation);
        mEditTextWord = view.findViewById(R.id.ed_word);
        mEditTextWord.clearFocus();
        mEditTextWord.requestFocus();
        mEditTextTranslation = view.findViewById(R.id.ed_translation);
        // views from -> to
        FrameLayout frameLayoutWord = view.findViewById(R.id.bt_pb_translate_layout_word);
        mImageButtonTrans = frameLayoutWord.findViewById(R.id.img_arrow_translate_word);
        // views to -> from
        FrameLayout frameLayoutTrans = view.findViewById(R.id.bt_pb_translate_layout_translation);
        final ImageButton imageButtonTransReverse = frameLayoutTrans.findViewById(R.id.img_arrow_translate_translation);

        // translate from -> to
        final DictionaryManager.Dictionary dict = DictionaryManager.getDictData(getContext());
        mImageButtonTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edWord = getActivity().findViewById(R.id.ed_word);
                EditText edTranslation = getActivity().findViewById(R.id.ed_translation);
                FrameLayout root = getActivity().findViewById(R.id.bt_pb_translate_layout_word);
                ProgressBar pBar = root.findViewById(R.id.pb_trans_word);
                ImageButton btTrans = root.findViewById(R.id.img_arrow_translate_word);
                translate(dict.speak_from,
                        dict.speak_to,
                        edWord.getText().toString(),
                        edTranslation,
                        btTrans,
                        pBar,
                        (MainActivity) getActivity());
            }
        });

        // translate to -> from
        imageButtonTransReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edWord = getActivity().findViewById(R.id.ed_word);
                EditText edTranslation = getActivity().findViewById(R.id.ed_translation);
                FrameLayout root = getActivity().findViewById(R.id.bt_pb_translate_layout_translation);
                ProgressBar pBar = root.findViewById(R.id.pb_trans_translation);
                ImageButton btTrans = root.findViewById(R.id.img_arrow_translate_translation);
                translate(dict.speak_to,
                        dict.speak_from,
                        edTranslation.getText().toString(),
                        edWord,
                        btTrans,
                        pBar,
                        (MainActivity) getActivity());
            }
        });

        // play tts
        mPlayWord = view.findViewById(R.id.ibt_play_add_word);
        mPlayWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tts ttsManager = new Tts(getActivity());
                if (!ttsManager.isPlaying()) {
                    boolean isConnected = Connection.isConnected(getContext());
                    DictionaryManager.Dictionary dict = DictionaryManager.getDictData(getContext());
                    ttsManager.onlineTts(mEditTextWord.getText().toString(), dict.speak_from, "", dict.speak_to, isConnected);

                    Flubber.with()
                            .animation(Flubber.AnimationPreset.ZOOM_IN) // Slide up animation
                            .duration(500)
                            .createFor(mPlayWord)
                            .start();
                }
            }
        });
        mPlayTranslation = view.findViewById(R.id.ibt_play_add_translation);
        mPlayTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tts ttsManager = new Tts(getActivity());
                if (!ttsManager.isPlaying()) {
                    boolean isConnected = Connection.isConnected(getContext());
                    DictionaryManager.Dictionary dict = DictionaryManager.getDictData(getContext());
                    ttsManager.onlineTts("", dict.speak_from, mEditTextTranslation.getText().toString(), dict.speak_to, isConnected);

                    Flubber.with()
                            .animation(Flubber.AnimationPreset.ZOOM_IN) // Slide up animation
                            .duration(500)
                            .createFor(mPlayTranslation)
                            .start();
                }
            }
        });

        // show current language hint
        final int mode = getArguments().getInt(MODE_KEY, 0);
        DictionaryManager.Dictionary d = DictionaryManager.getDictData(getContext());
        mEditTextWord.setHint(Language.getCountryName(getContext(), d.speak_from));
        mEditTextTranslation.setHint(Language.getCountryName(getContext(), d.speak_to));

        mEditTextWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                WordManager.WordEdit.saveTextItem(getContext(), WORD_KEY, s.toString());
                Flubber.with()
                        .animation(Flubber.AnimationPreset.ROTATION) // Slide up animation
                        .repeatCount(1)                              // Repeat once
                        .duration(1000)                              // Last for 1000 milliseconds(1 second)
                        .createFor(mImageButtonTrans)                             // Apply it to the view
                        .start();

                // todo async task
                Context context = getContext();
                if (context != null) {
                    SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
                    long dictId = DictionaryManager.getDictData(getContext()).dictId;
                    String selection = COLUMN_WORD + "=? AND " + COLUMN_DICTIONARY_ID + "=?";
                    String[] selectionArgs = new String[]{Text.shrinkText(s.toString()), Long.toString(dictId)};
                    Cursor cursor = db.query(TABLE_WORD, null, selection, selectionArgs, null, null, null);
                    if (cursor != null) {
                        if (cursor.getCount() > 0 && mode == ADD_MODE) {
                            tvWordExists.setVisibility(View.VISIBLE);
                        } else {
                            tvWordExists.setVisibility(View.INVISIBLE);
                        }
                        cursor.close();
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEditTextTranslation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                WordManager.WordEdit.saveTextItem(getContext(), TRANSLATION_KEY, s.toString());
                Flubber.with()
                        .animation(Flubber.AnimationPreset.ROTATION) // Slide up animation
                        .repeatCount(1)                              // Repeat once
                        .duration(1000)                              // Last for 1000 milliseconds(1 second)
                        .createFor(imageButtonTransReverse)                             // Apply it to the view
                        .start();
                // todo async task
                Context context = getContext();
                if (context != null) {
                    SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
                    long dictId = DictionaryManager.getDictData(getContext()).dictId;
                    String selection = COLUMN_TRANSLATION + "=? AND " + COLUMN_DICTIONARY_ID + "=?";
                    String[] selectionArgs = new String[]{Text.shrinkText(s.toString()), Long.toString(dictId)};
                    Cursor cursor = db.query(TABLE_WORD, null, selection, selectionArgs, null, null, null);
                    if (cursor != null) {
                        if (cursor.getCount() > 0 && mode == ADD_MODE) {
                            tvTransExists.setVisibility(View.VISIBLE);
                        } else {
                            tvTransExists.setVisibility(View.INVISIBLE);
                        }
                        cursor.close();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // buttons
        mButtonAdvanced = view.findViewById(R.id.bt_word_adv);
        Button mButtonSave = view.findViewById(R.id.bt_word_save);

        final long wordId = getArguments().getLong(WORD_ID_KEY);
        final boolean isDialog = getArguments().getBoolean(DIALOG_KEY, true);

        final Uri uri = UriHelper.Word.buildWordsAllUri();

        // activity and fragment
        final MainActivity activity = (MainActivity) getActivity();
        if (mode == ADD_MODE) {
            mButtonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WordManager.Word entry = getWordObjectForSave(getContext(), -1, mEditTextWord, mEditTextTranslation, !isDialog);
                    if (Text.validate(getContext(), entry.word) && Text.validate(getContext(), entry.translation)) {
                        ContentValues values = prepareContentValues(getContext(), entry);
                        ContentResolver resolver = getContext().getContentResolver();
                        Uri uriWordId = resolver.insert(uri, values);
                        int wordId = Integer.valueOf(uriWordId.getLastPathSegment());
                        WordManager.Tags.addTagsToWord(getContext(), wordId);
                        WordManager.Word.saveIdOfLastAddedWord(getContext(), wordId);
                        activity.commitWordsFragment();
                        dismiss();
                    }
                }
            });
            mButtonAdvanced.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    fm.beginTransaction().replace(R.id.fragment_container, WordAdvancedFragment.newInstance(getContext()), WordAdvancedFragment.TAG).commit();
                    dismiss();

                }
            });
        } else if (mode == EDIT_MODE) {
            mButtonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WordManager.Word entry = getWordObjectForSave(getContext(), wordId, mEditTextWord, mEditTextTranslation, !isDialog);
                    // Tags to add
                    if (Text.validate(getContext(), entry.word) && Text.validate(getContext(), entry.translation)) {
                        WordManager.Tags.addTagsToWord(getContext(), (int) wordId);
                        ContentValues values = prepareContentValues(getContext(), entry);
                        ContentResolver resolver = getContext().getContentResolver();
                        resolver.update(uri, values, _ID + "=?", new String[]{Long.toString(wordId)});
                        WordManager.Word.saveIdOfLastAddedWord(getContext(), (int) wordId);
                        activity.commitWordsFragment();
                        dismiss();
                    }

                }
            });
            mButtonAdvanced.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // show advanced dialog
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    fm.beginTransaction().replace(R.id.fragment_container, WordAdvancedFragment.newInstance(getContext(), wordId, EDIT_MODE), WordAdvancedFragment.TAG).commit();
                    dismiss();

                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final long wordId = getArguments().getLong(WORD_ID_KEY);
        boolean isDialog = getArguments().getBoolean(DIALOG_KEY, false);
        WordManager.Word entry = getWordObjectForSave(getContext(), wordId, null, null, !isDialog);
        String wordDraft = WordManager.WordEdit.getTextItem(getContext(), WORD_KEY);
        String translationDraft = WordManager.WordEdit.getTextItem(getContext(), TRANSLATION_KEY);
        if (WordManager.WordEdit.isEdited(getContext())) {
            mEditTextWord.setText(wordDraft);
            mEditTextTranslation.setText(translationDraft);
        } else {
            mEditTextWord.setText(entry.word);
            mEditTextTranslation.setText(entry.translation);
        }

        //advanced button visibility
        if (isDialog) {
            mButtonAdvanced.setVisibility(View.VISIBLE);
        } else {
            mButtonAdvanced.setVisibility(View.INVISIBLE);
        }
        if (getShowsDialog()) {
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        List<TutorialManager.TutorialItem> items = new ArrayList<>();
        items.add(new TutorialManager.TutorialItem(mImageButtonTrans, getString(R.string.tut_translate_word)));
        try {
            TutorialManager.showTutorialSequence(getActivity(), items, "wordAdvanced");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getShowsDialog()) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.fragment_word_translation, container, false);
    }
}
