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

import android.content.Context;

import java.util.Arrays;
import java.util.List;

import is.jacek.markowski.dictionary.keepest.R;

/**
 * Created by jacek on 03.07.17.
 */

public class Language {
    public static String getCountryName(Context context, String countryCode) {
        List<String> langValues = Arrays.asList(context.getResources().getStringArray(R.array.selectValues));
        List<String> langList = Arrays.asList(context.getResources().getStringArray(R.array.select));
        int index = langValues.indexOf(countryCode);
        if (index >= 0) {
            return langList.get(index);
        } else {
            return context.getString(R.string.unknown);
        }
    }

    public static String getCountryCode(Context context, String countryName) {
        List<String> langValues = Arrays.asList(context.getResources().getStringArray(R.array.selectValues));
        List<String> langList = Arrays.asList(context.getResources().getStringArray(R.array.select));
        int index = langList.indexOf(countryName);
        if (index >= 0) {
            return langValues.get(index);
        } else {
            return context.getString(R.string.unknown);
        }
    }

    public static int getIndex(Context context, String countryCode) {
        List<String> langValues = Arrays.asList(context.getResources().getStringArray(R.array.selectValues));
        return langValues.indexOf(countryCode);
    }

}
