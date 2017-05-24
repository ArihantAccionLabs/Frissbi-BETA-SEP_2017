package com.frissbi.app.Utility;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;

import com.frissbi.app.R;

/**
 * Created by thrymr on 2/1/17.
 */

public class ProgressLogoImageView extends android.support.v7.widget.AppCompatImageView {

    public ProgressLogoImageView(Context context) {
        super(context);
        init();
    }

    public ProgressLogoImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ProgressLogoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        setBackgroundResource(R.drawable.frame_animation);
        final AnimationDrawable frameAnimation = (AnimationDrawable) getBackground();
        post(new Runnable() {
            public void run() {
                frameAnimation.start();
            }
        });
    }

}