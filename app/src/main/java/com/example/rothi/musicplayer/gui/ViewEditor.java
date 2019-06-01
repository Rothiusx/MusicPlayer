package com.example.rothi.musicplayer.gui;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.example.rothi.musicplayer.R;

public class ViewEditor {

    private View view;
    private Context context;

    public ViewEditor(View view) {
        this.view = view;
    }

    public ViewEditor(View view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void alphaAnimation(float alphaStart, float alphaEnd, long duration) {
        Animation animation = new AlphaAnimation(alphaStart, alphaEnd);

        animation.setDuration(duration);
        view.startAnimation(animation);
    }

    public void setTextView(int id, boolean selected, boolean horrizontallyScrolling) {
        TextView textView = view.findViewById(id);

        textView.setSelected(selected);
        textView.setHorizontallyScrolling(horrizontallyScrolling);
    }

    public void setTextView(int id, boolean selected, boolean horrizontallyScrolling, boolean bold) {
        TextView textView = view.findViewById(id);

        textView.setSelected(selected);
        textView.setHorizontallyScrolling(horrizontallyScrolling);
        if (bold) {
            textView.setTypeface(null, Typeface.BOLD);
        }
        else {
            textView.setTypeface(null, Typeface.NORMAL);
        }
    }

    public void setTextView(int id, boolean selected, boolean horrizontallyScrolling, boolean bold, int color) {
        TextView textView = view.findViewById(id);

        textView.setSelected(selected);
        textView.setHorizontallyScrolling(horrizontallyScrolling);
        textView.setTextColor(ContextCompat.getColor(context, color));
        if (bold) {
            textView.setTypeface(null, Typeface.BOLD);
        }
        else {
            textView.setTypeface(null, Typeface.NORMAL);
        }
    }
}
