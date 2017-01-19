package org.kleverlinks.webservice;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.util.service.SmartReminderNotification;


public class SmartMeetingReminderCronJob implements org.quartz.Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		//SmartReminderNotification.checkingMeetingOnCurrentDate();
		System.out.println("SmartMeetingReminderCronJob==========at " + new Date());
	}
}
