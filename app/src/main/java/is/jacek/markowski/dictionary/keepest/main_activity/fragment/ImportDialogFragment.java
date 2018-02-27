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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.DirAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.ImportAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Files;

import static is.jacek.markowski.dictionary.keepest.main_activity.util.Files.listDirs;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Files.listFiles;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Files.readCurrentPath;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Files.saveCurrentPath;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImportDialogFragment extends DialogFragment {
    public static final String TAG = "importDialog";

    public RecyclerView mRecyclerViewFiles;
    public RecyclerView mRecyclerViewDirs;
    public TextView mDirTitle;
    private ImageButton mUpButton;
    private DirAdapter mAdapterDir;
    private ImportAdapter mAdapterFiles;

    public ImportDialogFragment() {
        // Required empty public constructor
    }

    public static DialogFragment newInstance() {
        ImportDialogFragment frag = new ImportDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onStart() {
        // after on create
        super.onStart();
        mUpButton = getDialog().findViewById(R.id.bt_dir_parent);
        mDirTitle = getDialog().findViewById(R.id.tv_path_import);
        mDirTitle.setText(readCurrentPath(getContext()));
        mRecyclerViewFiles = getDialog().findViewById(R.id.rv_import_list);
        mRecyclerViewDirs = getDialog().findViewById(R.id.rv_dirs);

        File directory = new File(readCurrentPath(getContext()));
        // files in directory
        File[] files = listFiles(directory);
        File[] dirs = listDirs(directory);

        mAdapterFiles = new ImportAdapter(files, getActivity());
        mAdapterDir = new DirAdapter(dirs, mAdapterFiles, getActivity());

        //dirs recycler
        mRecyclerViewDirs.setAdapter(mAdapterDir);
        mRecyclerViewDirs.setLayoutManager(new LinearLayoutManager(getActivity()));

        // files recycler
        mRecyclerViewFiles.setAdapter(mAdapterFiles);
        mRecyclerViewFiles.setLayoutManager(new LinearLayoutManager(getActivity()));

        // up button
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentDir = readCurrentPath(getContext());
                if (!currentDir.equals("/")) {
                    File newDir = new File(new File(currentDir).getParent());
                    mDirTitle.setText(newDir.toString());
                    saveCurrentPath(getContext(), newDir.getAbsolutePath());
                    File[] dirs = listDirs(newDir);
                    mAdapterDir.changeData(dirs);
                    File[] files = Files.listFiles(newDir);
                    mAdapterFiles.changeData(files);
                }
            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(R.layout.import_dialog);
        dialog.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return dialog.create();
    }
}

