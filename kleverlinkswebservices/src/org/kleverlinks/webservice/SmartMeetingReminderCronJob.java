package org.kleverlinks.webservice;

import java.util.Date;

import org.quartz.JobExecutionContext;


public class SmartMeetingReminderCronJob implements org.quartz.Job {

	@Override
	public void execute(JobExecutionContext arg0){

		try{
		    //SmartReminderNotification.checkingMeetingOnCurrentDate();
		    System.out.println("SmartMeetingReminderCronJob==========at " + new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
