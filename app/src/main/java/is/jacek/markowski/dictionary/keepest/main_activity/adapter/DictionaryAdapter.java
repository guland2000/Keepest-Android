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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tubb.smrv.SwipeHorizontalMenuLayout;
import com.tubb.smrv.SwipeMenuLayout;
import com.tubb.smrv.listener.SimpleSwipeSwitchListener;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.DictionaryDialogFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.ExportMethodDialogFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Language;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Message;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_FROM;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_NAME;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.Entry.COLUMN_DICTIONARY_TO;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_DICTIONARY_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager.getDictId;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager.saveDictData;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper.Dictionary.buildDictUri;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper.Word.buildWordsAllUri;

/**
 * Created by jacek on 10.06.17.
 */

public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.DictionaryViewHolder> {
    private Cursor mCursor;
    private FragmentActivity mActivity;

    public DictionaryAdapter(Cursor cursor, FragmentActivity activity) {
        mCursor = cursor;
        mActivity = activity;
    }


    @Override
    public DictionaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.row_recyclerview_swipe_menu_dictionary, parent, false);
        return new DictionaryAdapter.DictionaryViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final DictionaryViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        final String dict = mCursor.getString(mCursor.getColumnIndex(COLUMN_DICTIONARY_NAME));
        final String from = mCursor.getString(mCursor.getColumnIndex(COLUMN_DICTIONARY_FROM));
        final String to = mCursor.getString(mCursor.getColumnIndex(COLUMN_DICTIONARY_TO));
        long idInDatabase = mCursor.getLong(mCursor.getColumnIndex(_ID));
        int wordCount = DictionaryManager.getWordCount(mActivity, idInDatabase);
        // close previously opened items
        holder.mSml.smoothCloseEndMenu();
        holder.idInDatabase = idInDatabase;
        holder.mDictTextView.setText(dict + " {" + wordCount + "}");
        holder.mFromTextView.setText(Language.getCountryName(mActivity, from));
        holder.mToTextView.setText(Language.getCountryName(mActivity, to));
        holder.mDictNameHidden.setText(dict);
        holder.mDictNameHidden.setVisibility(View.INVISIBLE);

        // check if dictionary is just imported
        final boolean isDictNew = !DictionaryManager.isInSetOfOpenedEntries(mActivity, holder.idInDatabase);
        if (isDictNew) {
            holder.mIsDictNewTextView.setVisibility(View.VISIBLE);
        } else {
            holder.mIsDictNewTextView.setVisibility(View.INVISIBLE);
        }

        // highlight current open dictionary
        long currentDictId = getDictId(mActivity);
        if (holder.idInDatabase == currentDictId) {
            holder.mCardView.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.cardHighlightColor));
        } else {
            holder.mCardView.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.cardDefaultColor));
        }

        // swipe menu listener
        holder.mSml.setSwipeListener(new SimpleSwipeSwitchListener() {

            Animation animation_in;
            Animation animation_out;

            {
                animation_in = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in);
                animation_out = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_out);
            }

            @Override
            public void endMenuOpened(SwipeMenuLayout swipeMenuLayout) {
                super.endMenuOpened(swipeMenuLayout);
                holder.mDictNameHidden.startAnimation(animation_in);
                holder.mDictNameHidden.setVisibility(View.VISIBLE);
            }

            @Override
            public void endMenuClosed(SwipeMenuLayout swipeMenuLayout) {
                super.endMenuClosed(swipeMenuLayout);
                holder.mDictNameHidden.startAnimation(animation_out);
            }
        });
        final String where = _ID + "=?";
        final String[] selectionArgs = new String[]{Long.toString(holder.idInDatabase)};


        // delete dictionary
        holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
                dialog.setTitle(mActivity.getString(R.string.confirm_deletion_of) + dict);
                dialog.setPositiveButton(R.string.dict_delete_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        v.getContext().getContentResolver().delete(buildDictUri(), where, selectionArgs);
                        v.getContext().getContentResolver().delete(buildWordsAllUri(), COLUMN_DICTIONARY_ID + "=?", selectionArgs);
                        // open another dictionary if exist
                        DictionaryManager.openRandomDictionary(mActivity);
                    }
                });
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                if (!isDictNew) {
                    DictionaryManager.removeFromSetOfOpenedDictionaries(mActivity, holder.idInDatabase);
                }
                holder.mSml.smoothCloseEndMenu();
            }
        });

        // load dictionary
//        holder.mButtonOpen.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // save id of selected dictionary in sheraed preferences file
//                // words fragment reads this value to load proper dictionary
//                saveDictData(mActivity, holder.idInDatabase);
//                Toast.makeText(mActivity, mActivity.getString(R.string.dictionary) + dict +
//                        mActivity.getString(R.string.loaded), Toast.LENGTH_LONG).show();
//
//                if (isDictNew) {
//                    DictionaryManager.setDictionaryAsOpened(mActivity, holder.idInDatabase);
//                }
//                ((MainActivity) mActivity).commitWordsFragment();
//            }
//        });

        // menu after click
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save id of selected dictionary in shared preferences file
                // words fragment reads this value to load proper dictionary
                saveDictData(mActivity, holder.idInDatabase);
//                Toast.makeText(mActivity, mActivity.getString(R.string.dictionary) + dict +
//                        mActivity.getString(R.string.loaded), Toast.LENGTH_LONG).show();

                if (isDictNew) {
                    DictionaryManager.setDictionaryAsOpened(mActivity, holder.idInDatabase);
                }
                ((MainActivity) mActivity).commitWordsFragment();
            }
        });
        holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Message.showToast(mActivity, mActivity.getString(R.string.swipe_left_to_open_menu));
                return true;
            }
        });

        // edit dictionary/list
        holder.mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DictionaryDialogFragment dialog = DictionaryDialogFragment.newInstance(DictionaryDialogFragment.EDIT_MODE,
                        holder.idInDatabase, dict, from, to);
                FragmentManager fm = mActivity.getSupportFragmentManager();
                dialog.show(fm, "this");
                holder.mSml.smoothCloseEndMenu();
            }
        });
        // export dictionary
        holder.mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = ExportMethodDialogFragment.newInstance(dict, from, to, (int) holder.idInDatabase);
                dialogFragment.show(mActivity.getSupportFragmentManager(), "exportMethodDialog");
                holder.mSml.smoothCloseEndMenu();
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

    public class DictionaryViewHolder extends RecyclerView.ViewHolder {
        public long idInDatabase;
        private TextView mDictTextView;
        private TextView mFromTextView;
        private TextView mToTextView;
        private TextView mDictNameHidden;
        private TextView mIsDictNewTextView;
        private ImageButton mDeleteButton;
        private ImageButton mExportButton;
        private ImageButton mEditButton;
        private SwipeHorizontalMenuLayout mSml;
        private CardView mCardView;

        public DictionaryViewHolder(View itemView) {
            super(itemView);
            mDictTextView = itemView.findViewById(R.id.tv_dictionary);
            mFromTextView = itemView.findViewById(R.id.tv_speak_from);
            mToTextView = itemView.findViewById(R.id.tv_speak_to);
            mDeleteButton = itemView.findViewById(R.id.img_delete);
            mExportButton = itemView.findViewById(R.id.img_export);
            mEditButton = itemView.findViewById(R.id.img_list_edit);
            mSml = itemView.findViewById(R.id.sml);
            mCardView = itemView.findViewById(R.id.card_dictionary);
            mDictNameHidden = itemView.findViewById(R.id.tv_dict_name_hidden);
            mIsDictNewTextView = itemView.findViewById(R.id.tv_is_dict_new);
        }
    }
}
