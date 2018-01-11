package com.beatboxchad.android.selfcaredashboard;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by chad on 1/9/18.
 */

public class BindingAdapters {
    @BindingAdapter("android:backgroundColor")
    public static void customGoalColor(TextView textView, int color) {
        textView.setBackgroundColor(color);
    }
//    //https://stackoverflow.com/questions/38818866/cant-get-android-two-way-data-binding-to-work-intellij-idea
//    @InverseBindingAdapter(attribute = "android:text")
//    public static String captureTextValue(EditText view) {
//        return view.getText();
//    }
}
