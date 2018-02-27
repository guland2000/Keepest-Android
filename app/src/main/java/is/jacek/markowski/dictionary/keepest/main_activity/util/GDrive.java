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
import android.content.Context;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;

import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.ExportJsonTask.TYPE_CLOUD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Message.showSnack;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Message.showToast;

/**
 * Created by jacek on 10/4/17.
 */
public class GDrive implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final int REQUEST_CODE_OPENER = 102;
    public static final int MODE_UPLOAD = 0;
    public static final int MODE_DOWNLOAD = 1;
    private static final int REQUEST_CODE_RESOLUTION = 101;
    private static int mMode = 0;
    private MainActivity mActivity;
    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    public GDrive(FragmentActivity activity, int mode) {
        mActivity = (MainActivity) activity;
        mMode = mode;
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        mGoogleApiClient.disconnect();
    }

    public void importFile(DriveId driveId) {
        Drive.DriveApi.fetchDriveId(mGoogleApiClient, driveId.getResourceId())
                .setResultCallback(new ResultCallback<DriveApi.DriveIdResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveIdResult driveIdResult) {
                        new RetrieveDriveFileContentsAsyncTask(mActivity).execute(driveIdResult.getDriveId());
                    }
                });
    }

    public void uploadFile(final String title) {
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull final DriveApi.DriveContentsResult driveContentsResult) {
                        if (!driveContentsResult.getStatus().isSuccess()) {
                            showToast(mActivity, mActivity.getString(R.string.gdrive_error_create_file));
                            return;
                        }
                        final DriveContents driveContents = driveContentsResult.getDriveContents();
                        // Perform I/O off the UI thread.
                        new Thread() {
                            @Override
                            public void run() {
                                // write content to DriveContents
                                OutputStream outputStream = driveContents.getOutputStream();
                                try {
                                    FileInputStream inputStream = new FileInputStream(ImportExport.getNewDownloadFile(mActivity, null));
                                    Files.copy(inputStream, outputStream);
                                    inputStream.close();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    //showToast(mActivity, e.getLocalizedMessage());
                                } finally {
                                    try {
                                        outputStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }

                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                        .setTitle(title)
                                        .setMimeType("text/plain") //application/octet-stream
                                        .setStarred(true).build();

                                // create a file on root folder
                                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                        .createFile(mGoogleApiClient, changeSet, driveContents)
                                        .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                            @Override
                                            public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                                if (!driveContentsResult.getStatus().isSuccess()) {
                                                    showToast(mActivity, mActivity.getString(R.string.gdrive_error_create_file));
                                                    return;
                                                }
                                                showSnack(mActivity.getCurrentFocus(), mActivity.getString(R.string.gdrive_upload_success) + title);
                                            }
                                        });
                            }
                        }.start();
                    }
                });


    }

    public void showFileDialog() {
        if (mGoogleApiClient.isConnected()) {
            IntentSender intentSender = Drive.DriveApi
                    .newOpenFileActivityBuilder()
                    .build(mGoogleApiClient);
            try {
                mActivity.startIntentSenderForResult(
                        intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Message.makeText(mActivity, "Connection Success", Message.LENGTH_SHORT).show();
        switch (mMode) {
            case MODE_UPLOAD: {
                Files.prepareJsonAll(mActivity, ImportExport.getNewDownloadFile(mActivity, null).getName(), TYPE_CLOUD);
                break;
            }
            case MODE_DOWNLOAD: {
                mActivity.mGdrive.showFileDialog();
                break;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Toast.makeText(mActivity, connectionResult.toString(), Toast.LENGTH_SHORT).show();
        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(mActivity, connectionResult.getErrorCode(), 0).show();
            return;
        }
        try {
            connectionResult.startResolutionForResult(mActivity, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
        }

    }


    final private class RetrieveDriveFileContentsAsyncTask
            extends ApiClientAsyncTask<DriveId, Boolean, String> {

        public RetrieveDriveFileContentsAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackgroundConnected(DriveId... params) {
            DriveFile file = params[0].asDriveFile();
            DriveApi.DriveContentsResult driveContentsResult =
                    file.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                return null;
            }
            String filename = null;
            DriveResource.MetadataResult mdRslt = file.getMetadata(mGoogleApiClient).await();
            if (mdRslt != null && mdRslt.getStatus().isSuccess()) {
                filename = mdRslt.getMetadata().getTitle();
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();
            InputStream in = driveContents.getInputStream();
            try {
                OutputStream outputStream = new FileOutputStream(ImportExport.getNewDownloadFile(mActivity, filename));
                Files.copy(in, outputStream);
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            driveContents.discard(getGoogleApiClient());
            return filename;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ImportExport.importJson(mActivity, ImportExport.getNewDownloadFile(mActivity, result).getName(), true);
            mProgressDialog.dismiss();
        }
    }

    /**
     * An AsyncTask that maintains a connected client.
     */
    public abstract class ApiClientAsyncTask<Params, Progress, Result>
            extends AsyncTask<Params, Progress, Result> {

        ProgressDialog mProgressDialog;

        private GoogleApiClient mClient;

        public ApiClientAsyncTask(Context context) {
            GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE);
            mClient = builder.build();
            mProgressDialog = ProgressDialog.show(mActivity, "", mActivity.getString(R.string.importing_dictionaries_progress));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.show();
        }

        @Override
        protected final Result doInBackground(Params... params) {
            Log.d("TAG", "in background");
            final CountDownLatch latch = new CountDownLatch(1);
            mClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnectionSuspended(int cause) {
                }

                @Override
                public void onConnected(Bundle arg0) {
                    latch.countDown();
                }
            });
            mClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult arg0) {
                    latch.countDown();
                }
            });
            mClient.connect();
            try {
                latch.await();
            } catch (InterruptedException e) {
                return null;
            }
            if (!mClient.isConnected()) {
                return null;
            }
            try {
                return doInBackgroundConnected(params);
            } finally {
                mClient.disconnect();
            }
        }

        /**
         * Override this method to perform a computation on a background thread, while the client is
         * connected.
         */
        protected abstract Result doInBackgroundConnected(Params... params);

        /**
         * Gets the GoogleApliClient owned by this async task.
         */
        protected GoogleApiClient getGoogleApiClient() {
            return mClient;
        }
    }
}
