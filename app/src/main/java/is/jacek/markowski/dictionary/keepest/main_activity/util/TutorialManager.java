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

import android.app.Activity;
import android.view.View;

import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Created by jacek on 9/30/17.
 */

public class TutorialManager {

    public static void showTutorial(Activity activity, View view, String text, String key) {
        new MaterialShowcaseView.Builder(activity)
                .setTarget(view)
                .setShapePadding(100)
                .setDismissOnTouch(true)
                .setDismissOnTargetTouch(true)
                .setContentText(text)
                .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse(key)
                .show();
    }

    public static void showTutorialSequence(Activity activity, List<TutorialItem> items, String key) {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, key);
        sequence.singleUse(key);
        sequence.setConfig(config);

        for (TutorialItem item : items) {
            MaterialShowcaseView.Builder tut = new MaterialShowcaseView.Builder(activity)
                    .setTarget(item.getView())
                    .setShapePadding(100)
                    .setDismissOnTouch(true)
                    .setDismissOnTargetTouch(true)
                    .setContentText(item.getText());

            sequence.addSequenceItem(tut.build());
        }
        sequence.start();
    }

    public static class TutorialItem {
        private View mView;
        private String mText;

        public TutorialItem(View view, String text) {
            mView = view;
            mText = text;
        }

        public View getView() {
            return mView;
        }

        public String getText() {
            return mText;
        }
    }
}

