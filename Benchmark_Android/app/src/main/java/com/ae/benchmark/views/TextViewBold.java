package com.ae.benchmark.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by eheuristic9 on 25/12/17.
 */

public class TextViewBold extends TextView {

    public TextViewBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TextViewBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextViewBold(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/arialbd.ttf");
        setTypeface(tf ,1);

    }

}