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

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import is.jacek.markowski.dictionary.keepest.main_activity.database.Contract;
import is.jacek.markowski.dictionary.keepest.main_activity.database.DatabaseHelper;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.COLUMN_TAG;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TABLE_TAGS;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TABLE_WORD_TAG_RELATIONS;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.TAG_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Tag.Entry.WORD_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.UriHelper.Word.buildWordsLearningModeUri;

/**
 * Created by jacek on 8/24/17.
 */

public class LearningManager {
    public static final int MODE_WORD_TRANSLATION = 0;
    public static final int MODE_WORD_TAG = 1;
    public static final int MODE_TRANSLATION_WORD = 2;
    public static final int MODE_TRANSLATION_TAG = 3;
    public static final int MODE_WRITING_WORD = 4;
    public static final int MODE_WRITING_TRANSLATION = 5;
    public static final int MODE_FLASHCARDS_WORD = 6;
    public static final int MODE_FLASHCARDS_TRANSLATION = 7;
    public static final String MODE_KEY = "mode";
    public static final String QUESTION_COUNT_KEY = "questions";
    private static final String TAG = LearningManager.class.getPackage().getName() + "Learning";
    private static LearningSession sLearningSession;

    public static boolean createNewSession(ContentResolver resolver, DatabaseHelper db, int numberOfQuestions, int learningMode, Set<String> setOfTagIds) {
        Cursor cursor = resolver.query(buildWordsLearningModeUri(), null, null, null, null);
        if (cursor == null || cursor.getCount() < 1 || numberOfQuestions <= 0) {
            sLearningSession = null;
            return false;
        }
        sLearningSession = new LearningSession(setOfTagIds);
        sLearningSession.mWords = cursor;
        sLearningSession.mNumberOfQuestions = numberOfQuestions;
        sLearningSession.mLearningMode = learningMode;
        sLearningSession.mDb = db;
        sLearningSession.questions = RandomQuestions.prepareRandomizedQuestions(cursor, numberOfQuestions);
        cursor.moveToFirst();
        sLearningSession.restartSession();
        return true;
    }

    public static LearningSession getCurrentSession() {
        return sLearningSession;
    }

    public static void stopSession() {
        if (sLearningSession != null && sLearningSession.mWords != null) {
            sLearningSession.mWords.close();
        }
        sLearningSession = null;
    }

    public static void saveSettingsInPreferences(Context context, int mode, int questionCount) {
        SharedPreferences.Editor editor = context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        editor.putInt(MODE_KEY, mode);
        editor.putInt(QUESTION_COUNT_KEY, questionCount);
        editor.apply();
    }

    public static int getPreferencesLearningMode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return pref.getInt(MODE_KEY, 0);
    }

    public static int getPreferencesQuestionCount(Context context) {
        SharedPreferences pref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return pref.getInt(QUESTION_COUNT_KEY, 10);
    }


    public interface Question {
        String getCorrectAnswer();

        String getQuestionLanguage(Context context);

        String getAnswersLanguage(Context context);

        String getQuestion();

        List<String> getAnswers();

        boolean checkAnswer(String answer);

        long getIdInDatabase();
    }

    public static class LearningSession {
        public static final int HINTS_ALLOWED = 2;
        public static boolean mAnswerChecked = false;
        public static int hintsRemaining = HINTS_ALLOWED;
        public int mNumberOfQuestions;
        public int mQuestionsCounter = 0;
        public ArrayList<Integer> questions;
        public int mLearningMode;
        private int questionIndex = 0;
        private String[] mSetOfTagIds;
        public Cursor mWords;
        private int mCorrectCounter;
        private int mWrongCounter;
        private DatabaseHelper mDb;
        private Random mRandom = new Random(System.currentTimeMillis() / 1000);

        LearningSession(Set<String> set) {
            mSetOfTagIds = new String[set.size()];
            set.toArray(mSetOfTagIds);
        }

        public void moveToNextQuestion() {
            // random order of questions
            if (questionIndex == mNumberOfQuestions || questionIndex == mWords.getCount()) {
                questionIndex = 0;
            }
            mWords.moveToPosition(questions.get(questionIndex++));
            mQuestionsCounter++;
        }

        public Question getCurrentQuestion() {
            if (mQuestionsCounter < mNumberOfQuestions) {
                switch (mLearningMode) {
                    case MODE_WORD_TRANSLATION: {
                        return new QuestionWordTranslation();
                    }
                    case MODE_TRANSLATION_WORD: {
                        return new QuestionTranslationWord();
                    }
                    case MODE_WORD_TAG: {
                        return new QuestionWordTag();
                    }
                    case MODE_TRANSLATION_TAG: {
                        return new QuestionTranslationTag();
                    }
                    case MODE_WRITING_WORD: {
                        return new QuestionWordTranslation();
                    }
                    case MODE_WRITING_TRANSLATION: {
                        return new QuestionTranslationWord();
                    }
                    case MODE_FLASHCARDS_WORD: {
                        return new QuestionWordTranslation();
                    }
                    case MODE_FLASHCARDS_TRANSLATION: {
                        return new QuestionTranslationWord();
                    }
                    default: {
                        return new QuestionWordTranslation();
                    }
                }
            }
            return null;
        }

        public int getWrongCount() {
            return mWrongCounter;
        }

        public int getCorrectCount() {
            return mCorrectCounter;
        }

        public void increaseCorrectCounter() {
            mCorrectCounter++;
        }

        public void increaseWrongCounter() {
            mWrongCounter++;
        }

        public float getFinalScore() {
            return (mCorrectCounter / (float) (mCorrectCounter + mWrongCounter)) * 100;
        }

        public void restartSession() {
            sLearningSession.questions = RandomQuestions.prepareRandomizedQuestions(mWords, mNumberOfQuestions);
            questionIndex = 0;
            moveToNextQuestion();
            mWrongCounter = 0;
            mCorrectCounter = 0;
            mQuestionsCounter = 0;
        }

        public int getCurrentWordId() {
            return mWords.getInt(mWords.getColumnIndex(_ID));
        }

        ///////////////////////////
        // Word -> Translation mode
        ///////////////////////////
        private class QuestionWordTranslation extends QuestionScheme {
            @Override
            String generateQuestion() {
                return mWords.getString(mWords.getColumnIndex(Contract.Word.Entry.COLUMN_WORD));
            }

            @Override
            String generateAnswer() {
                return mWords.getString(mWords.getColumnIndex(Contract.Word.Entry.COLUMN_TRANSLATION));
            }

            @Override
            String[] generateCorrectAnswers() {
                String selection = Contract.Word.Entry.COLUMN_WORD + "=?";
                String[] selectionArgs = new String[]{generateQuestion()};
                Cursor c = mDb.getReadableDatabase().query(Contract.Word.Entry.TABLE_WORD, null, selection, selectionArgs, null, null, null);
                List<String> answers = new ArrayList<>();
                if (c != null && c.getCount() >= 1) {
                    while (c.moveToNext()) {
                        String answer = c.getString(c.getColumnIndex(Contract.Word.Entry.COLUMN_TRANSLATION));
                        answers.add(answer);
                    }
                    c.close();
                }
                return answers.toArray(new String[answers.size()]);
            }

            @Override
            public String getQuestionLanguage(Context context) {
                return DictionaryManager.getDictData(context).speak_from;
            }

            @Override
            public String getAnswersLanguage(Context context) {
                return DictionaryManager.getDictData(context).speak_to;
            }
        }

        ///////////////////////////
        // Translation -> Word mode
        ///////////////////////////
        private class QuestionTranslationWord extends QuestionWordTranslation {
            @Override
            String generateQuestion() {
                return mWords.getString(mWords.getColumnIndex(Contract.Word.Entry.COLUMN_TRANSLATION));
            }

            @Override
            String generateAnswer() {
                return mWords.getString(mWords.getColumnIndex(Contract.Word.Entry.COLUMN_WORD));
            }

            @Override
            String[] generateCorrectAnswers() {
                String selection = Contract.Word.Entry.COLUMN_TRANSLATION + "=?";
                String[] selectionArgs = new String[]{generateQuestion()};
                Cursor c = mDb.getReadableDatabase().query(Contract.Word.Entry.TABLE_WORD, null, selection, selectionArgs, null, null, null);
                List<String> answers = new ArrayList<>();
                if (c != null && c.getCount() >= 1) {
                    while (c.moveToNext()) {
                        String answer = c.getString(c.getColumnIndex(Contract.Word.Entry.COLUMN_WORD));
                        answers.add(answer);
                    }
                    c.close();
                }
                return answers.toArray(new String[answers.size()]);
            }

            @Override
            public String getQuestionLanguage(Context context) {
                return DictionaryManager.getDictData(context).speak_to;
            }

            @Override
            public String getAnswersLanguage(Context context) {
                return DictionaryManager.getDictData(context).speak_from;
            }
        }

        ///////////////////////////
        // Word -> TAG mode
        ///////////////////////////
        private class QuestionWordTag extends QuestionScheme {

            @Override
            String[] generateCorrectAnswers() {
                int wordId = mWords.getInt(mWords.getColumnIndex(WORD_ID));
                String selection = WORD_ID + "=?";
                String[] selectionArgs = new String[]{Integer.toString(wordId)};
                Cursor tagIdsForWordCursor = mDb.getReadableDatabase().query(TABLE_WORD_TAG_RELATIONS, null, selection, selectionArgs, null, null, null);
                List<String> tags = new ArrayList<>();
                while (tagIdsForWordCursor.moveToNext()) {
                    int tagId = tagIdsForWordCursor.getInt(tagIdsForWordCursor.getColumnIndex(TAG_ID));
                    String tagName = getTagName(tagId);
                    tags.add(tagName);
                }
                tagIdsForWordCursor.close();
                Collections.shuffle(tags);
                return tags.toArray(new String[tags.size()]);

            }

            @Override
            String generateAnswer() {
                int tagId = mWords.getInt(mWords.getColumnIndex(TAG_ID));
                return getTagName(tagId);
            }

            List<String> generateAnswers() {
                List<String> answers = new ArrayList<>();
                answers.add(generateAnswer()); // add correct answer
                // add wrong answers
                while (true) {
                    if (answers.size() == 4) {
                        break;
                    }
                    // find answer try avoid duplicates
                    int randomPosition = mRandom.nextInt(mSetOfTagIds.length);
                    int tagId = Integer.valueOf(mSetOfTagIds[randomPosition]);
                    for (int i = 0; i < 3; i++) {
                        if (!answers.contains(getTagName(tagId))) {
                            break;
                        }
                        randomPosition = mRandom.nextInt(mSetOfTagIds.length);
                        tagId = Integer.valueOf(mSetOfTagIds[randomPosition]);
                    }
                    answers.add(getTagName(tagId));
                }
                // shuffle answers
                Collections.shuffle(answers);
                return answers;
            }

            @Override
            String generateQuestion() {
                return mWords.getString(mWords.getColumnIndex(Contract.Word.Entry.COLUMN_WORD));
            }

            String getTagName(int tagId) {
                String selectionTag = _ID + "=?";
                String[] selectionTagArgs = new String[]{Integer.toString(tagId)};
                Cursor tagNameCursor = mDb.getReadableDatabase().query(TABLE_TAGS, null, selectionTag, selectionTagArgs, null, null, null);
                if (tagNameCursor != null) {
                    tagNameCursor.moveToFirst();
                }
                String tagName = tagNameCursor.getString(tagNameCursor.getColumnIndex(COLUMN_TAG));
                tagNameCursor.close();
                return "#" + tagName;
            }

            @Override
            public String getQuestionLanguage(Context context) {
                return DictionaryManager.getDictData(context).speak_from;
            }

            @Override
            public String getAnswersLanguage(Context context) {
                return null;
            }
        }

        ///////////////////////////
        // Translation -> TAG mode
        ///////////////////////////
        private class QuestionTranslationTag extends QuestionWordTag {
            @Override
            String generateQuestion() {
                return mWords.getString(mWords.getColumnIndex(Contract.Word.Entry.COLUMN_TRANSLATION));
            }

            @Override
            public String getQuestionLanguage(Context context) {
                return DictionaryManager.getDictData(context).speak_to;
            }

        }

        // Main questions scheme
        private abstract class QuestionScheme implements Question {

            public String mCorrectAnswer;
            protected String[] mCorrectAnswers;
            private int idInDatabase;
            private String mQuestion;
            private List<String> mAnswers;

            QuestionScheme() {
                mCorrectAnswers = generateCorrectAnswers();
                mQuestion = generateQuestion();
                mAnswers = generateAnswers();
                idInDatabase = mWords.getInt(mWords.getColumnIndex(_ID));
            }

            abstract String[] generateCorrectAnswers();

            abstract String generateAnswer();

            abstract String generateQuestion();

            List<String> generateAnswers() {
                List<String> answers = new ArrayList<>();
                int oldPosition = mWords.getPosition();
                int count = mWords.getCount();
                mCorrectAnswer = generateAnswer();
                answers.add(mCorrectAnswer); // add correct answer
                // add wrong answers
                while (true) {
                    if (answers.size() == 4) {
                        break;
                    }
                    String answer = "";
                    // find answer try avoid duplicates
                    for (int i = 0; i < 3; i++) {
                        int randomPosition = mRandom.nextInt(count);
                        mWords.moveToPosition(randomPosition);
                        answer = generateAnswer();
                        if (!answers.contains(answer)) {
                            break;
                        }
                    }
                    answers.add(answer);
                }
                mWords.moveToPosition(oldPosition);
                // shuffle answers
                Collections.shuffle(answers);
                return answers;
            }


            @Override
            public String getCorrectAnswer() {
                return mCorrectAnswer;
            }

            @Override
            public String getQuestion() {
                return mQuestion;
            }

            @Override
            public List<String> getAnswers() {
                return mAnswers;
            }

            @Override
            public boolean checkAnswer(String answer) {
                for (String s : mCorrectAnswers) {
                    if (s.toLowerCase().trim().equals(answer.toLowerCase().trim())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public long getIdInDatabase() {
                return idInDatabase;
            }
        }
    }
}

class RandomQuestions {
    private static Random random = new Random(System.currentTimeMillis() / 1000);

    static ArrayList<Integer> prepareRandomizedQuestions(Cursor cursor, int numberOfQuestions) {
        // available questions > numberOfQuestion
        ArrayList<Integer> questionSet = new ArrayList<>(numberOfQuestions);
        if (cursor.getCount() >= numberOfQuestions) {
            // find non repeating questions
            while (questionSet.size() < numberOfQuestions) {
                int randomIndex = random.nextInt(cursor.getCount());
                if (!questionSet.contains(randomIndex)) {
                    questionSet.add(randomIndex);
                }
            }
        } else {
            // question have to repeat
            int index = 0;
            while (questionSet.size() < cursor.getCount()) {
                questionSet.add(index++);
            }
        }

        // randomize questions
        Collections.shuffle(questionSet);
        return questionSet;
    }
}
