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

import java.io.File;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.ImportDialogFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport;

import static is.jacek.markowski.dictionary.keepest.main_activity.util.Files.listFiles;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Files.readCurrentPath;

/**
 * Created by jacek on 10.06.17.
 */

public class ImportAdapter extends RecyclerView.Adapter<ImportAdapter.ImportViewHolder> {
    private File[] mFiles;
    private FragmentActivity mActivity;

    public ImportAdapter(File[] files, FragmentActivity activity) {
        mFiles = files;
        mActivity = activity;
    }


    @Override
    public ImportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.row_recyclerview_swipe_menu_import, parent, false);
        return new ImportViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final ImportViewHolder holder, int position) {
        final String filename = mFiles[position].getName();
        holder.mFileName.setText(filename);

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
                holder.mImportFile.startAnimation(animation_out);
            }

            @Override
            public void endMenuClosed(SwipeMenuLayout swipeMenuLayout) {
                super.endMenuClosed(swipeMenuLayout);
                holder.mImportFile.startAnimation(animation_in);
            }
        });

        // open menu
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mSml.smoothOpenEndMenu();
            }
        });

        // import file
        holder.mImportFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImportDialogFragment dialog = (ImportDialogFragment) mActivity.getSupportFragmentManager().findFragmentByTag(ImportDialogFragment.TAG);
                dialog.dismiss();
                ImportExport.importJson(mActivity, filename, false);
                holder.mSml.smoothCloseEndMenu();
            }
        });

        // delete file
        holder.mDeleteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File dir = new File(readCurrentPath(mActivity));
                File file = new File(dir, filename);
                file.delete();
                mFiles = listFiles(dir);
                notifyDataSetChanged();
                holder.mSml.smoothCloseEndMenu();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mFiles != null) {
            return mFiles.length;
        } else {
            return 0;
        }
    }

    public void changeData(File[] files) {
        mFiles = files;
        notifyDataSetChanged();
    }

    public class ImportViewHolder extends RecyclerView.ViewHolder {
        private TextView mFileName;
        private CardView mCardView;
        private ImageButton mImportFile;
        private ImageButton mDeleteFile;
        private SwipeHorizontalMenuLayout mSml;

        public ImportViewHolder(View itemView) {
            super(itemView);
            mFileName = itemView.findViewById(R.id.tv_filename);
            mCardView = itemView.findViewById(R.id.card_import);
            mImportFile = itemView.findViewById(R.id.img_import);
            mDeleteFile = itemView.findViewById(R.id.img_delete_file);
            mSml = itemView.findViewById(R.id.sml);


        }
    }
}
