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

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Set;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_TRANSLATION;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_WORD;


public class WordAdapterLearningSummary extends RecyclerView.Adapter<WordAdapterLearningSummary.WordsViewHolder> {
    private Cursor mCursor;
    private String[] mListOfMistakes;

    public WordAdapterLearningSummary(Cursor cursor, Context context) {
        mCursor = cursor;
        Set<String> set = new Preferences.LearningSummary().getSetOfId(context);
        mListOfMistakes = set.toArray(new String[set.size()]);
    }


    @Override
    public WordsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.row_item_recycle_view_learning_summary, parent, false);
        return new WordsViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final WordsViewHolder holder, final int position) {
        mCursor.moveToFirst();
        int idInDatabase = Integer.valueOf(mListOfMistakes[position]);
        for (int i = 0; i < mCursor.getCount(); i++) {
            mCursor.moveToPosition(i);
            int id = mCursor.getInt(mCursor.getColumnIndex(_ID));
            if (id == idInDatabase) {
                break;
            }
        }
        final String word = mCursor.getString(mCursor.getColumnIndex(COLUMN_WORD));
        final String translation = mCursor.getString(mCursor.getColumnIndex(COLUMN_TRANSLATION));
        holder.mWordTextView.setText(word);
        holder.mTranslationTextView.setText(translation);
    }

    @Override
    public int getItemCount() {
        if (mListOfMistakes != null) {
            return mListOfMistakes.length;
        } else {
            return 0;
        }
    }

    public Cursor swapData(Cursor cursor) {
        Cursor oldCursor = mCursor;
        mCursor = cursor;
        return oldCursor;
    }

    class WordsViewHolder extends RecyclerView.ViewHolder {
        private TextView mWordTextView;
        private TextView mTranslationTextView;

        WordsViewHolder(View itemView) {
            super(itemView);
            mWordTextView = itemView.findViewById(R.id.tv_word);
            mTranslationTextView = itemView.findViewById(R.id.tv_translation);
        }
    }
}
