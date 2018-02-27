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
import android.widget.Toast;

import is.jacek.markowski.dictionary.keepest.R;

public class Text {
    private static final int MAX_SIZE = 100;
    private static Toast mToast;

    public static String shrinkText(String text) {
        if (text.length() > MAX_SIZE) {
            text = text.substring(0, MAX_SIZE);
        }
        return text.trim();
    }

    public static boolean validate(Context context, String text) {
        if (mToast != null) {
            mToast.cancel();
        }
        text = text.trim();
        if (text.length() == 0) {
            mToast = Toast.makeText(context, R.string.empty_fields_not_allowed, Toast.LENGTH_SHORT);
            mToast.show();
        }
        return text.length() > 0;
    }
}
