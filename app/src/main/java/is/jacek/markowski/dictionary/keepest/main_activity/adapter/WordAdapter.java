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

package is.jacek.markowski.dictionary.keepest.main_activity.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.appolica.flubber.Flubber;
import com.tubb.smrv.SwipeHorizontalMenuLayout;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.database.Contract;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordSummaryFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Connection;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Message;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences;
import is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.WORD_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_FAVOURITE;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_IMAGE;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_TRANSLATION;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_WORD;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordFragment.TAG;


public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordsViewHolder> {
    private Cursor mCursor;
    private FragmentActivity mActivity;
    private String mSortMode;

    public WordAdapter(Cursor cursor, FragmentActivity activity, String sortMode) {
        mCursor = cursor;
        mActivity = activity;
        mSortMode = sortMode;
    }


    @Override
    public WordsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.row_recyclerview_swipe_menu_word, parent, false);
        return new WordsViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final WordsViewHolder holder, final int position) {
        mCursor.moveToPosition(position);
        holder.idInDatabase = mCursor.getInt(mCursor.getColumnIndex(_ID)); // holds id of the row in db
        String image = mCursor.getString(mCursor.getColumnIndex(COLUMN_IMAGE));
        // close previously opened items
        holder.mSml.smoothCloseEndMenu();
        boolean isFavourite = mCursor.getInt(mCursor.getColumnIndex(COLUMN_FAVOURITE)) != 0;
        StringBuilder builder = new StringBuilder();
        String[] splitTags = WordManager.Tags.prepareStringWithAllTags(mActivity, holder.idInDatabase).split("\\s+");
        for (String tag : splitTags) {
            if (tag.length() > 0) {
                builder.append("#" + tag + " ");
            }
        }

        String allTags = builder.toString();
        if (image.length() > 5) {
            allTags = "✔️" + allTags; // image
        } else {
            allTags = "✖️" + allTags; // no image
        }
        if (allTags.length() == 0)

        {
            holder.mTags.setText(allTags);
        } else

        {
            holder.mTags.setText(allTags);
        }
        // animate last added word
        if (WordManager.Word.getIdOfLastAddedWord(mActivity) == holder.idInDatabase)

        {

            Flubber.with().animation(Flubber.AnimationPreset.FADE_IN_RIGHT)
                    .animation(Flubber.AnimationPreset.FADE_IN_RIGHT) // Slide up animation
                    .repeatCount(0)                              // Repeat once
                    .duration(900)                              // Last for 1000 milliseconds(1 second)
                    .createFor(holder.mCardView)                             // Apply it to the view
                    .start();
            WordManager.Word.clearIdOfLastAddedWord(mActivity);
        }
        if (isFavourite)

        {
            holder.mCardView.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.cardHighlightColor));

        } else

        {
            holder.mCardView.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.cardDefaultColor));
        }

        final String word = mCursor.getString(mCursor.getColumnIndex(COLUMN_WORD));
        holder.mWordHidden.setText(word);
        final String translation = mCursor.getString(mCursor.getColumnIndex(COLUMN_TRANSLATION));
        holder.mWordTextView.setText(word);
        holder.mTranslationTextView.setText(translation);


        // uri
        final Uri uri = UriHelper.Word.buildWordsAllUri();
        // query parameters
        final String where = _ID + "=?";
        final String[] selectionArgs = new String[]{Integer.toString(holder.idInDatabase)};

        // delete action
        holder.mDelete.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(final View v) {
                // remember position
                AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
                dialog.setTitle(mActivity.getString(R.string.confirm_deletion_of) + word);
                dialog.setPositiveButton(R.string.dict_delete_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        v.getContext().getContentResolver().delete(uri, where, selectionArgs);
                        if (position > 1 && mCursor.getCount() > 1) {
                            mCursor.moveToPosition(position - 1);
                            int wordId = mCursor.getInt(mCursor.getColumnIndex(_ID));
                            WordManager.Word.saveIdOfLastAddedWord(mActivity, wordId);
                            mCursor.moveToPosition(position);
                        }
                        // delete records from tag relations db
                        v.getContext().getContentResolver().delete(UriHelper.TagsWord.buildDeleteTagFromWordUri(),
                                WORD_ID + "=?",
                                new String[]{Integer.toString(holder.idInDatabase)});
                    }
                });
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                holder.mSml.smoothCloseEndMenu();
            }
        });

        // cut action
        holder.mCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                WordManager.Word.saveIdOfWordToPaste(mActivity, holder.idInDatabase);
                WordManager.Word.setWordOperationType(mActivity, Preferences.Word.CUT_WORD);
                Message.showToast(mActivity, mActivity.getString(R.string.cut_word));
                holder.mSml.smoothCloseEndMenu();
            }
        });

        // copy action
        holder.mCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                WordManager.Word.saveIdOfWordToPaste(mActivity, holder.idInDatabase);
                WordManager.Word.setWordOperationType(mActivity, Preferences.Word.COPY_WORD);
                Message.showToast(mActivity, mActivity.getString(R.string.copy_word));
                holder.mSml.smoothCloseEndMenu();
            }
        });

        // play sound
        holder.mSound.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                boolean isConnected = Connection.isConnected(mActivity);

                WordFragment fragment = (WordFragment)
                        mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
                if (!fragment.ttsManager.isPlaying()) {
                    DictionaryManager.Dictionary dict = DictionaryManager.getDictData(mActivity);
                    fragment.ttsManager.onlineTts(word, dict.speak_from, translation, dict.speak_to, isConnected);
                }
                Flubber.with()
                        .animation(Flubber.AnimationPreset.ZOOM_IN) // Slide up animation
                        .duration(500)
                        .createFor(holder.mSound)
                        .start();
            }
        });


        // menu on touch event
        holder.mCardView.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                WordSummaryFragment dialog = WordSummaryFragment.newInstance(mActivity, holder.idInDatabase);
                FragmentManager fm = mActivity.getSupportFragmentManager();
                dialog.show(fm, WordSummaryFragment.TAG);
            }
        });
        holder.mCardView.setOnLongClickListener(new View.OnLongClickListener()

        {
            @Override
            public boolean onLongClick(View v) {
                Message.showToast(mActivity, mActivity.getString(R.string.swipe_left_to_open_menu));
                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public Cursor swapData(Cursor cursor) {
        Cursor oldCursor = mCursor;
        mCursor = cursor;
        return oldCursor;
    }

    public int findPositionOfWord(long wordId) {
        for (int i = 0; i < mCursor.getCount(); i++) {
            mCursor.moveToPosition(i);
            int x = mCursor.getPosition();
            int idInDatabase = mCursor.getInt(mCursor.getColumnIndex(_ID));
            if (wordId == idInDatabase) {
                return i;
            }
        }
        return 0;
    }

    public long getIdOnPosition(int position) {
        if (position >= getItemCount()) {
            position = 0;
        } else if (position < 0) {
            position = getItemCount() - 1;
        }
        mCursor.moveToPosition(position);
        long id = mCursor.getLong(mCursor.getColumnIndex(Contract.Word.Entry._ID));
        return id;
    }

    public int getPosition() {
        return mCursor.getPosition();
    }

    public class WordsViewHolder extends RecyclerView.ViewHolder {
        public int idInDatabase;
        private TextView mWordTextView;
        private TextView mTranslationTextView;
        private ImageButton mDelete;
        private ImageButton mCopy;
        private ImageButton mCut;
        private CardView mCardView;
        private SwipeHorizontalMenuLayout mSml;
        private ImageButton mSound;
        private TextView mWordHidden;
        private TextView mTags;

        WordsViewHolder(View itemView) {
            super(itemView);
            mSml = itemView.findViewById(R.id.sml);
            mWordTextView = itemView.findViewById(R.id.tv_word);
            mTranslationTextView = itemView.findViewById(R.id.tv_translation);
            mCardView = itemView.findViewById(R.id.card_word);
            mDelete = itemView.findViewById(R.id.img_delete);
            mCopy = itemView.findViewById(R.id.img_copy);
            mCut = itemView.findViewById(R.id.img_cut);
            mSound = itemView.findViewById(R.id.img_sound);
            mWordHidden = itemView.findViewById(R.id.tv_word_hidden);
            mTags = itemView.findViewById(R.id.tv_tags);

        }
    }
}
