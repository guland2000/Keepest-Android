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

import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.TabTagsWordFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.TagEditDialogFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordAdvancedFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.COLUMN_TAG;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.WORD_ID;

/**
 * Created by jacek on 10.06.17.
 */

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {
    private static final String TAG = TagAdapter.class.getName();
    private Cursor mCursor;
    private FragmentActivity mActivity;

    public TagAdapter(Cursor cursor, FragmentActivity activity) {
        mCursor = cursor;
        mActivity = activity;
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.row_item_recycle_view_tag, parent, false);
        return new TagViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final TagViewHolder holder, final int position) {
        mCursor.moveToPosition(position);
        String name = mCursor.getString(mCursor.getColumnIndex(COLUMN_TAG));
        boolean isChecked = true;

        if (mCursor.isNull(mCursor.getColumnIndex(WORD_ID))) {
            isChecked = false;
        }

        holder.idInDb = mCursor.getInt(mCursor.getColumnIndex(_ID));
        // changes made by user not yet saved in db
        if (WordManager.WordEdit.isTagIdInSet(mActivity, holder.idInDb)) {
            isChecked = !isChecked;
        }

        holder.mCheckBoxTag.setChecked(isChecked);
        holder.mCheckBoxTag.setText("#" + name);
        holder.mCheckBoxTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WordManager.WordEdit.addOrRemoveTagIdFromSet(mActivity, holder.idInDb);

            }
        });

        //delete tag
        holder.mBtDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Uri uri = UriHelper.TagsWord.buildDeleteTagUri();
                final String where = _ID + "=?";
                final String[] selectionArgs = new String[]{Integer.toString(holder.idInDb)};
                AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
                dialog.setTitle(mActivity.getString(R.string.confirm_deletion_of) + holder.mCheckBoxTag.getText().toString());
                dialog.setPositiveButton(R.string.dict_delete_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        v.getContext().getContentResolver().delete(uri, where, selectionArgs);
                        WordAdvancedFragment f = (WordAdvancedFragment)
                                mActivity.getSupportFragmentManager()
                                        .findFragmentByTag(WordAdvancedFragment.TAG);

                        TabTagsWordFragment tagFragment = (TabTagsWordFragment) f.mFragments.get(2);
                        WordManager.WordEdit.addOrRemoveTagIdFromSet(mActivity, holder.idInDb);
                        tagFragment.restartLoader();
                    }
                });
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });


        //edit tag
        holder.mBtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagEditDialogFragment dialog = TagEditDialogFragment
                        .newInstance((long) holder.idInDb, holder.mCheckBoxTag.getText().toString());
                FragmentManager fm = mActivity.getSupportFragmentManager();
                dialog.show(fm, TagEditDialogFragment.TAG);
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

    public class TagViewHolder extends RecyclerView.ViewHolder {
        private CheckBox mCheckBoxTag;
        private int idInDb;
        private ImageButton mBtDelete;
        private ImageButton mBtEdit;

        public TagViewHolder(View itemView) {
            super(itemView);
            mCheckBoxTag = itemView.findViewById(R.id.checkbox_tag);
            mBtDelete = itemView.findViewById(R.id.bt_tag_delete);
            mBtEdit = itemView.findViewById(R.id.bt_tag_edit);
        }
    }
}
