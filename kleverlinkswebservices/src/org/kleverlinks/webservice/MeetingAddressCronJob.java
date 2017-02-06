package org.kleverlinks.webservice;


import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.util.service.TrackMeetingTime;

public class MeetingAddressCronJob implements Job{

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		//TrackMeetingTime.sendMeetingNotificationBeforeOneHour();
		System.out.println("TrackingMeetingAddress====   : "+ new Date());
	}
}
