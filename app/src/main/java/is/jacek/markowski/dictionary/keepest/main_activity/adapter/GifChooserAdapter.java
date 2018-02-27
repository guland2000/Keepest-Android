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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.TabGifsWordFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Connection;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Giphy;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.IMAGE_KEY;

/**
 * Created by jacek on 09.10.17.
 */

public class GifChooserAdapter extends RecyclerView.Adapter<GifChooserAdapter.GifViewHolder> {
    private static final String TAG = GifChooserAdapter.class.getName();
    public String mSearch;
    public int mLimit = 10;
    public int mOffset = 0;
    private TabGifsWordFragment fragment;
    private ArrayList<String> data;

    public GifChooserAdapter(TabGifsWordFragment fragment, ArrayList<String> data) {
        this.fragment = fragment;
        this.data = data;
    }

    @Override
    public GifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.row_item_recycle_view_gif_chooser, parent, false);
        return new GifViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final GifViewHolder holder, final int position) {
        if (getItemCount() - 2 <= position) {
            mOffset += 10;
            if (mOffset < 50 && Connection.isConnected(fragment.getContext())) {
                Giphy.queryGifs(fragment.getString(R.string.giphy_api), this, mSearch, mLimit, mOffset);
            }
        }
        final String gifUrl = data.get(position);
        Giphy.displayGif(fragment.getActivity(), gifUrl, holder.mImgGifDisplay);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.mRoot.findViewById(R.id.rv_gifs).setVisibility(View.INVISIBLE);
                ImageView imageView = fragment.mRoot.findViewById(R.id.img_gif);
                imageView.setVisibility(View.VISIBLE);
                Giphy.displayGif(fragment.getActivity(), gifUrl, imageView);
                // save url in preferences
                WordManager.WordEdit.saveTextItem(fragment.getContext(), IMAGE_KEY, data.get(position));

            }
        });

    }

    public void swapData(ArrayList<String> newData) {
        if (data != null && newData != null) {
            data.addAll(newData);
        } else {
            data = newData;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    public class GifViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImgGifDisplay;

        public GifViewHolder(View itemView) {
            super(itemView);
            mImgGifDisplay = itemView.findViewById(R.id.img_gif_display);
        }
    }
}
