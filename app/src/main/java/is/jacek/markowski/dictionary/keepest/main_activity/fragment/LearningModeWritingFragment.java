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
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import is.jacek.markowski.dictionary.keepest.main_activity.util.Text;
import is.jacek.markowski.dictionary.keepest.main_activity.util.Tts;
import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager;

import static is.jacek.markowski.dictionary.keepest.main_activity.util.LearningManager.LearningSession.HINTS_ALLOWED;


public class LearningModeWritingFragment extends Fragment {
    public static final String TAG = LearningModeWritingFragment.class.getName();
    static final String TYPED_ANSWER = "typed_answer";
    private LearningManager.LearningSession mSession;
    private Button mBtNext;
    private Button mBtFlashcard;
    private ProgressBar mProgressBar;
    private TextView mWrongTvCounter;
    private TextView mCorrectTvCounter;
    private ImageButton mBtRestart;
    private ImageButton mBtStop;
    private ImageView mGifView;
    private ImageView mGiphyLogo;
    private Button mBtCheck;
    private Button mBtHint;
    private EditText mEdAnswer;
    private TextView mAnswer;

    public LearningModeWritingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_learning_mode_writing, container, false);
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
        mEdAnswer = getView().findViewById(R.id.ed_test_writing_answer);
        String typedAnswer = WordManager.WordEdit.getTextItem(getContext(), TYPED_ANSWER);
        if (typedAnswer.length() > 0) {
            mEdAnswer.setText(typedAnswer);
        }
        if (LearningManager.LearningSession.mAnswerChecked) {
            nextQuestion();
        }
    }

    private void setupQuestion() {
        TextView question = getView().findViewById(R.id.tv_test_question);
        mBtHint = getView().findViewById(R.id.bt_test_writing_hint);
        mBtCheck = getView().findViewById(R.id.bt_test_writing_check);
        mAnswer = getView().findViewById(R.id.tv_answer);
        mEdAnswer = getView().findViewById(R.id.ed_test_writing_answer);
        mEdAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                WordManager.WordEdit.saveTextItem(getContext(), TYPED_ANSWER, s.toString());
                Question q = LearningManager.getCurrentSession().getCurrentQuestion();
                if (mSession != null && q != null && q.checkAnswer(s.toString()) && !LearningManager.LearningSession.mAnswerChecked) {
                    checkAnswer();
                    // hide keyboard
                    mEdAnswer.clearFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEdAnswer.setTextColor(ContextCompat.getColor(getContext(), R.color.editButton));
        mProgressBar = getView().findViewById(R.id.progress_test_questions);
        mBtNext = getView().findViewById(R.id.bt_test_next);
        mBtFlashcard = getView().findViewById(R.id.bt_test_flashcard);
        mCorrectTvCounter = getView().findViewById(R.id.tv_test_correct_counter);
        mWrongTvCounter = getView().findViewById(R.id.tv_test_wrong_counter);
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
                        .replace(R.id.fragment_container, new LearningSettingsFragment(), LearningModeWritingFragment.TAG)
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
            showAfterQuestionButtons(View.INVISIBLE);
            mBtCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkAnswer();
                }
            });
            mBtHint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (LearningManager.LearningSession.hintsRemaining > 0) {
                        String answer = mSession.getCurrentQuestion().getCorrectAnswer().toLowerCase();
                        String typedAnswer = mEdAnswer.getText().toString().toLowerCase();
                        StringBuilder hint = new StringBuilder(10);
                        for (int i = 0; i < answer.length(); i++) {
                            hint.append(answer.charAt(i));
                            if (i > typedAnswer.length() - 1) {
                                break;
                            }
                            if (answer.charAt(i) != typedAnswer.charAt(i)) {
                                break;
                            }
                        }
                        mEdAnswer.setText(hint.toString());
                        mEdAnswer.setSelection(hint.length());
                        LearningManager.LearningSession.hintsRemaining--;
                    } else {
                        mBtHint.setEnabled(false);
                    }
                }
            });

            mBtFlashcard.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick(View v) {
                    WordSummaryFragment dialog = WordSummaryFragment.newInstance(getActivity(), q.getIdInDatabase());
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    dialog.show(fm, WordSummaryFragment.TAG);
                }
            });

            mBtNext.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick(View v) {
                    nextQuestion();
                }
            });
        } else

        {
            //showFinalScore();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LearningSummaryFragment(), DictionaryFragment.TAG)
                    .commit();
        }
        playQuestion.setOnClickListener(new View.OnClickListener()

        {
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
        if (q != null && Preferences.isShowGif(

                getContext()))

        {
            WordManager.Word entry = WordManager.getWordById(getContext(), q.getIdInDatabase());
            Giphy.displayGif(getActivity(), entry.imageUrl, mGifView);
        }

    }

    private void nextQuestion() {
        WordManager.WordEdit.saveTextItem(getContext(), TYPED_ANSWER, "");
        mSession.moveToNextQuestion();
        setupQuestion();
        mBtHint.setEnabled(true);
        mEdAnswer.setEnabled(true);
        mEdAnswer.setText("");
        mAnswer.setText("");
        mEdAnswer.requestFocus();
        LearningManager.LearningSession.mAnswerChecked = false;
        LearningManager.LearningSession.hintsRemaining = HINTS_ALLOWED;
    }

    private void checkAnswer() {
        if (Text.validate(getContext(), mEdAnswer.getText().toString())) {
            mEdAnswer.setEnabled(false);
            if (LearningManager.getCurrentSession().getCurrentQuestion().checkAnswer(mEdAnswer.getText().toString())) {
                mEdAnswer.setTextColor(ContextCompat.getColor(getContext(), R.color.buttonRightAnswerColor));
                if (!LearningManager.LearningSession.mAnswerChecked)
                    mSession.increaseCorrectCounter();
                mCorrectTvCounter.setText(Integer.toString(mSession.getCorrectCount()));
            } else {
                mEdAnswer.setTextColor(ContextCompat.getColor(getContext(), R.color.buttonWrongAnswerColor));
                if (!LearningManager.LearningSession.mAnswerChecked)
                    mSession.increaseWrongCounter();
                mWrongTvCounter.setText(Integer.toString(mSession.getWrongCount()));
                Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= 26) {
                    vib.vibrate(VibrationEffect.createOneShot(150, 10));
                } else {
                    vib.vibrate(150);
                }
            }
            showAfterQuestionButtons(View.VISIBLE);
            mAnswer.setText(mSession.getCurrentQuestion().getCorrectAnswer());
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
                    ttsManager.onlineTts(
                            entry.word,
                            dict.speak_from,
                            entry.translation,
                            dict.speak_to,
                            isConnected);
                }
            }
        }
        LearningManager.LearningSession.mAnswerChecked = true;
    }


    private void showAfterQuestionButtons(int visibility) {
        mBtNext.setVisibility(visibility);
        mBtFlashcard.setVisibility(visibility);
        if (visibility == View.INVISIBLE) {
            mBtHint.setEnabled(true);
            mBtCheck.setVisibility(View.VISIBLE);
        } else {
            mBtHint.setEnabled(false);
            mBtCheck.setVisibility(View.INVISIBLE);
        }
    }

    private void restartSession() {
        showAfterQuestionButtons(View.INVISIBLE);
        mSession.restartSession();
        mProgressBar.setProgress(0);
        mCorrectTvCounter.setText("0");
        mWrongTvCounter.setText("0");
        setupQuestion();
    }
}
