package com.frissbi.interfaces;

import com.frissbi.models.Friend;
import com.frissbi.models.FrissbiContact;

/**
 * Created by thrymr on 10/2/17.
 */

public interface FriendRequestListener {
    void sendFriendRequest(FrissbiContact frissbiContact);

    void viewProfile(FrissbiContact frissbiContact);
}
