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

package is.jacek.markowski.dictionary.keepest.main_activity.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jacek on 8/14/17.
 */

public class Contract {

    public static final class Dictionary {
        public static final String URI_AUTHORITY = "providerDict";
        public static final String URI_PATH_DICTS_ALL = "dict";
        private static final String URI_SCHEME = "content://";
        public static final Uri URI_FULL = Uri.parse(URI_SCHEME + URI_AUTHORITY);

        public static class Entry implements BaseColumns {
            public static final String TABLE_DICTIONARY = "DICTIONARIES";
            public static final String COLUMN_DICTIONARY_NAME = "DICTIONARY";
            public static final String COLUMN_DICTIONARY_FROM = "SPEAK_FROM";
            public static final String COLUMN_DICTIONARY_TO = "SPEAK_TO";
        }
    }

    public static final class Tag {
        public static final String URI_AUTHORITY = "providerTags";
        public static final String URI_WORD_TAGS = "wordTags";
        public static final String URI_CREATE_TAG = "createTags";
        public static final String URI_DELETE_TAG = "deleteTag";
        public static final String URI_UPDATE_TAG = "updateTag";
        public static final String URI_CONNECT_WORD_WITH_TAG = "connectWordWithTag";
        public static final String URI_REMOVE_TAG_FROM_WORD = "removeTagFromWord";
        public static final String URI_WORD_TAG_CONNECTED = "wordTagsConnected";
        private static final String URI_SCHEME = "content://";
        public static final Uri URI_FULL = Uri.parse(URI_SCHEME + URI_AUTHORITY);

        public static class Entry implements BaseColumns {
            public static final String TABLE_TAGS = "TAGS";
            public static final String TABLE_DICTIONARY_TAGS = "TAGS";
            public static final String TABLE_WORD_TAG_RELATIONS = "WORD_TAGS_RELATIONS";
            public static final String TABLE_DICTIONARY_TAGS_RELATIONS = "DICTIONARY_TAGS_RELATIONS";
            public static final String COLUMN_TAG = "TAG";
            public static final String TAG_ID = "TAG_ID";
            public static final String WORD_ID = "WORD_ID";
            public static final String DICTIONARY_ID = "DICTIONARY_ID";
        }

    }

    public static final class Word {
        public static final String URI_AUTHORITY = "providerWord";
        public static final String URI_PATH_WORDS = "word";
        private static final String URI_SCHEME = "content://";
        public static final Uri URI_FULL = Uri.parse(URI_SCHEME + URI_AUTHORITY);

        public static class Entry implements BaseColumns {
            public static final String TABLE_WORD = "WORDS";
            public static final String COLUMN_WORD = "WORD";
            public static final String COLUMN_TRANSLATION = "TRANSLATION";
            public static final String COLUMN_DICTIONARY_ID = "DICTIONARY";
            public static final String COLUMN_FAVOURITE = "FAVOURITE";
            public static final String COLUMN_NOTES = "NOTES";
            public static final String COLUMN_IMAGE = "IMAGE";
            public static final String COLUMN_LEVEL = "LEVEL";
            public static final String COLUMN_NEXT_REVIEW = "NEXT_REVIEW";
            public static final String COLUMN_GOOD_IN_ROW = "GOOD_ROW";
            public static final String COLUMN_BAD_IN_ROW = "BAD_ROW";
            public static final String COLUMN_GOOD = "GOOD";
            public static final String COLUMN_BAD = "BAD";
        }
    }
}
