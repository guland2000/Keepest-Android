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

import android.net.Uri;

import is.jacek.markowski.dictionary.keepest.main_activity.database.Contract;

import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.URI_FULL;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Dictionary.URI_PATH_DICTS_ALL;
import static is.jacek.markowski.dictionary.keepest.main_activity.database.Contract.Word.URI_PATH_WORDS;

/**
 * Created by jacek on 8/6/17.
 */

public class UriHelper {

    public static class Dictionary {

        public static Uri buildDictUri() {
            return URI_FULL.buildUpon()
                    .appendPath(URI_PATH_DICTS_ALL)
                    .build();
        }
    }

    public static class Word {

        public static Uri buildWordsAllUri() {
            return Contract.Word.URI_FULL.buildUpon()
                    .appendPath(URI_PATH_WORDS)
                    .build();
        }

        public static Uri buildWordsLearningModeUri() {
            return Contract.Word.URI_FULL.buildUpon()
                    .appendPath(URI_PATH_WORDS)
                    .appendPath("LEARNING")
                    .build();
        }

        public static Uri buildWordWithSelectionUri() {
            return Contract.Word.URI_FULL.buildUpon()
                    .appendPath(URI_PATH_WORDS + "_ALL")
                    .build();
        }

        public static Uri buildWordSearchUri(String word) {
            return Contract.Word.URI_FULL.buildUpon()
                    .appendPath(URI_PATH_WORDS)
                    .appendPath("SEARCH")
                    .appendPath(word)
                    .build();
        }

    }

    public static class TagsWord {

        public static Uri buildTagsWordUri(int id) {
            return Contract.Tag.URI_FULL.buildUpon()
                    .appendPath(Contract.Tag.URI_WORD_TAGS)
                    .appendPath(Integer.toString(id))
                    .build();
        }

        public static Uri buildInsertTagUri() {
            return Contract.Tag.URI_FULL.buildUpon()
                    .appendPath(Contract.Tag.URI_CREATE_TAG)
                    .build();
        }

        public static Uri buildAddTagToWordUri() {
            return Contract.Tag.URI_FULL.buildUpon()
                    .appendPath(Contract.Tag.URI_CONNECT_WORD_WITH_TAG)
                    .build();
        }

        public static Uri buildDeleteTagFromWordUri() {
            return Contract.Tag.URI_FULL.buildUpon()
                    .appendPath(Contract.Tag.URI_REMOVE_TAG_FROM_WORD)
                    .build();
        }

        public static Uri buildTagsForWords() {
            return Contract.Tag.URI_FULL.buildUpon()
                    .appendPath(Contract.Tag.URI_WORD_TAG_CONNECTED)
                    .build();
        }

        public static Uri buildDeleteTagUri() {
            return Contract.Tag.URI_FULL.buildUpon()
                    .appendPath(Contract.Tag.URI_DELETE_TAG)
                    .build();
        }

        public static Uri buildUpdateTagUri() {
            return Contract.Tag.URI_FULL.buildUpon()
                    .appendPath(Contract.Tag.URI_UPDATE_TAG)
                    .build();
        }
    }
}
