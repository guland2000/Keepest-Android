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
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Files;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Permissions;

import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.ExportJsonTask.TYPE_MAIL;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.FILE_CSV;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.FILE_KEEP;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Permissions.arePermissionReadWriteGranted;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExportMethodDialogFragment extends DialogFragment {

    private static final String NAME_KEY = "name";
    private static final String FROM_KEY = "from";
    private static final String TO_KEY = "to";
    private static final String ID_KEY = "id";

    private Button mButtonSaveFile;
    private Button mButtonMail;
    private ToggleButton mToggleFormat;

    public ExportMethodDialogFragment() {
        // Required empty public constructor
    }

    public static DialogFragment newInstance() {
        ExportMethodDialogFragment frag = new ExportMethodDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    public static DialogFragment newInstance(String name, String from, String to, int dictId) {
        ExportMethodDialogFragment frag = new ExportMethodDialogFragment();
        Bundle args = new Bundle();
        args.putString(NAME_KEY, name);
        args.putString(FROM_KEY, from);
        args.putString(TO_KEY, to);
        args.putInt(ID_KEY, dictId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onStart() {
        // after on create
        super.onStart();
        Bundle args = getArguments();
        final String dict = args.getString(NAME_KEY);
        final String from = args.getString(FROM_KEY);
        final String to = args.getString(TO_KEY);
        final int dictId = args.getInt(ID_KEY, -1);

        // save file
        mButtonSaveFile = getDialog().findViewById(R.id.bt_save_file);
        mToggleFormat = getDialog().findViewById(R.id.toggle_file_format);
        // export all lists only to keep format
        if (dictId == -1) {
            mToggleFormat.setEnabled(false);
        }
        mButtonSaveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arePermissionReadWriteGranted(getActivity())) {
                    String format = FILE_KEEP;
                    if (!mToggleFormat.isChecked() && mToggleFormat.isEnabled()) {
                        format = FILE_CSV;
                    }
                    DialogFragment dialogFragment;
                    if (dictId == -1) {
                        dialogFragment = ExportFilesystemDialogFragment.newInstance();
                    } else {
                        dialogFragment = ExportFilesystemDialogFragment.newInstance(dict, from, to, dictId, format);
                    }
                    dialogFragment.show(getActivity().getSupportFragmentManager(), ExportFilesystemDialogFragment.TAG);
                    getDialog().dismiss();
                } else {
                    Permissions.askForPermissionsImportExport(getActivity());
                }
            }
        });

        // send mail
        mButtonMail = getDialog().findViewById(R.id.bt_send_mail);
        mButtonMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arePermissionReadWriteGranted(getActivity())) {
                    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                        // check permissions
                        String format = FILE_KEEP;
                        if (!mToggleFormat.isChecked() && mToggleFormat.isEnabled()) {
                            format = FILE_CSV;
                        }
                        String exportFileName = dict + "_" + getString(R.string.from) + "_" + to;
                        if (dictId == -1) {
                            exportFileName = getString(R.string.backup_keep);
                            Files.prepareJsonAll(getActivity(), exportFileName, TYPE_MAIL);
                        } else {
                            Files.prepareJsonOne(getActivity(), dictId, exportFileName, TYPE_MAIL, format);
                        }
                        getDialog().dismiss();
                    }
                } else {
                    Permissions.askForPermissionsImportExport(getActivity());
                }
            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(R.layout.export_method_dialog);
        dialog.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return dialog.create();
    }
}

