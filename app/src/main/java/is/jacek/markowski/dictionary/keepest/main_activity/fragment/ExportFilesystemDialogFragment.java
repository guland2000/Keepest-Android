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


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.DirAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Files;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Permissions;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Text;

import static is.jacek.markowski.dictionary.keepest.main_activity.util.Files.listDirs;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Files.readCurrentPath;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Files.saveCurrentPath;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.ExportJsonTask.TYPE_FILE;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.FILE_KEEP;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExportFilesystemDialogFragment extends DialogFragment {
    private static final String NAME_KEY = "name";
    private static final String FROM_KEY = "from";
    private static final String TO_KEY = "to";
    private static final String ID_KEY = "id";
    private static final String FILE_FORMAT = "format";
    public static String TAG = "exportDialog";
    public RecyclerView mRecyclerViewDirs;
    public TextView mDirTitle;
    private ImageButton mUpButton;
    private DirAdapter mAdapterDir;
    private EditText mFilename;

    public ExportFilesystemDialogFragment() {
        // Required empty public constructor
    }

    public static DialogFragment newInstance() {
        ExportFilesystemDialogFragment frag = new ExportFilesystemDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    public static DialogFragment newInstance(String name, String from, String to, int dictId, String format) {
        ExportFilesystemDialogFragment frag = new ExportFilesystemDialogFragment();
        Bundle args = new Bundle();
        args.putString(NAME_KEY, name);
        args.putString(FROM_KEY, from);
        args.putString(TO_KEY, to);
        args.putString(FILE_FORMAT, format);
        args.putInt(ID_KEY, dictId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onStart() {
        // after on create
        super.onStart();
        Bundle args = getArguments();
        final String name = args.getString(NAME_KEY, "backup");
        final String from = args.getString(FROM_KEY, "");
        final String to = args.getString(TO_KEY, "");

        mUpButton = getDialog().findViewById(R.id.bt_dir_parent);
        mDirTitle = getDialog().findViewById(R.id.tv_path_import);
        mDirTitle.setText(readCurrentPath(getContext()));
        mRecyclerViewDirs = getDialog().findViewById(R.id.rv_dirs);
        mFilename = getDialog().findViewById(R.id.ed_export_filename);
        mFilename.setText(name + "_" + from + "_" + to);

        File directory = new File(readCurrentPath(getContext()));
        // files in directory
        File[] dirs = listDirs(directory);

        mAdapterDir = new DirAdapter(dirs, null, getActivity());

        //dirs recycler
        mRecyclerViewDirs.setAdapter(mAdapterDir);
        mRecyclerViewDirs.setLayoutManager(new LinearLayoutManager(getActivity()));

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
                }
            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final String dict = args.getString(NAME_KEY);
        final String from = args.getString(FROM_KEY);
        final String to = args.getString(TO_KEY);
        final int dictId = args.getInt(ID_KEY, -1);
        final String format = args.getString(FILE_FORMAT, FILE_KEEP);

        // Inflate the layout for this fragment
        final Activity activity = getActivity();
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(R.layout.export_filesystem_dialog);
        dialog.setPositiveButton(R.string.save, null);
        final AlertDialog d = dialog.create();
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Permissions.askForPermissionsImportExport(getActivity());
                        // check and grant permission
                        if (Text.validate(getContext(), mFilename.getText().toString())) {
                            if (Permissions.getPermissionWriteStatus(activity) == PackageManager.PERMISSION_GRANTED) {
                                // create export file
                                String exportFileName = Text.shrinkText(mFilename.getText().toString());
                                if (dictId == -1) {
                                    Files.prepareJsonAll(getActivity(), exportFileName, TYPE_FILE);
                                } else {
                                    Files.prepareJsonOne(getActivity(), dictId, exportFileName, TYPE_FILE, format);
                                }
                            } else {
                                Toast.makeText(activity, getString(R.string.no_write_permission), Toast.LENGTH_LONG).show();
                            }
                            d.dismiss();
                        }
                    }
                });
            }
        });
        return d;
    }
}

