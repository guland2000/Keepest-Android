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

package is.jacek.markowski.dictionary.keepest.main_activity.util;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;

import static com.google.android.gms.drive.Drive.SCOPE_FILE;


// google drive api v3
public class GDriveV3 {
    public static final int REQUEST_CODE_OPENER = 101;
    public static final int REQUEST_CODE_UPLOAD = 102;
    public static final int REQUEST_CODE_SIGN_IN_UPLOAD = 103;
    public static final int REQUEST_CODE_SIGN_IN_DOWNLOAD = 104;
    private MainActivity mActivity;

    public GDriveV3(FragmentActivity activity) {
        mActivity = (MainActivity) activity;
    }

    public void signIn(int requestCode) {
        if(!isSignedIn()) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(SCOPE_FILE)
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(mActivity, gso);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            mActivity.startActivityForResult(signInIntent, requestCode);
        }
    }

    public boolean isSignedIn() {
        return getAccount() != null;
    }

    private GoogleSignInAccount getAccount() {
        return GoogleSignIn.getLastSignedInAccount(mActivity);
    }


    public void showFileDialog() {
        if (isSignedIn()) {
            Toast.makeText(mActivity, mActivity.getString(R.string.connecting_to_gdrive), Toast.LENGTH_SHORT).show();
            OpenFileActivityOptions options = new OpenFileActivityOptions.Builder()
                    .setActivityTitle("*.keep or *.csv")
                    .build();
            DriveClient driveClient = Drive.getDriveClient(mActivity, getAccount());
            driveClient.newOpenFileActivityIntentSender(options).addOnSuccessListener(new OnSuccessListener<IntentSender>() {
                @Override
                public void onSuccess(IntentSender intentSender) {
                    try {
                        mActivity.startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void uploadFile(final String title) {
        final DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(mActivity, getAccount());
        final DriveClient driveClient = Drive.getDriveClient(mActivity, getAccount());

        driveResourceClient.getRootFolder().addOnCompleteListener(new OnCompleteListener<DriveFolder>() {
            @Override
            public void onComplete(@NonNull final Task<DriveFolder> taskRoot) {
                driveResourceClient.createContents().addOnCompleteListener(new OnCompleteListener<DriveContents>() {
                    @Override
                    public void onComplete(@NonNull Task<DriveContents> task) {
                        OutputStream outputStream = task.getResult().getOutputStream();
                        try {
                            FileInputStream inputStream = new FileInputStream(ImportExport.getNewDownloadFile(mActivity, null));
                            Files.copy(inputStream, outputStream);
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                outputStream.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(title)
                                .setMimeType("text/plain")
                                .setStarred(true).build();
                        CreateFileActivityOptions uploadFile = new CreateFileActivityOptions.Builder()
                                .setActivityTitle("Upload file")
                                .setInitialMetadata(changeSet)
                                .setInitialDriveContents(task.getResult())
                                .setActivityStartFolder(taskRoot.getResult().getDriveId())
                                .build();
                        Task<IntentSender> intent = driveClient.newCreateFileActivityIntentSender(uploadFile);
                        intent.addOnCompleteListener(new OnCompleteListener<IntentSender>() {
                            @Override
                            public void onComplete(@NonNull Task<IntentSender> task) {
                                IntentSender intentSender = task.getResult();
                                try {
                                    mActivity.startIntentSenderForResult(intentSender, REQUEST_CODE_UPLOAD, null, 0, 0, 0);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    }
                });
            }
        });
    }

    public void importFile(DriveId driveId) {
        final DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(mActivity, getAccount());
        driveResourceClient.openFile(driveId.asDriveFile(), DriveFile.MODE_READ_ONLY).addOnSuccessListener(new OnSuccessListener<DriveContents>() {
            @Override
            public void onSuccess(final DriveContents driveContents) {
                Task<Metadata> task = driveResourceClient.getMetadata(driveContents.getDriveId().asDriveFile());
                task.addOnCompleteListener(new OnCompleteListener<Metadata>() {
                    @Override
                    public void onComplete(@NonNull Task<Metadata> task) {
                        new DownloadFileAsyncTask(mActivity, driveResourceClient, driveContents, task.getResult().getTitle()).execute();
                    }
                });
            }
        });
    }

    private static class DownloadFileAsyncTask extends AsyncTask<Void, Void, Void> {
        MainActivity mActivity; // todo: refactor to get rid of this
        DriveContents mDriveContents;
        DriveResourceClient mDriveResourceClient;
        ProgressDialog mProgressDialog;
        File targetFile;

        DownloadFileAsyncTask(MainActivity activity, DriveResourceClient driveResourceClient, DriveContents driveContents, String fileName) {
            mActivity = activity;
            targetFile = new File(activity.getApplicationInfo().dataDir, fileName);
            mDriveContents = driveContents;
            mDriveResourceClient = driveResourceClient;
            mProgressDialog = ProgressDialog.show(
                    activity,
                    "",
                    activity.getString(R.string.importing_dictionaries_progress));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try (InputStream in = mDriveContents.getInputStream()) {
                OutputStream outputStream = new FileOutputStream(targetFile);
                Files.copy(in, outputStream);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mDriveResourceClient.discardContents(mDriveContents);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ImportExport.importJson(mActivity, targetFile.getName(), true);
            mProgressDialog.dismiss();
        }
    }
}

