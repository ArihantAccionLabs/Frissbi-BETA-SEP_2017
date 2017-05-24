package com.frissbi.app.interfaces;


import com.frissbi.app.Utility.EndlessScrollView;

/**
 * Created by thrymr on 1/4/17.
 */

public interface EndlessScrollListener {
    void onScrollChanged(EndlessScrollView scrollView, int x, int y, int oldx, int oldy);
}