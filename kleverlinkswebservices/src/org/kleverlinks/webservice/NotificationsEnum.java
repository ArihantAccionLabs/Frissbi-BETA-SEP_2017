package org.kleverlinks.webservice;

public enum NotificationsEnum {
	
	Friend_Pending_Requests(1), Friend_Request_Acceptance(2), Meeting_Pending_Requests(3), Meeting_Request_Acceptance(4),
	Meeting_Time_Changed(5), Meeting_Place_Changed(6), Meeting_Rejected(7) ,Meeting_Summary(8);

	 private int value;

     private NotificationsEnum(int value) {
             this.value = value;
     }

}
