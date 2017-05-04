package com.frissbi.interfaces;


import com.frissbi.Utility.EndlessScrollView;

/**
 * Created by thrymr on 1/4/17.
 */

public interface EndlessScrollListener {
    void onScrollChanged(EndlessScrollView scrollView, int x, int y, int oldx, int oldy);
}