package is.jacek.markowski.dictionary.keepest.main_activity.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager.Word;

import static android.provider.BaseColumns._ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.Entry.COLUMN_DICTIONARY_ID;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.WordManager.WordEdit.prepareContentValues;

// Spaced Repetition System
public class SRS {

    private static void updateWord(Context context, Word entry) {
        Uri uri = UriHelper.Word.buildWordsAllUri();
        ContentValues values = prepareContentValues(context, entry);
        values.remove(COLUMN_DICTIONARY_ID); // don't update dict
        ContentResolver resolver = context.getContentResolver();
        resolver.update(uri, values, _ID + "=?", new String[]{Long.toString(entry.id)});
    }

    public static void correctAnswer(Context context, Word entry) {
        entry.increaseCorrect();
        updateWord(context, entry);
    }

    public static void wrongAnswer(Context context, Word entry) {
        entry.increaseWrong();
        updateWord(context, entry);
    }
}
