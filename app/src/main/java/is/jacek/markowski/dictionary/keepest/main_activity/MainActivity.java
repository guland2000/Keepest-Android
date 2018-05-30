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

package is.jacek.markowski.dictionary.keepest.main_activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityOptions;

import java.util.ArrayList;
import java.util.Locale;

import is.jacek.markowski.dictionary.keepest.BuildConfig;
import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.database.Contract;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.DictionaryDialogFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.DictionaryFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.EmptyFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.LearningSessionFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.LearningSettingsFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.SettingsFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordAdvancedFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordFragment;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Files;
import is.jacek.markowski.dictionary.keepest.main_activity.util.GDriveV3;
import is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Language;
import is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Loaders;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Message;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences;
import is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TAG_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_DICTIONARY_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.ADD_MODE;
import static is.jacek.markowski.dictionary.keepest.main_activity.fragment.WordDialogFragment.EDIT_MODE;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.GDriveV3.REQUEST_CODE_OPENER;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.GDriveV3.REQUEST_CODE_SIGN_IN_DOWNLOAD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.GDriveV3.REQUEST_CODE_SIGN_IN_UPLOAD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.GDriveV3.REQUEST_CODE_UPLOAD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.ExportJsonTask.TYPE_CLOUD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Loaders.Words.LOADER_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Loaders.Words.SORT_BY_NAMES;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Loaders.Words.SORT_BY_STARS;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.PREFERENCES_FILE;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.Word.COPY_WORD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.Word.CUT_WORD;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences.Word.EMPTY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper.Word.buildWordsAllUri;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager.WordEdit.prepareContentValues;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TextToSpeech.OnInitListener {

    public static final String LAST_FRAGMENT = "last_fragment";
    // fragments
    public SettingsFragment mSettingsFragment;
    public WordFragment mWordFragment;
    public WordAdvancedFragment mWordAdvancedFragment;
    public DictionaryFragment mDictionaryFragment;
    public boolean mIsWordFragmentOpened = true;
    public GDriveV3 mGdriveV3;
    private NavigationView mNavigationView;
    public TextToSpeech mTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // debug options
        if (BuildConfig.DEBUG) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    //.penaltyLog()
                    //.penaltyDeath()
                    .build());
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setApplicationTheme();
        setContentView(R.layout.activity_main);

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        // fragments
        mDictionaryFragment = new DictionaryFragment();
        mWordFragment = new WordFragment();
        long id = WordManager.WordEdit.getId(this);
        if (id != 0) {
            mWordAdvancedFragment = WordAdvancedFragment.newInstance(this, id, EDIT_MODE);
        } else {
            mWordAdvancedFragment = WordAdvancedFragment.newInstance(this, id, ADD_MODE);
        }
        mSettingsFragment = new SettingsFragment();
        // ivona tts icelandic
        mTts = new TextToSpeech(this, this, "com.ivona.tts");
    }

    private void setApplicationTheme() {
        if (Preferences.NightMode.isNightMode(this)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }

    }

    @Override
    protected void onPostResume() {
        // toolbar search, prevent keyboard display
        super.onPostResume();
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Toolbar toolbarSearch = findViewById(R.id.toolbar_words);
        EditText searchEditText = toolbarSearch.findViewById(R.id.ed_words_search);
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchWords(new View(MainActivity.this));

            }
        });
    }

    public void setActionMenuIconColor(Menu menu, int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(ContextCompat.getColor(this, color), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public void commitWordsFragment() {
        Cursor c = getContentResolver().query(UriHelper.Dictionary.buildDictUri(), null, null, null, null);
        int dictCount = 0;
        if (c != null) {
            dictCount = c.getCount();
            c.close();
        }
        mIsWordFragmentOpened = true;
        invalidateOptionsMenu();
        mNavigationView.setCheckedItem(R.id.nav_words);
        Fragment f;
        String tag;
        if (dictCount < 1) {
            f = new EmptyFragment();
            tag = EmptyFragment.TAG;
        } else {
            f = mWordFragment;
            tag = WordFragment.TAG;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, f, tag)
                .commitAllowingStateLoss();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!mIsWordFragmentOpened) { // always show words fragment on back pressed
            commitWordsFragment();
        } else {
            hideKeyboard();
            super.onBackPressed();
        }
    }


    public void displayDictionaryDetails() {
        final DictionaryManager.Dictionary d = DictionaryManager.getDictData(getApplicationContext());
        final TextView dictSummary = findViewById(R.id.tv_dict_summary);
        final TextView dictName = findViewById(R.id.tv_dict_name);
        LinearLayout layout = findViewById(R.id.linear_layout_dict);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mDictionaryFragment, DictionaryFragment.TAG)
                        .commit();
                mIsWordFragmentOpened = false;
                try {
                    hideWordsSearchToolbar(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DictionaryDialogFragment dialog = DictionaryDialogFragment
                        .newInstance(DictionaryDialogFragment.EDIT_MODE, d.dictId,
                                d.name,
                                d.speak_from,
                                d.speak_to);

                FragmentManager fm = getSupportFragmentManager();
                dialog.show(fm, "editDictDialog");
                return true;
            }
        });
        dictName.setText(d.name);
        dictSummary.setText(Language.getCountryName(getApplicationContext(), d.speak_from) + " - " +
                Language.getCountryName(getApplicationContext(), d.speak_to));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getContentResolver().notifyChange(buildWordsAllUri(), null);
        restoreLastFragment();
    }

    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (this.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(this
                    .getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setAsLastFragment(WordFragment.TAG);
        if (mTts != null) {
            mTts.shutdown();
        }
        WordManager.Word.clearIdOfWordToPaste(this); // empty clipboard
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void restoreLastFragment() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        String lastFragment = preferences.getString(LAST_FRAGMENT, WordFragment.TAG);
        try {
            if (lastFragment.equals(SettingsFragment.TAG)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mSettingsFragment, SettingsFragment.TAG)
                        .commit();
            } else if (lastFragment.equals(DictionaryFragment.TAG)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mDictionaryFragment, DictionaryFragment.TAG)
                        .commit();
            } else if (lastFragment.equals(WordAdvancedFragment.TAG)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mWordAdvancedFragment, WordAdvancedFragment.TAG)
                        .commit();
            } else if (lastFragment.equals(LearningSessionFragment.TAG)) {
                Fragment f;
                if (LearningManager.getCurrentSession() != null) {
                    f = new LearningSessionFragment();
                } else {
                    f = new LearningSettingsFragment();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, f, LearningSessionFragment.TAG)
                        .commit();

            } else {
                commitWordsFragment();
            }

        } catch (Exception e) {
            commitWordsFragment();
        }
    }

    public void setAsLastFragment(String tag) {
        SharedPreferences.Editor preferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
        preferences.putString(LAST_FRAGMENT, tag);
        preferences.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("1", "a");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTts != null)
            mTts.stop();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_words) {
            commitWordsFragment();
        } else if (id == R.id.nav_manage_dictionaries) {
            mIsWordFragmentOpened = false;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DictionaryFragment(), DictionaryFragment.TAG)
                    .commit();
        } else if (id == R.id.nav_settings) {
            mIsWordFragmentOpened = false;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mSettingsFragment, SettingsFragment.TAG)
                    .commit();
        } else if (id == R.id.nav_learning_mode) {
            mIsWordFragmentOpened = false;
            Fragment f;
            if (LearningManager.getCurrentSession() != null) {
                f = new LearningSessionFragment();
            } else {
                f = new LearningSettingsFragment();
                // set only current dictionary checked on the dictionaries list
                DictionaryManager.DictChooser.resetSet(this);
                WordManager.TagChooser.resetSet(this);
                int idOfDict = (int) DictionaryManager.getDictData(this).dictId;
                DictionaryManager.DictChooser.addOrRemoveTagIdFromSet(this, idOfDict);
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, f, LearningSessionFragment.TAG)
                    .commit();
        } else if (id == R.id.nav_theme) {
            if (Preferences.NightMode.isNightMode(this)) {
                Preferences.NightMode.setNightMode(this, false);
            } else {
                Preferences.NightMode.setNightMode(this, true);
            }
            setApplicationTheme();
            finish();
            startActivity(getIntent());
        } else if (id == R.id.nav_rate) {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(myAppLinkToMarket);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.unable_to_find_playstore, Toast.LENGTH_LONG).show();
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        hideWordsSearchToolbar(null);
        hideKeyboard();
        return true;
    }


    public void setToolbar(Toolbar toolbar, String title) {
        AppCompatActivity actionBar = this;
        actionBar.setSupportActionBar(toolbar);

        DrawerLayout drawer = actionBar.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toogle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toogle);
        toogle.setDrawerIndicatorEnabled(true);
        toogle.syncState();
        if (toolbar != null)
            toolbar.setTitle(title);
        supportInvalidateOptionsMenu();
    }

    public void addWordDialog(View view) {
        WordManager.WordEdit.resetValues(this);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container,
                        WordAdvancedFragment.newInstance(this),
                        WordAdvancedFragment.TAG)
                .commit();
    }

    public void addDictionaryDialog(View view) {
        DictionaryDialogFragment dialog = DictionaryDialogFragment.newInstance(DictionaryDialogFragment.ADD_MODE, -1L, "", "", "");
        FragmentManager fm = getSupportFragmentManager();
        dialog.show(fm, DictionaryDialogFragment.TAG);
    }

    public void sortWordList(MenuItem item) {
        WordFragment fragment = (WordFragment) getSupportFragmentManager().findFragmentByTag(WordFragment.TAG);
        RecyclerView recyclerView = fragment.mRecyclerView;
        Uri uri = buildWordsAllUri();
        if (fragment.sortMode.equals(SORT_BY_NAMES)) {
            fragment.sortMode = SORT_BY_STARS;
        } else {
            fragment.sortMode = SORT_BY_NAMES;
        }
        getSupportLoaderManager().destroyLoader(LOADER_ID);
        getSupportLoaderManager().initLoader(LOADER_ID, null,
                new Loaders.Words.LoadAllWords(getBaseContext(), recyclerView, uri, fragment.sortMode));
    }

    public void pasteWord(MenuItem item) {
        int id = (int) WordManager.Word.getIdOfWordToPaste(this);
        String type = WordManager.Word.getWordOperationType(this);
        if (id <= 0) {
            Message.showToast(this, getString(R.string.clipboard_empty));
            return;
        }
        switch (type) {
            case CUT_WORD: {
                ContentValues values = new ContentValues();
                values.put(COLUMN_DICTIONARY_ID, DictionaryManager.getDictData(this).dictId);
                final String where = _ID + "=?";
                final String[] selectionArgs = new String[]{Integer.toString(id)};
                this.getContentResolver().update(
                        buildWordsAllUri(),
                        values,
                        where,
                        selectionArgs);
                Message.showToast(this, getString(R.string.word_pasted));
                break;
            }
            case COPY_WORD: {
                WordManager.Word entry = WordManager.getWordById(this, id);
                ContentValues values = prepareContentValues(this, entry);
                Uri uri = this.getContentResolver().insert(buildWordsAllUri(), values);
                int newWordId = Integer.valueOf(uri.getLastPathSegment());
                // get tags of copied word
                ContentResolver resolver = this.getContentResolver();
                Cursor c = resolver.query(UriHelper.TagsWord.buildTagsForWords(), null, Contract.Tag.Entry.WORD_ID + "=?", new String[]{Integer.toString(id)}, null);
                ArrayList<Integer> tagIds = new ArrayList<>();
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    int tagId = c.getInt(c.getColumnIndex(TAG_ID));
                    tagIds.add(tagId);
                }
                // add tags to copied word
                for (int i = 0; i < c.getCount(); i++) {
                    ContentValues v = new ContentValues();
                    v.put(Contract.Tag.Entry.WORD_ID, newWordId);
                    v.put(TAG_ID, c.getInt(c.getColumnIndex(TAG_ID)));
                    resolver.insert(UriHelper.TagsWord.buildAddTagToWordUri(), v);
                }
                Message.showToast(this, getString(R.string.word_pasted));
                c.close();
                break;
            }
            default:
                Message.showToast(this, getString(R.string.clipboard_empty));
                break;
        }
        WordManager.Word.setWordOperationType(this, EMPTY);
        WordManager.Word.clearIdOfLastAddedWord(this);

    }

    public void commitSettingsFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment(), SettingsFragment.TAG)
                .commit();
    }

    public void showWordsSearchToolbar(MenuItem item) {
        Toolbar toolbarSearch = findViewById(R.id.toolbar_words);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setVisibility(View.INVISIBLE);
        toolbarSearch.setVisibility(View.VISIBLE);
        EditText searchEditText = toolbarSearch.findViewById(R.id.ed_words_search);
        searchEditText.setVisibility(View.VISIBLE);
        toolbarSearch.clearFocus();
        toolbarSearch.requestFocus();
    }

    public void hideWordsSearchToolbar(View view) {
        Toolbar toolbarSearch = findViewById(R.id.toolbar_words);
        Toolbar toolbar = findViewById(R.id.toolbar);
        EditText searchEditText = toolbarSearch.findViewById(R.id.ed_words_search);
        searchEditText.setText("");
        searchEditText.setVisibility(View.INVISIBLE);
        searchEditText.clearFocus();
        toolbar.setVisibility(View.VISIBLE);
        toolbarSearch.setVisibility(View.INVISIBLE);
        hideKeyboard();
    }

    public void searchWords(View view) {
        WordFragment fragment = (WordFragment) getSupportFragmentManager().findFragmentByTag(WordFragment.TAG);
        if (fragment != null) {
            RecyclerView recyclerView = fragment.mRecyclerView;
            Toolbar toolbarSearch = findViewById(R.id.toolbar_words);
            String word = ((EditText) toolbarSearch.findViewById(R.id.ed_words_search)).getText().toString().trim();
            Uri uri = UriHelper.Word.buildWordSearchUri(word);
            WordManager.Word.clearIdOfLastAddedWord(this);
            if (word.length() == 0) {
                uri = buildWordsAllUri();
            }
            getSupportLoaderManager().destroyLoader(LOADER_ID);
            getSupportLoaderManager().initLoader(LOADER_ID, null,
                    new Loaders.Words.LoadAllWords(getBaseContext(), recyclerView, uri, fragment.sortMode));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_OPENER:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
                    mGdriveV3.importFile(driveId);
                }
                break;
            case REQUEST_CODE_UPLOAD:
                if (resultCode == RESULT_OK) {
                    Message.showToast(this, getString(R.string.gdrive_upload_success));
                }
                break;
            case REQUEST_CODE_SIGN_IN_UPLOAD:
                GDriveV3 drive_up = new GDriveV3(this);
                mGdriveV3 = drive_up;
                if (drive_up.isSignedIn()) {
                    Files.prepareJsonAll(this, ImportExport.getNewDownloadFile(this, null).getName(), TYPE_CLOUD);
                }
                break;
            case REQUEST_CODE_SIGN_IN_DOWNLOAD:
                GDriveV3 drive_dwn = new GDriveV3(this);
                mGdriveV3 = drive_dwn;
                drive_dwn.showFileDialog();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void commitWordsFragment(MenuItem item) {
        commitWordsFragment();
    }

    // text to speech initializer
    @Override
    public void onInit(int status) {
        /* todo - add all ivona voices, menu entry for tts setting for languages*/
        // set voice to icelandic
        mTts.setLanguage(new Locale("is"));
    }
}
