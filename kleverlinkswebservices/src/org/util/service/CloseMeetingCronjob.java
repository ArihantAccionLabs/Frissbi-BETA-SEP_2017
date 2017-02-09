package org.util.service;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;


public class CloseMeetingCronjob implements Job {

	@Override
	public void execute(JobExecutionContext arg0){
	
		try{
			
			System.out.println(""+new Date());
			TrackMeetingTime.completingMeetingAfterTimeOut();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
