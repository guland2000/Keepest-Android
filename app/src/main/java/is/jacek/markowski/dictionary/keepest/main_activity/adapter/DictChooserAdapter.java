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

import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_NAME;

/**
 * Created by jacek on 10.06.17.
 */

public class DictChooserAdapter extends RecyclerView.Adapter<DictChooserAdapter.DictViewHolder> {
    private static final String TAG = DictChooserAdapter.class.getName();
    private Cursor mCursor;
    private FragmentActivity mActivity;

    public DictChooserAdapter(Cursor cursor, FragmentActivity activity) {
        mCursor = cursor;
        mActivity = activity;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public DictViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.row_item_recycle_view_dictionary_chooser, parent, false);
        return new DictViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final DictViewHolder holder, final int position) {
        mCursor.moveToPosition(position);
        String name = mCursor.getString(mCursor.getColumnIndex(COLUMN_DICTIONARY_NAME));
        boolean isChecked = false;
        holder.idInDb = mCursor.getInt(mCursor.getColumnIndex(_ID));
        // changes made by user not yet saved in db
        if (DictionaryManager.DictChooser.isTagIdInSet(mActivity, holder.idInDb)) {
            isChecked = !isChecked;
        }

        holder.mCheckBoxTag.setChecked(isChecked);
        holder.mCheckBoxTag.setText(name);
        holder.mCheckBoxTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DictionaryManager.DictChooser.addOrRemoveTagIdFromSet(mActivity, holder.idInDb);

            }
        });
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    public Cursor swapData(Cursor cursor) {
        Cursor oldCursor = mCursor;
        mCursor = cursor;
        return oldCursor;
    }

    public class DictViewHolder extends RecyclerView.ViewHolder {
        private CheckBox mCheckBoxTag;
        private CardView mCardView;
        private int idInDb;

        public DictViewHolder(View itemView) {
            super(itemView);
            mCheckBoxTag = itemView.findViewById(R.id.checkbox_tag);
            mCardView = itemView.findViewById(R.id.card_dict_chooser);
        }
    }
}
