package com.frissbi.app.interfaces;

import com.frissbi.app.models.FrissbiGroup;

/**
 * Created by thrymr on 2/3/17.
 */

public interface GroupDetailsListener {
    void showGroupDetails(FrissbiGroup group);
    void viewOrExitGroup(FrissbiGroup group);
}
