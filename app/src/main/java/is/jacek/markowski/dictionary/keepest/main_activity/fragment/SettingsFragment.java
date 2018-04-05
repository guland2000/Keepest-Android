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
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import is.jacek.markowski.dictionary.keepest.BuildConfig;
import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Cache;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Files;
import is.jacek.markowski.dictionary.keepest.main_activity.util.GDriveV3;
import is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport;

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
        // load default values
        PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, false);

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
        // download ivona
        Preference pre_download_ivona = getPreferenceScreen().findPreference("pref_tts_ivona_download");
        pre_download_ivona.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = "http://lmgtfy.com/?q=IVONA+Text-to-Speech+apkpure";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return false;
            }
        });
        // clear cache on voice change
        String[] voices = {
                "pref_tts_polish",
                "pref_tts_icelandic",
                "pref_tts_english",
                "pref_tts_spanish",
                "pref_tts_romanian",
                "pref_tts_german",
                "pref_tts_french",
                "pref_tts_italian",
                "pref_tts_welsh"
        };
        for (String voice : voices) {
            Preference pre_tts_lang = getPreferenceScreen().findPreference(voice);
            pre_tts_lang.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Cache.clearCache(activity);
                    return true;
                }
            });
        }


        Preference pre_version = getPreferenceScreen().findPreference("pref_version");
        String appVersion = BuildConfig.VERSION_NAME;
        pre_version.setSummary(appVersion);


        // send to cloud
        Preference pre_send_to_cloud = getPreferenceScreen().findPreference("pref_send");
        pre_send_to_cloud.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (checkPlayServices()) {
                    GDriveV3 drive = new GDriveV3(activity);
                    drive.signIn(GDriveV3.REQUEST_CODE_UPLOAD);
                    activity.mGdriveV3 = drive;
                    if (drive.isSignedIn()) {
                        Files.prepareJsonAll(activity, ImportExport.getNewDownloadFile(getContext(), null).getName(), TYPE_CLOUD);
                    }
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
                if (checkPlayServices()) {
                    GDriveV3 drive = new GDriveV3(activity);
                    drive.signIn(GDriveV3.REQUEST_CODE_SIGN_IN_DOWNLOAD);
                    activity.mGdriveV3 = drive;
                    drive.showFileDialog();
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

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(getActivity(), resultCode, 0)
                        .show();
            } else {
                return false;
            }
            return false;
        }
        return true;
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
