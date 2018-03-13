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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import is.jacek.markowski.dictionary.keepest.BuildConfig;
import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Cache;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Files;
import is.jacek.markowski.dictionary.keepest.main_activity.util.GDrive;
import is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Message;

import static is.jacek.markowski.dictionary.keepest.main_activity.util.GDrive.MODE_DOWNLOAD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.GDrive.MODE_UPLOAD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.ExportJsonTask.TYPE_CLOUD;

/**
 * Created by jacek on 08.07.17.
 */

public class SettingsFragment extends PreferenceFragment {
    public static final String TAG = "settingsFragment";
    Toolbar toolbarFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final MainActivity activity = (MainActivity) getActivity();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // privacy policy
        Preference pre_privacy_policy = getPreferenceScreen().findPreference("pref_privacy_policy");
        pre_privacy_policy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = "https://jacekm-git.github.io";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return false;
            }
        });
        // source code
        Preference pre_source_code = getPreferenceScreen().findPreference("pref_source_code");
        pre_source_code.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = "https://github.com/jacekm-git/Keepest-Android";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return false;
            }
        });

        Preference pre_version = getPreferenceScreen().findPreference("pref_version");
        String appVersion = BuildConfig.VERSION_NAME;
        pre_version.setSummary(appVersion);


        // send to cloud
        Preference pre_send_to_cloud = getPreferenceScreen().findPreference("pref_send");
        pre_send_to_cloud.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (activity.mGdrive == null) {
                    activity.mGdrive = new GDrive(activity, MODE_UPLOAD);
                }
                if (activity.mGdrive != null && activity.mGdrive.isConnected()) {
                    Files.prepareJsonAll(activity, ImportExport.getNewDownloadFile(getContext(), null).getName(), TYPE_CLOUD);
                } else

                {
                    Message.showToast(activity, getString(R.string.connecting_to_gdrive));
                }
                return true;
            }
        });

        // download from cloud
        Preference pre_download_from_cloud = getPreferenceScreen().findPreference("pref_download");
        pre_download_from_cloud.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()

        {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (activity.mGdrive == null) {
                    activity.mGdrive = new GDrive(activity, MODE_DOWNLOAD);
                    activity.mGdrive.showFileDialog();
                }

                if (activity.mGdrive != null && activity.mGdrive.isConnected()) {
                    activity.mGdrive.showFileDialog();
                } else {
                    Message.showToast(activity, getString(R.string.connecting_to_gdrive));
                }
                return true;
            }
        });
        activity.setAsLastFragment(TAG);

// show cache

        final Preference pre_show_cache = getPreferenceScreen().findPreference("pref_cache_screen");
        pre_show_cache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()

        {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Preference pre_clear_cache = getPreferenceScreen().findPreference("pref_clear_cache");
                pre_clear_cache.setSummary(String.format(getString(R.string.cache_size), Cache.getCacheTotalSize(getContext())));
                pre_clear_cache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Cache.clearCache(getContext());
                        activity.commitSettingsFragment();
                        return false;
                    }
                });
                return false;
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        return super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbarFragment = getActivity().findViewById(R.id.toolbar);
        final MainActivity activity = (MainActivity) getActivity();
        activity.setToolbar(toolbarFragment, getString(R.string.settings_title));
        //check for logged user
        setSummarySignedUser();
    }

    public void setSummarySignedUser() {
    }
}
