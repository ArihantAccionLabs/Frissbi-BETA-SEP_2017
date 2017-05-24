package com.frissbi.app.Utility;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by thrymr on 12/5/17.
 */

public class CustomEditText extends android.support.v7.widget.AppCompatEditText {

    public CustomEditText(Context context) {
        super(context);
        init(null);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Karla-Regular.ttf");
            setTypeface(myTypeface);
        }
    }

}
