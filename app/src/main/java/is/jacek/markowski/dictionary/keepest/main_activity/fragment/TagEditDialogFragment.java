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
import android.widget.TextView;
import android.widget.Toast;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Text;
import is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.COLUMN_TAG;


public class TagEditDialogFragment extends DialogFragment {
    public static final String TAG = TagEditDialogFragment.class.getName();
    private static final String TAG_NAME_KEY = "tagName";
    private static final String TAG_ID_KEY = "tagID";


    private TextView mTagEditText;

    public static TagEditDialogFragment newInstance(Long dictId,
                                                    String tagName) {
        Bundle args = new Bundle();
        args.putLong(TAG_ID_KEY, dictId);
        args.putString(TAG_NAME_KEY, tagName.substring(1));
        TagEditDialogFragment fragment = new TagEditDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        mTagEditText = getDialog().findViewById(R.id.ed_tag_edit_name);
        // show keyboard
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        Bundle bundle = getArguments();
        mTagEditText.setText(bundle.getString(TAG_NAME_KEY, ""));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final long tagId = args.getLong(TAG_ID_KEY, -1);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(R.layout.tag_edit_dialog);
        dialog.setTitle(R.string.edit_tag);
        dialog.setPositiveButton(R.string.save, null);
        final AlertDialog d = dialog.create();
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = mTagEditText.getText().toString();
                        if (Text.validate(getContext(), name)) {
                            ContentResolver resolver = getContext().getContentResolver();
                            ContentValues values = new ContentValues();
                            values.put(COLUMN_TAG, name);
                            String where = _ID + "=?";
                            String[] args = new String[]{Long.toString(tagId)};
                            Uri uri = UriHelper.TagsWord.buildUpdateTagUri();
                            int updated = resolver.update(uri, values, where, args);
                            WordAdvancedFragment f = (WordAdvancedFragment)
                                    getActivity().getSupportFragmentManager()
                                            .findFragmentByTag(WordAdvancedFragment.TAG);
                            TabTagsWordFragment tagFragment = (TabTagsWordFragment) f.mFragments.get(2);
                            tagFragment.restartLoader();
                            if (updated == 0) {
                                Toast.makeText(getContext(), R.string.tag_laready_exists, Toast.LENGTH_SHORT).show();
                            } else {
                                dialog.dismiss();
                            }
                        }
                    }
                });
            }
        });
        return d;
    }
}

