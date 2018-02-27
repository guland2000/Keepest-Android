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


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.DictionaryAdapter;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Loaders;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Permissions;
import is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class DictionaryFragment extends Fragment {
    public static final String TAG = "dictionaryFragment";

    public RecyclerView mRecyclerView;
    private Toolbar toolbarFragment;
    private MainActivity mActivity;
    private FloatingActionButton mFabAdd;

    public DictionaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        toolbarFragment = getActivity().findViewById(R.id.toolbar);
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_dictionary, container, false);
        mRecyclerView = root.findViewById(R.id.rv_dicts);
        mFabAdd = root.findViewById(R.id.fab_add);
        DictionaryAdapter adapter = new DictionaryAdapter(null, getActivity());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getLoaderManager().restartLoader(Loaders.Dictionary.LOADER_ID, null, new Loaders.Dictionary.LoadAllDictionaries(getContext(), mRecyclerView, UriHelper.Dictionary.buildDictUri()));

        mActivity = (MainActivity) getActivity();
        mActivity.setToolbar(toolbarFragment, getString(R.string.dictionaries_title));
        mActivity.setAsLastFragment(TAG);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.dictionary, menu);
        mActivity.setActionMenuIconColor(menu, android.R.color.white);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();

        switch (id) {
            case R.id.action_import: {
                if (Permissions.arePermissionReadWriteGranted(getActivity())) {
                    DialogFragment dialogFragment = ImportDialogFragment.newInstance();
                    dialogFragment.show(getActivity().getSupportFragmentManager(), ImportDialogFragment.TAG);
                } else {
                    Permissions.askForPermissionsImportExport(getActivity());
                }
                break;
            }
            case R.id.action_export_all: {
                DialogFragment dialogFragment = ExportMethodDialogFragment.newInstance();
                dialogFragment.show(getActivity().getSupportFragmentManager(), ExportFilesystemDialogFragment.TAG);
                break;
            }
        }
        return true;
    }
}
