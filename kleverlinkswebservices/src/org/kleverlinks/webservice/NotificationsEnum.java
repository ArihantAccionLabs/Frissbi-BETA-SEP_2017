package org.kleverlinks.webservice;


public enum NotificationsEnum {
	
	FRIEND_PENDING_REQUESTS(1), FRIEND_REQUEST_ACCEPTANCE(2), MEETING_PENDING_REQUESTS(3), MEETING_REQUEST_ACCEPTANCE(4),
	MEETING_TIME_CHANGED(5), MEETING_PLACE_CHANGED(6), MEETING_REJECTED(7) ,MEETING_SUMMARY(8) , MEETING_LOCATION_SUGGESTION(9);

	 private int value;

     private NotificationsEnum(int value) {
             this.value = value;
     }
     
}
