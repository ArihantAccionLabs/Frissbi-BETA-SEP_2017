package com.frissbi.Utility;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.frissbi.interfaces.EndlessScrollListener;


/**
 * Created by thrymr on 1/4/17.
 */

public class EndlessScrollView extends ScrollView
{
    private EndlessScrollListener endlessScrollListener  = null;
    public EndlessScrollView(Context context) {
        super(context);
    }

    public EndlessScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EndlessScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(EndlessScrollListener endlessScrollListener) {
        this.endlessScrollListener = endlessScrollListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (endlessScrollListener != null) {
            endlessScrollListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }


}