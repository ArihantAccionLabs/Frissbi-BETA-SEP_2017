package com.frissbi.app.Utility;

/**
 * Created by thrymr on 30/6/16.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class CustomFontTextView extends android.support.v7.widget.AppCompatTextView {

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public CustomFontTextView(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Karla-Regular.ttf");
            setTypeface(myTypeface);
            /*TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);

            String fontName = a.getString(R.styleable.CustomFontTextView_font);

            try {
                if (fontName != null) {
                    Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontName);
                    setTypeface(myTypeface);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            a.recycle();*/
        }
    }

}