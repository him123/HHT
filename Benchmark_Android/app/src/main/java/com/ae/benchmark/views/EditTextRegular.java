package com.ae.benchmark.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by eheuristic9 on 25/12/17.
 */

public class EditTextRegular extends EditText {

    public EditTextRegular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public EditTextRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditTextRegular(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/arial.ttf");
        setTypeface(tf ,1);

    }

}