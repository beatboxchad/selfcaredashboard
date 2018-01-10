package com.beatboxchad.android.selfcaredashboard;

import android.databinding.BindingAdapter;
import android.widget.TextView;

/**
 * Created by chad on 1/9/18.
 */

public class BindingAdapters {
    @BindingAdapter("android:textColor")
    public static void customGoalColor(TextView textView, int color) {
        textView.setTextColor(color);
    }
}
