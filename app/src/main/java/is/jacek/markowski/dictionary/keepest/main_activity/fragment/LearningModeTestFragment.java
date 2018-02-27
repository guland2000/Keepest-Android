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


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appolica.flubber.Flubber;

import java.util.ArrayList;
import java.util.List;

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Connection;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Giphy;
import is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager.Question;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Tts;
import is.jacek.markowski.dictionary.keepest.main_activity.util.TutorialManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.TutorialManager.TutorialItem;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;


public class LearningModeTestFragment extends Fragment {
    public static final String TAG = LearningModeTestFragment.class.getName();
    private LearningManager.LearningSession mSession;
    private TextView mBtAnswerOne;
    private TextView mBtAnswerTwo;
    private TextView mBtAnswerThree;
    private TextView mBtAnswerFour;
    private Button mBtNext;
    private Button mBtFlashcard;
    private TextView[] mAnswerButtons;
    private ProgressBar mProgressBar;
    private TextView mWrongTvCounter;
    private TextView mCorrectTvCounter;
    private ConstraintLayout mAnswersLayout;
    private int mDefaultButtonColor;
    private ImageButton mBtRestart;
    private ImageButton mBtStop;
    private ImageView mGifView;
    private ImageView mGiphyLogo;

    public LearningModeTestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_learning_mode_test, container, false);
        mDefaultButtonColor = ContextCompat.getColor(getActivity(), R.color.buttonDefaultColor);
        return root;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbarFragment = getActivity().findViewById(R.id.toolbar);
        final MainActivity activity = (MainActivity) getActivity();
        activity.setToolbar(toolbarFragment, getString(R.string.learning_mode));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.learning, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        final MainActivity activity = (MainActivity) getActivity();
        activity.hideKeyboard();
        activity.setAsLastFragment(LearningSessionFragment.TAG);
        setupQuestion();
        // tutorial
        List<TutorialItem> items = new ArrayList<>();
        items.add(new TutorialItem(mBtRestart, getString(R.string.turorial_restart_session)));
        items.add(new TutorialItem(mBtStop, getString(R.string.tutorial_end_session)));
        try {
            TutorialManager.showTutorialSequence(getActivity(), items, TAG + "sessionLearn");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupQuestion() {
        TextView question = getView().findViewById(R.id.tv_test_question);
        mBtAnswerOne = getView().findViewById(R.id.tv_test_answer_1);
        mBtAnswerTwo = getView().findViewById(R.id.tv_test_answer_2);
        mBtAnswerThree = getView().findViewById(R.id.tv_test_answer_3);
        mBtAnswerFour = getView().findViewById(R.id.tv_test_answer_4);
        mAnswerButtons = new TextView[]{mBtAnswerOne, mBtAnswerTwo, mBtAnswerThree, mBtAnswerFour};
        mProgressBar = getView().findViewById(R.id.progress_test_questions);
        mBtNext = getView().findViewById(R.id.bt_test_next);
        mBtFlashcard = getView().findViewById(R.id.bt_test_flashcard);
        mCorrectTvCounter = getView().findViewById(R.id.tv_test_correct_counter);
        mWrongTvCounter = getView().findViewById(R.id.tv_test_wrong_counter);
        mAnswersLayout = getView().findViewById(R.id.layout_test_questions);
        mGifView = getView().findViewById(R.id.img_learning_gif);
        mGiphyLogo = getView().findViewById(R.id.img_giphy_logo);

        if (!Preferences.isShowGif(getContext())) {
            mGifView.setVisibility(View.INVISIBLE);
            mGiphyLogo.setVisibility(View.INVISIBLE);
        }

        final ImageButton playQuestion = getView().findViewById(R.id.ibt_test_play_question);


        mBtRestart = getView().findViewById(R.id.bt_test_restart);
        mBtStop = getView().findViewById(R.id.bt_test_stop);

        mBtRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartSession();
            }
        });
        mBtStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LearningManager.stopSession();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LearningSettingsFragment(), LearningModeTestFragment.TAG)
                        .commit();
            }
        });

        mSession = LearningManager.getCurrentSession();
        if (mSession == null) {
            MainActivity activity = (MainActivity) getActivity();
            activity.commitWordsFragment();
        }

        mProgressBar.setMax(mSession.mNumberOfQuestions);
        mProgressBar.setProgress(mSession.mQuestionsCounter);
        mProgressBar.setScaleY(3.5f);

        mCorrectTvCounter.setText(Integer.toString(mSession.getCorrectCount()));
        mWrongTvCounter.setText(Integer.toString(mSession.getWrongCount()));

        final Question q = mSession.getCurrentQuestion();
        if (q != null) {
            question.setText(q.getQuestion());
            List<String> answers = q.getAnswers();
            int answerIndex = 0;
            for (TextView b : mAnswerButtons) {
                // on click listeners
                setOnClickAnswerListener(b);
                // answers text
                b.setText(answers.get(answerIndex));
                answerIndex++;
            }

            showAfterQuestionButtons(View.INVISIBLE);

            mBtFlashcard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WordSummaryFragment dialog = WordSummaryFragment.newInstance(getActivity(), q.getIdInDatabase());
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    dialog.show(fm, WordSummaryFragment.TAG);
                }
            });

            mBtNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupQuestion();
                }
            });
            resetAllButtons();
        } else {
            // wyświet final score fragment
            //showFinalScore();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LearningSummaryFragment(), DictionaryFragment.TAG)
                    .commit();
        }
        playQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Flubber.with()
                        .animation(Flubber.AnimationPreset.ZOOM_IN)
                        .duration(500)
                        .createFor(playQuestion)
                        .start();
                Tts ttsManager = new Tts(getActivity());
                if (!ttsManager.isPlaying()) {
                    boolean isConnected = Connection.isConnected(getContext());
                    DictionaryManager.Dictionary dict = DictionaryManager.getDictData(getContext());
                    String questionLang = q.getQuestionLanguage(getContext());
                    ttsManager.onlineTts(q.getQuestion(), questionLang, "", dict.speak_to, isConnected);
                }
            }
        });

        // show gif
        if (q != null && Preferences.isShowGif(getContext())) {
            WordManager.Word entry = WordManager.getWordById(getContext(), q.getIdInDatabase());
            Giphy.displayGif(getActivity(), entry.imageUrl, mGifView);
        }

    }


    private void showAfterQuestionButtons(int visibility) {
        mBtNext.setVisibility(visibility);
        mBtFlashcard.setVisibility(visibility);
    }

    private void restartSession() {
        showAfterQuestionButtons(View.INVISIBLE);
        enableAllAnswerButtons(true);
        mSession.restartSession();
        mProgressBar.setProgress(0);
        mCorrectTvCounter.setText("0");
        mWrongTvCounter.setText("0");
        setupQuestion();
    }

    private void resetAllButtons() {
        enableAllAnswerButtons(true);
        for (TextView b : mAnswerButtons) {
            b.setBackgroundColor(mDefaultButtonColor);
        }
    }

    @SuppressLint("SetTextI18n")
    private void changeButtonColorOnAnswer(TextView button) {
        String buttonAnswer = button.getText().toString();
        if (LearningManager.getCurrentSession().getCurrentQuestion().checkAnswer(buttonAnswer)) {
            button.setBackgroundResource(R.color.buttonRightAnswerColor);
            mSession.increaseCorrectCounter();
            mCorrectTvCounter.setText(Integer.toString(mSession.getCorrectCount()));
            // animation
            Flubber.with()
                    .animation(Flubber.AnimationPreset.ALPHA)
                    .duration(500)
                    .createFor(button)
                    .start();
        } else {
            button.setBackgroundResource(R.color.buttonWrongAnswerColor);
            showCorrectAnswers();
            mSession.increaseWrongCounter();
            mWrongTvCounter.setText(Integer.toString(mSession.getWrongCount()));
            // vibrate
            Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(150, 10));
            } else {
                v.vibrate(150);
            }

            //shake animation
            Flubber.with()
                    .animation(Flubber.AnimationPreset.WOBBLE)
                    .duration(300)
                    .createFor(button)
                    .start();
        }
        // play text to speech
        Tts ttsManager = new Tts(getActivity());
        if (!ttsManager.isPlaying()) {
            boolean isConnected = Connection.isConnected(getContext());
            DictionaryManager.Dictionary dict = DictionaryManager.getDictData(getContext());
            String answersLang = mSession.getCurrentQuestion().getAnswersLanguage(getContext());
            WordManager.Word entry = WordManager.getWordById(
                    getContext(),
                    mSession.getCurrentQuestion().getIdInDatabase());
            if (answersLang != null && Preferences.isReadAnswers(getContext())) {
                // read answers
                if ((mSession.mLearningMode == LearningManager.MODE_WORD_TRANSLATION)
                        || (mSession.mLearningMode == LearningManager.MODE_WORD_TAG)) {
                    ttsManager.onlineTts(
                            mSession.getCurrentQuestion().getQuestion(),
                            dict.speak_from,
                            entry.translation,
                            dict.speak_to,
                            isConnected);
                } else {
                    ttsManager.onlineTts(
                            mSession.getCurrentQuestion().getQuestion(),
                            dict.speak_to,
                            entry.word,
                            dict.speak_from,
                            isConnected);
                }
            }
        }
    }

    private void setOnClickAnswerListener(final TextView button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonColorOnAnswer(button);
                enableAllAnswerButtons(false);
                mBtNext.setVisibility(View.VISIBLE);
                mBtFlashcard.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(mProgressBar.getProgress() + 1);
                showAfterQuestionButtons(View.VISIBLE);
                mSession.moveToNextQuestion();
            }
        });
    }

    private void enableAllAnswerButtons(boolean isEnabled) {
        for (TextView b : mAnswerButtons) {
            if (b != null) {
                b.setEnabled(isEnabled);
            }
        }
    }

    private void showCorrectAnswers() {
        for (TextView b : mAnswerButtons) {
            if (b != null) {
                String buttonAnswer = b.getText().toString();
                if (LearningManager.getCurrentSession().getCurrentQuestion().checkAnswer(buttonAnswer)) {
                    b.setBackgroundResource(R.color.buttonRightAnswerColor);
                }
            }
        }
    }

}
