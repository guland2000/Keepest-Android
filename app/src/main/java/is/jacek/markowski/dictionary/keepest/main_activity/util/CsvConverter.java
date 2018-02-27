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

import com.opencsv.CSVReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.DICT_ARRAY_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.DICT_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.FROM_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.IMAGE_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.NOTES_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.STAR_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.TAGS_DICT_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.TAGS_WORD_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.TO_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.TRANS_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.VERSION;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.VERSION_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.WORD_ARRAY_KEY;
import static is.jacek.markowski.dictionary.keepest.main_activity.util.ImportExport.WORD_KEY;


/**
 * Created by jacek on 27.10.17.
 */

public class CsvConverter {
    public static JSONObject convertToJson(File file) {
        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonRoot = new JSONObject();
            JSONArray entries = new JSONArray();
            jsonRoot.put(VERSION_KEY, VERSION);
            jsonRoot.put(DICT_ARRAY_KEY, entries);
            JSONObject dictObj = new JSONObject();
            JSONArray words = new JSONArray();
            dictObj.put(DICT_KEY, file.getName());
            dictObj.put(FROM_KEY, "en");
            dictObj.put(TO_KEY, "en");
            dictObj.put(TAGS_DICT_KEY, "");
            dictObj.put(WORD_ARRAY_KEY, words);
            entries.put(dictObj);
            String[] row = null;
            while ((row = csvReader.readNext()) != null) {

                String word = "";
                String translation = "";
                try {
                    word = row[0];
                    translation = row[1];
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                String wordTags = "";
                try {
                    wordTags = row[2];
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String notes = "";
                try {
                    notes = row[3];
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String image = "";
                try {
                    image = row[4];
                } catch (Exception e) {
                    e.printStackTrace();
                }


                JSONObject wordObj = new JSONObject();
                wordObj.put(WORD_KEY, word);
                wordObj.put(TRANS_KEY, translation);
                wordObj.put(STAR_KEY, 0);
                wordObj.put(NOTES_KEY, notes);
                wordObj.put(TAGS_WORD_KEY, wordTags);
                wordObj.put(IMAGE_KEY, image);
                words.put(wordObj);
            }
            return jsonRoot;

        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        } finally {
            try {
                csvReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
