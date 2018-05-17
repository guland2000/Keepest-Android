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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.text.DecimalFormat;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.adapter.WordAdapterLearningSummary;
import is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences;


public class LearningSummaryFragment extends Fragment {
    public static final String TAG = LearningSummaryFragment.class.getName();
    public RecyclerView mRecyclerView;


    public LearningSummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_learning_summary, container, false);
        ImageButton restart = root.findViewById(R.id.bt_test_restart_big);
        ImageButton stop = root.findViewById(R.id.bt_test_stop_big);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LearningManager.getCurrentSession().restartSession();
                // reset set of mistakes
                Preferences.LearningSummary.resetSet(getContext());
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LearningSessionFragment(), LearningSessionFragment.TAG)
                        .commit();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LearningManager.stopSession();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LearningSettingsFragment(), LearningSessionFragment.TAG)
                        .commit();

            }
        });

        // recycler view: show mistakes
        mRecyclerView = root.findViewById(R.id.rv_learning_summary);
        WordAdapterLearningSummary adapter = new WordAdapterLearningSummary(LearningManager.getCurrentSession().mWords, getContext());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbarFragment = getActivity().findViewById(R.id.toolbar);
        final MainActivity activity = (MainActivity) getActivity();
        activity.setToolbar(toolbarFragment, getString(R.string.learning_mode));
        try {
            showFinalScore();
        } catch (Exception e) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LearningSettingsFragment(), LearningSessionFragment.TAG)
                    .commit();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.learning, menu);
    }

    private void showFinalScore() {
        DecimalFormat df = new DecimalFormat("#.0");
        LearningManager.LearningSession mSession = LearningManager.getCurrentSession();
        TextView mTvScorePercent = getView().findViewById(R.id.tv_test_score_percent);
        DecoView mArcView = getView().findViewById(R.id.dynamicArcView);
        if (mSession.getFinalScore() == 0) {
            mTvScorePercent.setText("0.0 %");
        } else {
            mTvScorePercent.setText(df.format(mSession.getFinalScore()) + " %");
        }
        // circle background
        mArcView.addSeries(new SeriesItem.Builder(ContextCompat.getColor(getActivity(), R.color.buttonWrongAnswerColor))
                .setRange(0, 100, 100)
                .setLineWidth(64f)
                .build());

        // circle filling
        SeriesItem seriesItem1 = new SeriesItem.Builder(ContextCompat.getColor(getActivity(), R.color.buttonRightAnswerColor))
                .setRange(0, 100, 0)
                .setLineWidth(64f)
                .build();

        int series1Index = mArcView.addSeries(seriesItem1);
        //add events and animate class
        mArcView.addEvent(new DecoEvent.Builder(mSession.getFinalScore()).setIndex(series1Index).setDelay(400)
                .setDuration(700L)
                .build());

    }
}
