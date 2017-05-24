package com.frissbi.app.interfaces;

import com.frissbi.app.models.FrissbiContact;

/**
 * Created by thrymr on 10/2/17.
 */

public interface FriendRequestListener {
    void sendFriendRequest(FrissbiContact frissbiContact);

    void viewProfile(FrissbiContact frissbiContact);
}
