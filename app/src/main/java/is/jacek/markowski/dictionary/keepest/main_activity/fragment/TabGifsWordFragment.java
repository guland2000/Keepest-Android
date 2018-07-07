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


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import is.jacek.markowski.dictionary.keepest.GoogleSearchActivity;
import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.GifChooserAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.WordAdvancedTabsAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.util.ApiKeys;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Connection;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Giphy;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Text;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static is.jacek.markowski.dictionary.keepest.GoogleSearchActivity.RESULT_IMAGE_FOUND;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.IMAGE_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.WORD_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Translation.translate;


public class TabGifsWordFragment extends Fragment implements WordAdvancedTabsAdapter.TabFragmentWord {
    public static final String TAG = "tagsTab";
    public View mRoot;
    private String mTitle = "GIF";
    private EditText mSearchGif;
    private ImageButton mGoogleSearch;
    public static final String EXTRA_MESSAGE = "is.jacek.markowski.dictionary.keepest.MESSAGE";
    public static final int REQUEST_IMAGE_CODE = 999;
    ImageView mLogo;
    ImageView mGifView;


    public TabGifsWordFragment() {
        // Required empty public constructor
    }

    public static TabGifsWordFragment newInstance(long wordId) {
        Bundle args = new Bundle();
        TabGifsWordFragment fragment = new TabGifsWordFragment();
        args.putLong("id", wordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.gif_chooser, container, false);
        long wordId = getArguments().getLong("id", 0);
        mRoot = root;
        mLogo = root.findViewById(R.id.img_logo);
        final RecyclerView recyclerView = root.findViewById(R.id.rv_gifs);
        mSearchGif = root.findViewById(R.id.ed_search_gif);
        mGoogleSearch = root.findViewById(R.id.ibt_google_search);
        mGoogleSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GoogleSearchActivity.class);
                String message = mSearchGif.getText().toString();
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivityForResult(intent, REQUEST_IMAGE_CODE);
            }
        });
        final GifChooserAdapter adapter = new GifChooserAdapter(this, null);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        final ImageView btSearch = root.findViewById(R.id.bt_gif_search);
        final ImageView gifView = root.findViewById(R.id.img_gif);
        WordManager.Word entry = WordManager.getWordById(getContext(), wordId);
        recyclerView.setVisibility(View.INVISIBLE);
        gifView.setVisibility(View.VISIBLE);
        mGifView = gifView;


        btSearch.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                String searchTerm = mSearchGif.getText().toString();
                Context context = getContext();
                if (context != null && Text.validate(context, searchTerm)) {
                    int limit = 10;
                    int offset = 0;
                    recyclerView.setVisibility(View.VISIBLE);
                    gifView.setVisibility(View.INVISIBLE);
                    adapter.mLimit = limit;
                    adapter.mOffset = offset;
                    adapter.mSearch = searchTerm;
                    adapter.swapData(null);
                    MainActivity activity = (MainActivity) getActivity();
                    activity.hideKeyboard();
                    if (Connection.isConnected(context)) {
                        Giphy.queryGifs(ApiKeys.getGiphyApiKey(getContext()), adapter, searchTerm, limit, offset);
                    }
                }
            }
        });
        String wordDraft = WordManager.WordEdit.getTextItem(getContext(), WORD_KEY);
        DictionaryManager.Dictionary d = DictionaryManager.getDictData(getContext());
        if (wordDraft.length() > 0) {
            translate(
                    d.speak_from,
                    "en",
                    wordDraft,
                    mSearchGif,
                    null,
                    null,
                    (MainActivity) getActivity());
        }


        gifView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getItemCount() > 0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    gifView.setVisibility(View.INVISIBLE);
                } else {
                    btSearch.callOnClick();
                    Giphy.setHorizontalLogo("giphy", mLogo);
                }
            }
        });

        if (entry.imageUrl.length() > 0) {
            Giphy.displayGif(getActivity(), entry.imageUrl, gifView);
            Giphy.setHorizontalLogo(entry.imageUrl, mLogo);
        }
        String draftImage = WordManager.WordEdit.getTextItem(getContext(), IMAGE_KEY);
        if (draftImage.length() > 0) {
            Giphy.displayGif(getActivity(), draftImage, gifView);
            Giphy.setHorizontalLogo(draftImage, mLogo);

        } else {
            WordManager.WordEdit.saveTextItem(getContext(), IMAGE_KEY, entry.imageUrl);
        }

        if (draftImage.length() == 0 && entry.imageUrl.length() == 0) {
            Giphy.setHorizontalLogo("giphy", mLogo);
        }
        return root;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_IMAGE_FOUND) {
            String draftImage = data.getStringExtra("url");
            Giphy.displayGif(getActivity(), draftImage, mGifView);
            Giphy.setHorizontalLogo(draftImage, mLogo);
        }
    }
}
