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

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.ExportFilesystemDialogFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.ImportDialogFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Files;

import static is.jacek.markowski.dictionary.keepest.main_activity.util.Files.listDirs;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Files.readCurrentPath;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Files.saveCurrentPath;

/**
 * Created by jacek on 10.06.17.
 */

public class DirAdapter extends RecyclerView.Adapter<DirAdapter.ImportViewHolder> {
    private File[] mDirs;
    private FragmentActivity mActivity;
    private ImportAdapter mImportAdapter;

    public DirAdapter(File[] dirs, ImportAdapter adapter, FragmentActivity activity) {
        mDirs = dirs;
        mActivity = activity;
        mImportAdapter = adapter;
    }


    @Override
    public ImportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.row_item_recycle_view_dir, parent, false);
        return new ImportViewHolder(row);
    }

    @Override
    public void onBindViewHolder(final ImportViewHolder holder, int position) {
        final String directory = mDirs[position].getName();
        holder.mDirName.setText(directory);
        holder.mAbsolutePath = mDirs[position].getAbsolutePath();

        // open dir
        holder.mDirName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File dir = new File(readCurrentPath(mActivity));
                File newDir = new File(dir + "/" + directory);
                mDirs = listDirs(newDir);
                File[] files = Files.listFiles(newDir);
                saveCurrentPath(mActivity, newDir.getAbsolutePath());
                String tag = "exportDialog";
                DialogFragment fm = (DialogFragment) mActivity.getSupportFragmentManager().findFragmentByTag(tag);
                if (fm == null) {
                    tag = "importDialog";
                    fm = (DialogFragment) mActivity.getSupportFragmentManager().findFragmentByTag(tag);
                }
                if (tag.equals("importDialog")) {
                    ((ImportDialogFragment) fm).mDirTitle.setText(newDir.getAbsolutePath());
                } else {
                    ((ExportFilesystemDialogFragment) fm).mDirTitle.setText(newDir.getAbsolutePath());
                }
                if (mImportAdapter != null) {
                    mImportAdapter.changeData(files);
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mDirs != null) {
            return mDirs.length;
        } else {
            return 0;
        }
    }

    public void changeData(File[] dirs) {
        mDirs = dirs;
        notifyDataSetChanged();
    }

    public class ImportViewHolder extends RecyclerView.ViewHolder {
        private TextView mDirName;
        private String mAbsolutePath;

        public ImportViewHolder(View itemView) {
            super(itemView);
            mDirName = itemView.findViewById(R.id.tv_dir);
        }
    }
}
