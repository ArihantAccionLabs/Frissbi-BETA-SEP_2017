package com.frissbi.interfaces;

import com.frissbi.models.Friend;

/**
 * Created by thrymr on 10/2/17.
 */

public interface FriendRequestListener {
    void sendFriendRequest(Friend friend);

    void viewProfile(Friend friend);
}
