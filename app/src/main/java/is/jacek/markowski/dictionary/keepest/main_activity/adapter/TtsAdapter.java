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

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Set;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences;

public class TtsAdapter extends RecyclerView.Adapter<TtsAdapter.TtsViewHolder> {
    private FragmentActivity mActivity;
    private String[] mTtsArray;

    public TtsAdapter(FragmentActivity activity) {
        mActivity = activity;
        reloadList();
    }


    @Override
    public TtsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.row_item_tts, parent, false);
        return new TtsViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final TtsViewHolder holder, int position) {
        final String ttsString = mTtsArray[position];
        holder.mTextTts.setText(ttsString);
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences.TextToSpeech.addOrRemoveTtsSettings(mActivity, ttsString);
                reloadList();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mTtsArray != null) {
            return mTtsArray.length;
        } else {
            return 0;
        }
    }

    public void reloadList() {
        Set<String> ttsSet = Preferences.TextToSpeech.getSetOfTtsSettings(mActivity);
        mTtsArray = ttsSet.toArray(new String[ttsSet.size()]);
        notifyDataSetChanged();
    }

    class TtsViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextTts;
        private ImageButton mDelete;

        TtsViewHolder(View itemView) {
            super(itemView);
            mTextTts = itemView.findViewById(R.id.tv_tts);
            mDelete = itemView.findViewById(R.id.bt_delete_tts);
        }
    }
}
