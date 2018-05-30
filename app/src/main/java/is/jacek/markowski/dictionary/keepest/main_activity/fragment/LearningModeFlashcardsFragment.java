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


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import is.jacek.markowski.dictionary.keepest.R;
import is.jacek.markowski.dictionary.keepest.main_activity.MainActivity;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Connection;
import is.jacek.markowski.dictionary.keepest.main_activity.util.DictionaryManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Giphy;
import is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager;
import is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager.Question;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Preferences;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Tts;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;


public class LearningModeFlashcardsFragment extends Fragment {
    public static final String TAG = LearningModeFlashcardsFragment.class.getName();
    private LearningManager.LearningSession mSession;
    private ProgressBar mProgressBar;
    private TextView mWrongTvCounter;
    private TextView mCorrectTvCounter;
    private ImageButton mBtRestart;
    private ImageButton mBtStop;
    private ImageView mGifView;
    private ImageView mGiphyLogo;
    private Button mBtCheck;
    private TextView mAnswer;
    private Context mContext;
    private ImageButton mIbtCorrect;
    private ImageButton mIbtWrong;

    public LearningModeFlashcardsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_learning_mode_flashcards, container, false);
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
        mContext = getContext();
        final MainActivity activity = (MainActivity) getActivity();
        activity.hideKeyboard();
        activity.setAsLastFragment(LearningSessionFragment.TAG);
        setupQuestion();
        if (LearningManager.LearningSession.mAnswerChecked) {
            nextQuestion();
        }
    }

    private void setupQuestion() {
        final TextView question = getView().findViewById(R.id.tv_flashcards_question);
        mBtCheck = getView().findViewById(R.id.btn_flashcards_check);
        mIbtCorrect = getView().findViewById(R.id.ibtn_flashcards_correct);
        mIbtWrong = getView().findViewById(R.id.ibtn_flashcards_wrong);
        mAnswer = getView().findViewById(R.id.tv_flashcards_answer);
        mProgressBar = getView().findViewById(R.id.progress_test_questions);
        mCorrectTvCounter = getView().findViewById(R.id.tv_test_correct_counter);
        mWrongTvCounter = getView().findViewById(R.id.tv_test_wrong_counter);
        mGifView = getView().findViewById(R.id.img_learning_gif);
        mGiphyLogo = getView().findViewById(R.id.img_giphy_logo);
        // visibility of buttons
        mBtCheck.setVisibility(View.VISIBLE);
        mIbtWrong.setVisibility(View.INVISIBLE);
        mAnswer.setText("");

        if (!Preferences.isShowGif(getContext())) {
            mGifView.setVisibility(View.INVISIBLE);
            mGiphyLogo.setVisibility(View.INVISIBLE);
        }

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
                        .replace(R.id.fragment_container, new LearningSettingsFragment(), LearningModeFlashcardsFragment.TAG)
                        .commit();
            }
        });

        mSession = LearningManager.getCurrentSession();
        if (mSession == null) {
            MainActivity activity = (MainActivity) getActivity();
            activity.commitWordsFragment();
            return;
        }

        mProgressBar.setMax(mSession.mNumberOfQuestions);
        mProgressBar.setProgress(mSession.mQuestionsCounter);
        mProgressBar.setScaleY(3.5f);

        mCorrectTvCounter.setText(Integer.toString(mSession.getCorrectCount()));
        mWrongTvCounter.setText(Integer.toString(mSession.getWrongCount()));

        final Question q = mSession.getCurrentQuestion();
        if (q != null) {
            question.setText(q.getQuestion());
            mBtCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBtCheck.setVisibility(View.INVISIBLE);
                    mIbtWrong.setVisibility(View.VISIBLE);
                    checkAnswer();
                }
            });
            mAnswer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    WordSummaryFragment dialog = WordSummaryFragment.newInstance(getActivity(), q.getIdInDatabase());
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    dialog.show(fm, WordSummaryFragment.TAG);
                    return true;
                }
            });
            mAnswer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Flubber.with()
                            .animation(Flubber.AnimationPreset.ZOOM_IN)
                            .duration(500)
                            .createFor(mAnswer)
                            .start();
                    Tts ttsManager = new Tts(getActivity());
                    if (!ttsManager.isPlaying()) {
                        boolean isConnected = Connection.isConnected(getContext());
                        DictionaryManager.Dictionary dict = DictionaryManager.getDictData(getContext());
                        String answerLang = q.getAnswersLanguage(getContext());
                        ttsManager.onlineTts(q.getCorrectAnswer(), answerLang, "", dict.speak_to, isConnected);
                    }
                }
            });


        } else {
            //showFinalScore;
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LearningSummaryFragment(), DictionaryFragment.TAG)
                    .commit();
        }
        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Flubber.with()
                        .animation(Flubber.AnimationPreset.ZOOM_IN)
                        .duration(500)
                        .createFor(question)
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
        if (q != null && Preferences.isShowGif(mContext)) {
            WordManager.Word entry = WordManager.getWordById(mContext, q.getIdInDatabase());
            Giphy.displayGif(getActivity(), entry.imageUrl, mGifView);
        }
        // play question on start
        if (q != null) {
            if (Preferences.isReadLangOne(mContext)
                    && mSession.mLearningMode == LearningManager.MODE_FLASHCARDS_WORD) {
                question.callOnClick();
            } else if (Preferences.isReadLangTwo(mContext)
                    && mSession.mLearningMode == LearningManager.MODE_FLASHCARDS_TRANSLATION) {
                question.callOnClick();
            }
        }

        mIbtCorrect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSession.increaseCorrectCounter();
                nextQuestion();
            }
        });
        mIbtWrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSession.increaseWrongCounter();
                Preferences.LearningSummary.addIdToSet(getContext(), mSession.getCurrentWordId());
                nextQuestion();
            }
        });
    }

    private void nextQuestion() {
        mSession.moveToNextQuestion();
        setupQuestion();
        LearningManager.LearningSession.mAnswerChecked = false;
    }

    private void checkAnswer() {
        mAnswer.setText(mSession.getCurrentQuestion().getCorrectAnswer());
        // play text to speech
        Tts ttsManager = new Tts(getActivity());
        if (!ttsManager.isPlaying()) {
            String answersLang = mSession.getCurrentQuestion().getAnswersLanguage(getContext());
            if (answersLang != null) {
                if (Preferences.isReadLangOne(mContext)
                        && mSession.mLearningMode == LearningManager.MODE_FLASHCARDS_TRANSLATION) {
                    mAnswer.callOnClick();
                } else if (Preferences.isReadLangTwo(mContext)
                        && mSession.mLearningMode == LearningManager.MODE_FLASHCARDS_WORD) {
                    mAnswer.callOnClick();
                }
            }
        }
    }

    private void restartSession() {
        mSession.restartSession();
        mProgressBar.setProgress(0);
        mCorrectTvCounter.setText("0");
        mWrongTvCounter.setText("0");
        setupQuestion();
    }
}
