package org.kleverlinks.bean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MeetingCreationBean {

	private Long senderUserId;
	private List<Long> meetingIdList = new ArrayList<Long>();
	private String durationTime;
	private LocalDateTime senderFromDateTime;
	private LocalDateTime senderToDateTime;
	private Boolean isLocationSelected;//
	private List<Long> friendsIdList = new ArrayList<Long>();
	private List<String> emailIdList = new ArrayList<String>();
	private List<String> contactList = new ArrayList<String>();
}
