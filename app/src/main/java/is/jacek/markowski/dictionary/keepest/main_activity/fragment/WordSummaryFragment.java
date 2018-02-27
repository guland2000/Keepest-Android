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


import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.appolica.flubber.Flubber;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.WordAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Connection;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Giphy;
import is.jacek.markowski.dictionary.keepest.main_activity.util.OnSwipeTouchListener;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Tts;
import is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static android.provider.BaseColumns._ID;
import static android.support.v4.content.ContextCompat.getDrawable;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_FAVOURITE;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.EDIT_MODE;


public class WordSummaryFragment extends DialogFragment {

    public static final String TAG = WordSummaryFragment.class.getName();
    private static final String WORD = "word";
    private static final String TRANSLATION = "translation";
    private static final String TAGS = "tags";
    private static final String NOTES = "notes";
    private static final String WORD_ID = "word_id";
    private static final int RIGHT = 1;
    private static final int LEFT = -1;
    private ImageButton mStar;

    public static WordSummaryFragment newInstance(Context context, long wordId) {
        Bundle args = new Bundle();
        WordSummaryFragment fragment = new WordSummaryFragment();
        WordManager.Word wordObj = WordManager.getWordById(context, wordId);
        args.putString(WORD, wordObj.word);
        args.putString(TRANSLATION, wordObj.translation);
        args.putString(NOTES, wordObj.notes);
        args.putString(TAGS, wordObj.tags);
        args.putLong(WORD_ID, wordId);
        fragment.setArguments(args);
        return fragment;
    }

    private void displayWordData() {
        View view = getView();
        final Bundle args = getArguments();
        final TextView word = view.findViewById(R.id.tv_summary_word);
        final TextView translation = view.findViewById(R.id.tv_summary_translation);
        TextView notesAndTags = view.findViewById(R.id.tv_summary_tags_notes);
        Button btClose = view.findViewById(R.id.bt_summary_close);
        Button btEdit = view.findViewById(R.id.bt_summary_edit);
        final ImageButton btPlayWord = view.findViewById(R.id.bt_summary_play_word);
        final int wordId = (int) args.getLong(WORD_ID);
        ImageButton next = view.findViewById(R.id.img_word_next);
        ImageButton prev = view.findViewById(R.id.img_word_prev);
        ImageView gifView = getView().findViewById(R.id.img_summary_gif_view);
        getView().setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                showWordOnDirection(LEFT);
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                showWordOnDirection(RIGHT);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWordOnDirection(RIGHT);
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWordOnDirection(LEFT);
            }
        });


        mStar = view.findViewById(R.id.img_summary_star);
        mStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                WordManager.Word entry = WordManager.getWordById(getContext(), wordId);
                if (entry.favourite == 1) {
                    values.put(COLUMN_FAVOURITE, 0);
                } else {
                    values.put(COLUMN_FAVOURITE, 1);
                }
                final String where = _ID + "=?";
                final String[] selectionArgs = new String[]{Integer.toString(wordId)};
                getContext().getContentResolver().update(
                        UriHelper.Word.buildWordsAllUri(),
                        values,
                        where,
                        selectionArgs);
                setStarStatus();
            }
        });

        btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WordManager.WordEdit.resetValues(getActivity());
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction().replace(R.id.fragment_container, WordAdvancedFragment.newInstance(getContext(), wordId, EDIT_MODE), WordAdvancedFragment.TAG).commit();
                // remember position
                WordManager.Word.saveIdOfLastAddedWord(getActivity(), wordId);
                dismiss();
                MainActivity activity = (MainActivity) getActivity();
                try {
                    activity.hideWordsSearchToolbar(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btPlayWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tts ttsManager = new Tts(getActivity());
                if (!ttsManager.isPlaying()) {
                    boolean isConnected = Connection.isConnected(getContext());
                    DictionaryManager.Dictionary dict = DictionaryManager.getDictData(getContext());
                    ttsManager.onlineTts(word.getText().toString(), dict.speak_from, "", dict.speak_to, isConnected);

                    Flubber.with()
                            .animation(Flubber.AnimationPreset.ZOOM_IN) // Slide up animation
                            .duration(500)
                            .createFor(btPlayWord)
                            .start();
                }
            }
        });
        final ImageButton btPlayTrans = view.findViewById(R.id.bt_summary_play_translation);
        btPlayTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tts ttsManager = new Tts(getActivity());
                if (!ttsManager.isPlaying()) {
                    boolean isConnected = Connection.isConnected(getContext());
                    DictionaryManager.Dictionary dict = DictionaryManager.getDictData(getContext());
                    ttsManager.onlineTts("", dict.speak_from, translation.getText().toString(), dict.speak_to, isConnected);

                    Flubber.with()
                            .animation(Flubber.AnimationPreset.ZOOM_IN) // Slide up animation
                            .duration(500)
                            .createFor(btPlayTrans)
                            .start();
                }
            }
        });
        word.setText(args.getString(WORD));
        translation.setText(args.getString(TRANSLATION));
        String notes = args.getString(NOTES);
        String[] splitTags = args.getString(TAGS).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String tag : splitTags) {
            if (tag.length() > 0) {
                builder.append("#" + tag + " ");
            }
        }
        String tags = builder.toString();
        notesAndTags.setText(tags + "\n" + notes);
        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        //display gif
        WordManager.Word entry = WordManager.getWordById(getContext(), wordId);
        Giphy.displayGif(getActivity(), entry.imageUrl, gifView);
        setStarStatus();
    }

    private void showWordOnDirection(int direction) {
        Bundle args = getArguments();
        MainActivity activity = (MainActivity) getActivity();
        WordAdapter adapter = (WordAdapter)
                activity.mWordFragment.mRecyclerView.getAdapter();
        long oldId = args.getLong(WORD_ID, 0);
        int newPosition = adapter.findPositionOfWord(oldId) + direction;
        long wordId = adapter.getIdOnPosition(newPosition);
        WordManager.Word entry = WordManager.getWordById(getContext(), wordId);
        args.putLong(WORD_ID, wordId);
        args.putString(WORD, entry.word);
        args.putString(TRANSLATION, entry.translation);
        args.putString(NOTES, entry.notes);
        args.putString(TAGS, entry.tags);
        ConstraintLayout summaryLayout = getView().findViewById(R.id.layout_word_summary);
        switch (direction) {
            case LEFT: {
                Flubber.with()
                        .animation(Flubber.AnimationPreset.FADE_IN_RIGHT) // Slide up animation
                        .duration(300)
                        .createFor(summaryLayout)
                        .start();
                break;
            }
            case RIGHT: {
                Flubber.with()
                        .animation(Flubber.AnimationPreset.FADE_IN_LEFT) // Slide up animation
                        .duration(300)
                        .createFor(summaryLayout)
                        .start();
                break;
            }
            default:
                break;
        }
        displayWordData();
    }

    private void setStarStatus() {
        Bundle args = getArguments();
        final int wordId = (int) args.getLong(WORD_ID);
        WordManager.Word.saveIdOfLastAddedWord(getContext(), wordId);
        WordManager.Word entry = WordManager.getWordById(getContext(), wordId);
        if (entry.favourite == 1) {
            mStar.setImageDrawable(getDrawable(getActivity(), R.drawable.star_on));

        } else {
            mStar.setImageDrawable(getDrawable(getActivity(), R.drawable.star_off));
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        displayWordData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getShowsDialog()) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.fragment_word_summary, container, false);
    }
}
